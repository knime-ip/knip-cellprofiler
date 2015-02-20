package org.knime.knip.cellprofiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.iterableinterval.unary.MinMax;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;

import org.apache.commons.io.FileUtils;
import org.cellprofiler.knimebridge.CellProfilerException;
import org.cellprofiler.knimebridge.IFeatureDescription;
import org.cellprofiler.knimebridge.IKnimeBridge;
import org.cellprofiler.knimebridge.KBConstants;
import org.cellprofiler.knimebridge.KnimeBridgeFactory;
import org.cellprofiler.knimebridge.PipelineException;
import org.cellprofiler.knimebridge.ProtocolException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.cellprofiler.data.CellProfilerCell;
import org.knime.knip.cellprofiler.data.CellProfilerContent;
import org.knime.knip.cellprofiler.data.CellProfilerMeasurementTable;
import org.zeromq.ZMQException;

/**
 * Starts and manages an instance of CellProfiler.
 * 
 * @author Patrick Winter, University of Konstanz
 * @author Christian Dietz, University of Konstanz
 */
@SuppressWarnings("deprecation")
public class CellProfilerInstance {

	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(CellProfilerInstance.class);

	private Process m_cellProfilerProcess;

	private boolean closed = false;

	private IKnimeBridge m_knimeBridge = new KnimeBridgeFactory()
			.newKnimeBridge();

	private int m_port;

	/**
	 * Creates a CellProfiler instance in a separate Python process and connects
	 * to it via TCP.
	 * 
	 * @throws IOException
	 *             If something goes wrong
	 * @throws URISyntaxException
	 * @throws ProtocolException
	 * @throws ZMQException
	 * @throws PipelineException
	 */
	public CellProfilerInstance() throws IOException, ZMQException,
			ProtocolException, URISyntaxException, PipelineException {
		// Do some error checks on the configured module path
		final String[] cellProfilerCommand = CellProfilerPreferencePage
				.getCellProfilerCommand();

		// Get a free port for communication with CellProfiler
		m_port = getFreePort();
		// Start CellProfiler
		ProcessBuilder processBuilder = new ProcessBuilder(
				cellProfilerCommand[0], cellProfilerCommand[1],
				"--knime-bridge-address=tcp://127.0.0.1:" + m_port);
		m_cellProfilerProcess = processBuilder.start();

		startStreamListener(m_cellProfilerProcess.getInputStream(), false);
		startStreamListener(m_cellProfilerProcess.getErrorStream(), true);

		// Connect to CellProfiler via the given port
		m_knimeBridge.connect(new URI("tcp://127.0.0.1:" + m_port));
	}

	public void loadPipeline(final String pipelineFile) throws ZMQException,
			PipelineException, ProtocolException, IOException {
		m_knimeBridge.loadPipeline(FileUtils.readFileToString(new File(
				pipelineFile)));
		m_knimeBridge.cleanPipeline();
	}

	/**
	 * @return The number of images expected by the pipeline.
	 */
	public String[] getInputParameters() {
		List<String> inputParameters = m_knimeBridge.getInputChannels();
		return inputParameters.toArray(new String[inputParameters.size()]);
	}

	/**
	 * @return Spec of the output produced by the pipeline.
	 */
	public static DataTableSpec getOutputSpec(final DataTableSpec inSpec,
			final Pair<String, String>[] imageColumns,
			final List<String> objectNames) {
		// Passing null to createColumnRearranger will cause an NPE if we use it
		// for more than the spec
		return createColumnRearranger(inSpec, imageColumns, null, objectNames)
				.createSpec();
	}

	/**
	 * Executes the pipeline and returns the results.
	 * 
	 * @param exec
	 *            Execution context needed to create a new table.
	 * @param inputTable
	 *            The input table.
	 * @param imageColumns
	 *            The image columns used by the pipeline.
	 * @return Table containing the metrics calculated by the pipeline.
	 * @throws IOException
	 *             If something goes wrong.
	 * @throws ProtocolException
	 * @throws PipelineException
	 * @throws CellProfilerException
	 * @throws ZMQException
	 * @throws CanceledExecutionException
	 */
	public BufferedDataTable execute(ExecutionContext exec,
			BufferedDataTable inputTable, Pair<String, String>[] imageColumns,
			List<String> objectNames) throws IOException, ZMQException,
			CellProfilerException, PipelineException, ProtocolException,
			CanceledExecutionException {
		ColumnRearranger colRearranger = createColumnRearranger(
				inputTable.getDataTableSpec(), imageColumns, m_knimeBridge,
				objectNames);
		return exec.createColumnRearrangeTable(inputTable, colRearranger, exec);
	}

	private static CellProfilerCell[] createCellProfilerContentCell(
			final String parentKey, final IKnimeBridge knimeBridge) {

		final List<String> measurementNames = knimeBridge.getResultTableNames();
		final CellProfilerCell[] cells = new CellProfilerCell[measurementNames
				.size()];

		int i = 0;
		for (String measurementName : measurementNames) {
			CellProfilerMeasurementTable measurement = new CellProfilerMeasurementTable(
					parentKey);
			for (IFeatureDescription featureDescription : knimeBridge
					.getFeatures(measurementName)) {
				if (featureDescription.getType().equals(Double.class)) {
					double[] values = knimeBridge
							.getDoubleMeasurements((IFeatureDescription) featureDescription);
					measurement.addDoubleFeature(featureDescription.getName(),
							values);
				} else if (featureDescription.getType().equals(Float.class)) {
					float[] values = knimeBridge
							.getFloatMeasurements((IFeatureDescription) featureDescription);
					measurement.addFloatFeature(featureDescription.getName(),
							values);
				} else if (featureDescription.getType().equals(Integer.class)) {
					int[] values = knimeBridge
							.getIntMeasurements((IFeatureDescription) featureDescription);
					measurement.addIntegerFeature(featureDescription.getName(),
							values);
				} else if (featureDescription.getType().equals(String.class)) {
					String value = knimeBridge
							.getStringMeasurement((IFeatureDescription) featureDescription);
					measurement.addStringFeature(featureDescription.getName(),
							value);
				}
			}

			cells[i++] = new CellProfilerCell(new CellProfilerContent(
					parentKey, measurement,
					measurementName.equals(KBConstants.IMAGE)));
		}

		return cells;
	}

	private void startStreamListener(final InputStream stream,
			final boolean error) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						stream));
				String line = null;
				try {
					while ((line = in.readLine()) != null) {
						if (error) {
							LOGGER.warn(line);
						} else {
							LOGGER.debug(line);
						}
					}
				} catch (IOException e) {
					// Once the process is killed the stream will be closed and
					// we will end here
				}
			}
		}).start();

	}

	/**
	 * Shuts down the CellProfiler instance.
	 */
	public void close() {
		if (!closed) {
			closed = true;
			m_knimeBridge.disconnect();
			m_cellProfilerProcess.destroy();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	/**
	 * Finds a free TCP port.
	 * 
	 * @return A free TCP port.
	 * @throws IOException
	 *             If opening the server socket fails.
	 */
	private static int getFreePort() throws IOException {
		int port;
		try {
			// With the argument 0 the socket finds a free port
			ServerSocket socket = new ServerSocket(0);
			port = socket.getLocalPort();
			socket.close();
		} catch (IOException e) {
			throw new IOException("Could not get a free port", e);
		}
		return port;
	}

	/**
	 * @return number of output measurements
	 */
	public List<String> getObjectNames() {
		return m_knimeBridge.getResultTableNames();
	}

	private static ColumnRearranger createColumnRearranger(
			final DataTableSpec inSpec,
			final Pair<String, String>[] imageColumns,
			final IKnimeBridge knimeBridge, final List<String> objectNames) {

		final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		final DataColumnSpec[] colSpecs = new DataColumnSpec[objectNames.size()];
		int k = 0;
		for (final String colName : objectNames) {
			final String columnName = DataTableSpec.getUniqueColumnName(inSpec,
					"CellProfiler Measurement: [" + colName + "]");
			colSpecs[k++] = new DataColumnSpecCreator(columnName,
					CellProfilerCell.TYPE).createSpec();
		}

		final int[] colIndexes = new int[imageColumns.length];
		for (int i = 0; i < imageColumns.length; i++) {
			colIndexes[i] = inSpec.findColumnIndex(imageColumns[i].getSecond());
		}
		CellFactory factory = new AbstractCellFactory(colSpecs) {

			private Interval[] m_reference;

			@Override
			public DataCell[] getCells(final DataRow row) {
				try {
					return createCells(row, inSpec, imageColumns, colIndexes,
							knimeBridge);
				} catch (ZMQException | ProtocolException
						| CellProfilerException | PipelineException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			private <T extends RealType<T>> DataCell[] createCells(
					final DataRow row, final DataTableSpec inSpec,
					final Pair<String, String>[] imageColumns,
					final int[] colIndexes, final IKnimeBridge knimeBridge)
					throws ProtocolException, ZMQException,
					CellProfilerException, PipelineException {
				boolean group = false;
				Map<String, ImgPlus<?>> images = new HashMap<String, ImgPlus<?>>();
				for (int i = 0; i < colIndexes.length; i++) {
					final ImgPlusValue<T> value = (ImgPlusValue<T>) row
							.getCell(colIndexes[i]);

					if (m_reference == null || m_reference[i] == null) {
						m_reference = new Interval[colIndexes.length];
						m_reference[i] = value.getImgPlus();
					} else if (!Intervals.equalDimensions(m_reference[i],
							value.getImgPlus())) {
						throw new IllegalStateException(
								"All images in one column must have the same dimensionality!");
					}

					// convert to floats
					final RandomAccessibleInterval<FloatType> converted = Converters
							.convert((RandomAccessibleInterval<T>) value
									.getImgPlus(),
									new FloatConverter<T>(value.getImgPlus()),
									new FloatType());

					if (converted.numDimensions() == 2
							|| (converted.numDimensions() == 3 && value
									.getImgPlus().axis(2).type()
									.equals(Axes.CHANNEL))) {
						try {
							images.put(
									imageColumns[i].getFirst(),
									new ImgPlus(
											new ImgView<FloatType>(
													converted,
													value.getImgPlus()
															.factory()
															.imgFactory(
																	new FloatType())),
											value.getImgPlus()));
						} catch (IncompatibleTypeException e) {
							throw new RuntimeException(e);
						}
					} else {
						try {
							images.put(
									imageColumns[i].getFirst(),
									new ImgPlus(
											new ImgView<FloatType>(
													converted,
													value.getImgPlus()
															.factory()
															.imgFactory(
																	new FloatType())),
											value.getImgPlus()));
						} catch (IncompatibleTypeException e) {
							throw new RuntimeException(e);
						}
						group = true;
					}
				}
				if (group) {
					knimeBridge.runGroup(images);
				} else {
					knimeBridge.run(images);
				}

				return createCellProfilerContentCell(row.getKey().getString(),
						knimeBridge);
			}

			/**
			 * Helper to convert pixels of images to floats in range [0..1]
			 * 
			 * @author Christian Dietz, University of Konstanz
			 *
			 * @param <T>
			 */
			class FloatConverter<T extends RealType<T>> implements
					Converter<T, FloatType> {

				private final double max;
				private final double min;

				public FloatConverter(final Img<T> input) {

					final ValuePair<T, T> res = Operations.compute(
							new MinMax<T>(), input);

					this.min = res.getA().getRealDouble();
					this.max = res.getB().getRealDouble();
				}

				@Override
				public void convert(final T arg0, final FloatType arg1) {
					arg1.setReal((arg0.getRealFloat() - min) / (max - min));
				}

			}
		};
		// Append columns from the factory
		rearranger.append(factory);
		return rearranger;
	}

}
