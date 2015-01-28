package org.knime.knip.cellprofiler.nodes.pipelineexecutor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.cellprofiler.knimebridge.PipelineException;
import org.cellprofiler.knimebridge.ProtocolException;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.cellprofiler.CellProfilerInstance;

/**
 * CellProfiler Pipeline Executor node model.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class PipelineExecutorNodeModel extends NodeModel {

	private PipelineExecutorNodeConfig m_config = new PipelineExecutorNodeConfig();

	/**
	 * Constructor.
	 */
	public PipelineExecutorNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		BufferedDataTable table;
		CellProfilerInstance cellProfiler = new CellProfilerInstance(
				m_config.getPipelineFile());
		try {
			table = cellProfiler.execute(exec, inData[0],
					createInputParameters());
		} finally {
			cellProfiler.close();
		}
		return new BufferedDataTable[] { table };
	}

	private Pair<String, String>[] createInputParameters() {
		String[] parameterNames = m_config.getInputParameters();
		String[] parameterValues = m_config.getImageColumns();
		@SuppressWarnings("unchecked")
		Pair<String, String>[] parameters = new Pair[parameterNames.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new Pair<String, String>(parameterNames[i],
					parameterValues[i]);
		}
		return parameters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// Check pipeline file
		String pipelineFile = m_config.getPipelineFile();
		if (pipelineFile.isEmpty()) {
			throw new InvalidSettingsException("No pipeline file selected");
		}
		if (!new File(pipelineFile).exists()) {
			throw new InvalidSettingsException("The pipeline file "
					+ pipelineFile + " does not exist");
		}
		if (new File(pipelineFile).isDirectory()) {
			throw new InvalidSettingsException("The pipeline file "
					+ pipelineFile + " is a folder");
		}
		// Create output spec
		DataTableSpec spec;
		CellProfilerInstance cellProfiler;
		try {
			cellProfiler = new CellProfilerInstance(pipelineFile);
		} catch (IOException | ProtocolException | URISyntaxException
				| PipelineException e) {
			throw new InvalidSettingsException(e.getMessage(), e);
		}
		try {
			// Check if pipeline input parameters have changed
			if (!Arrays.equals(cellProfiler.getInputParameters(),
					m_config.getInputParameters())) {
				throw new InvalidSettingsException(
						"The input parameters of the pipeline have changed");
			}
			// Check column configuration
			String[] imageColumns = m_config.getImageColumns();
			for (int i = 0; i < imageColumns.length; i++) {
				if (imageColumns[i].isEmpty()) {
					throw new InvalidSettingsException("Image " + (i + 1)
							+ " not selected");
				}
				DataColumnSpec imageColumnSpec = inSpecs[0]
						.getColumnSpec(imageColumns[i]);
				if (imageColumnSpec == null) {
					throw new InvalidSettingsException("The column "
							+ imageColumns[i]
							+ " is missing in the input table");
				}
				if (!imageColumnSpec.getType().isCompatible(ImgPlusValue.class)) {
					throw new InvalidSettingsException("The column "
							+ imageColumns[i]
							+ " is not of the type image plus");
				}
			}
			spec = cellProfiler.getOutputSpec(inSpecs[0],
					createInputParameters());
		} finally {
			cellProfiler.close();
		}
		return new DataTableSpec[] { spec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_config.saveConfig(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		new PipelineExecutorNodeConfig().loadConfig(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		PipelineExecutorNodeConfig config = new PipelineExecutorNodeConfig();
		config.loadConfig(settings);
		m_config = config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

}
