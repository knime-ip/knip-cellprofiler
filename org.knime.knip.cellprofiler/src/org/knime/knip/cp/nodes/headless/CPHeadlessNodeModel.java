package org.knime.knip.cp.nodes.headless;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.io.nodes.imgwriter.ImgWriter;

public class CPHeadlessNodeModel extends NodeModel {

	protected static SettingsModelString createPathToCPInstallationModel() {
		return new SettingsModelString("path_to_cellprofiler_installation", "");
	}

	protected static SettingsModelString createPathToCPPipelineModel() {
		return new SettingsModelString("path_to_cp_pipeline", "");
	}

	public static SettingsModelString createImageColumnNameModel() {
		return new SettingsModelString("image_column", "");
	}

	public static SettingsModelString createMoreImageColumnNameModel() {
		return new SettingsModelString("more_image_column", "");
	}

	protected CPHeadlessNodeModel() {
		// first port contains images (TODO, IGNORED FOR NOW)
		// second port contains measurements of source images
		// third node contains per cell statistics
		super(1, 2);
	}

	private SettingsModelString m_pathToCPInstallationModel = createPathToCPInstallationModel();

	private SettingsModelString m_pathToCPPipelineModel = createPathToCPPipelineModel();

	private SettingsModelString m_imgColumnNameModel = createImageColumnNameModel();

	private SettingsModelString m_moreImgColumnNameModel = createMoreImageColumnNameModel();

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// TODO
		// we don't know the output of the pipeline, yet.
		// hopefully future work will make it possible to define the layout of
		// all three output tables prior to
		// executing the pipeline. Maybe this information can be stored in the
		// project file of cellprofiler (-> knime information)

		return null;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		// create tmp dir
		String tmpDirPath = System.getProperty("java.io.tmpdir") + "/cp"
				+ System.currentTimeMillis() + "/";

		File tmpDir = new File(tmpDirPath);
		tmpDir.mkdir();

		// write out images
		final CloseableRowIterator it = inData[0].iterator();

		new File(tmpDirPath + "in").mkdir();
		File outDir = new File(tmpDirPath + "out");
		outDir.mkdir();
		new File(tmpDirPath + "in/input.csv").createNewFile();

		String pipelineName = null;
		// copy all relevant files into in
		for (final File f : new File(m_pathToCPPipelineModel.getStringValue())
				.listFiles()) {
			if (f.getAbsolutePath().endsWith(".csv")
					|| f.getAbsolutePath().endsWith("cppipe")) {
				FileUtils.copyFile(f,
						new File(tmpDirPath + "in/" + f.getName()));
			}

			if (pipelineName == null && f.getAbsolutePath().endsWith("cppipe")) {
				pipelineName = f.getName();
			}
		}

		final BufferedWriter writer = new BufferedWriter(new FileWriter(
				tmpDirPath + "in/input.csv"));

		// TODO if we have two columns, we should write two columns etc
		// TODO Actually make use of dialog components
		// TODO Writing images on disc like this give a certain overhead.
		// Especially as we already might have written them to disk using KNIME
		// internal storage format
		// TODO we can try to write a BioFormatsReader which reads KNIME data
		// and this one to CellProfiler. Like this we avoid writing data too
		// oftenG

		// TODO We should be smarter about the pipelines we support. We can
		// check if load images is used and how many images columns are expected
		// as input
		// TODO We can then auto-generate the column selection depending on the
		// CellProfiler Pipeline
		// TODO Maybe we can even replace other types of data loading (types /
		// names, metadata filter etc) with LoadImage Modules automatically.
		// TODO Maybe we can create more customized dialogs for the
		// CellProfilerPipelines

		// TODO Better Control of Column Selection (sanity/validation checks
		// etc)
		int idxA = inData[0].getDataTableSpec().findColumnIndex(
				m_imgColumnNameModel.getStringValue());
		int idxB = inData[0].getDataTableSpec().findColumnIndex(
				m_moreImgColumnNameModel.getStringValue());

		while (it.hasNext()) {
			DataRow next = it.next();
			final ImgPlusValue<?> value = (ImgPlusValue<?>) next.getCell(idxA);
			final ImgPlusValue<?> moreValue = (ImgPlusValue<?>) next
					.getCell(idxB);

			final int[] map = new int[value.getDimensions().length];

			for (int i = 0; i < map.length; i++) {
				map[i] = i;
			}

			// TODO use better prefixed (not A_ and B_). Maybe they can be
			// derived from the pipeline
			ImgWriter imgWriter = new ImgWriter();
			imgWriter.writeImage(value.getImgPlus(), tmpDirPath + "in/" + "A_"
					+ next.getKey().getString() + ".tif",
					"Tagged Image File Format (tif)", "Uncompressed", map);

			imgWriter.writeImage(moreValue.getImgPlus(), tmpDirPath + "in/"
					+ "B_" + next.getKey().getString() + ".tif",
					"Tagged Image File Format (tif)", "Uncompressed", map);

			writer.write("A_" + next.getKey().getString() + ".tif" + "," + "B_"
					+ next.getKey().getString() + ".tif" + "\n");
		}

		writer.close();

		ProcessBuilder processBuilder = new ProcessBuilder("python",
				m_pathToCPInstallationModel.getStringValue()
						+ "/CellProfiler.py", "-c", "-r", "-p",
				m_pathToCPPipelineModel.getStringValue() + "/" + pipelineName,
				"-i", tmpDirPath + "in", "-o", tmpDirPath + "out", "-b",
				"--do-not-fetch", "--data-file", "input.csv");

		processBuilder.environment().put("JAVA_HOME",
				System.getProperty("java.home"));

		// TODO can we be smarter here?
		processBuilder.environment().put("LD_LIBRARY_PATH",
				"/opt/java/64/lib/amd64:/opt/java/64/jre/lib/amd64/server");

		// TODO: Better propagation of output messages to the end user
		Process process = processBuilder.start();

		copy(process.getInputStream(), System.out);

		process.waitFor();

		// TODO this can be done better
		// TODO also IntegerCells should be supported
		final Map<String, List<String[]>> rowMap = new HashMap<>();

		// we need to maintain this list, as file order is important
		final List<String> perObjectFiles = new ArrayList<String>();
		final List<String> perImageFiles = new ArrayList<String>();

		// spec for data per image // per object
		// TODO later we should also add the output images of CellProfiler
		DataTableSpec perImageSpec = null, perObjectSpec = null;
		try {
			for (final File f : outDir.listFiles()) {
				String absolutPath = f.getAbsolutePath();
				if (absolutPath.endsWith(".csv")) {
					ArrayList<String[]> rows = new ArrayList<String[]>();
					rowMap.put(absolutPath, rows);
					final BufferedReader reader = new BufferedReader(
							new FileReader(f));

					final String[] headers = reader.readLine().split(",");
					final DataColumnSpec[] specs = new DataColumnSpec[headers.length];

					final String[] row = reader.readLine().split(",");

					for (int j = 0; j < headers.length; j++) {
						try {
							Double.valueOf(row[j]);
							specs[j] = new DataColumnSpecCreator(headers[j],
									DoubleCell.TYPE).createSpec();
							continue;
						} catch (NumberFormatException e) {
							// we just go on
						}
						specs[j] = new DataColumnSpecCreator(headers[j],
								StringCell.TYPE).createSpec();
					}

					rows.add(row);

					// read the rest of the file
					String line = null;
					while ((line = reader.readLine()) != null) {
						rows.add(line.split(","));
					}

					// TODO we really have to do checks here if tables have same
					// number of elements etc if we join them and check if the
					// sorting of the csvs is correct (or discuss with
					// CellProfiler).

					final DataTableSpec currentSpec = new DataTableSpec(specs);

					if (rows.size() == inData[0].getRowCount()) {
						if (perImageSpec != null) {
							perImageSpec = mergeDataTableSpecs(perImageSpec,
									currentSpec);
						} else {
							perImageSpec = currentSpec;
						}

						perImageFiles.add(absolutPath);
					} else {
						if (perObjectSpec != null) {
							perObjectSpec = mergeDataTableSpecs(perObjectSpec,
									currentSpec);
						} else {
							perObjectSpec = currentSpec;
						}

						perObjectFiles.add(absolutPath);
					}
					reader.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			tmpDir.delete();
		}

		return new BufferedDataTable[] {
				createAndFillContainer(perImageSpec, rowMap, perImageFiles,
						exec),
				createAndFillContainer(perObjectSpec, rowMap, perObjectFiles,
						exec) };
	}

	// Create container with results
	private BufferedDataTable createAndFillContainer(DataTableSpec spec,
			final Map<String, List<String[]>> rowMap,
			final List<String> fileNames, final ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(spec);

		for (int r = 0; r < rowMap.get(fileNames.get(0)).size(); r++) {
			final DataCell[] cells = new DataCell[spec.getNumColumns()];
			int j = 0;
			for (String file : fileNames) {
				for (String val : rowMap.get(file).get(r)) {
					if (spec.getColumnSpec(j).getType()
							.isCompatible(DoubleValue.class)) {
						cells[j] = new DoubleCell(Double.valueOf(val));
					} else {
						cells[j] = new StringCell(val);
					}

					++j;
				}
			}
			container.addRowToTable(new DefaultRow("Row" + r, cells));
		}

		container.close();
		return container.getTable();
	}

	private DataTableSpec mergeDataTableSpecs(DataTableSpec perObjectSpec,
			DataTableSpec currentSpec) {

		DataColumnSpec[] spec = new DataColumnSpec[perObjectSpec
				.getNumColumns() + currentSpec.getNumColumns()];

		HashMap<String, Integer> colNames = new HashMap<>();

		for (int s = 0; s < perObjectSpec.getNumColumns(); s++) {
			spec[s] = perObjectSpec.getColumnSpec(s);
			colNames.put(spec[s].getName(), 0);
		}

		int offset = perObjectSpec.getNumColumns();
		for (int s = 0; s < currentSpec.getNumColumns(); s++) {
			DataColumnSpec colSpec = currentSpec.getColumnSpec(s);
			if (colNames.containsKey(colSpec.getName())) {
				colSpec = new DataColumnSpecCreator(colSpec.getName() + "(#"
						+ 1 + ")" + System.currentTimeMillis(),
						colSpec.getType()).createSpec();
			}

			spec[offset + s] = colSpec;
		}

		return new DataTableSpec(spec);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_pathToCPInstallationModel.saveSettingsTo(settings);
		m_pathToCPPipelineModel.saveSettingsTo(settings);
		m_imgColumnNameModel.saveSettingsTo(settings);
		m_moreImgColumnNameModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_pathToCPInstallationModel.validateSettings(settings);
		m_pathToCPPipelineModel.validateSettings(settings);
		m_imgColumnNameModel.validateSettings(settings);
		m_moreImgColumnNameModel.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_pathToCPInstallationModel.loadSettingsFrom(settings);
		m_pathToCPPipelineModel.loadSettingsFrom(settings);
		m_imgColumnNameModel.loadSettingsFrom(settings);
		m_moreImgColumnNameModel.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

	static void copy(InputStream in, OutputStream out) throws IOException {
		while (true) {
			int c = in.read();
			if (c == -1)
				break;
			out.write((char) c);
		}
	}

}
