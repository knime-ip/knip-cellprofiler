package org.knime.knip.cellprofiler.nodes.contentexpander;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * CellProfiler Pipeline Executor node config.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class MeasurementToTableNodeConfig {

	private static final String PIPELINE_FILE_CFG = "pipelineFile";

	private String m_pipelineFile = "";

	private static final String IMAGE_COLUMNS_CFG = "imageColumns";

	private String[] m_imageColumns = new String[0];

	private static final String INPUT_PARAMETERS_CFG = "inputParameters";

	private String[] m_inputParameters = new String[0];

	/**
	 * Loads previously saved node settings.
	 * 
	 * @param settings
	 *            The settings to load from.
	 * @throws InvalidSettingsException
	 *             If the settings are invalid.
	 */
	public void loadConfig(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_pipelineFile = settings.getString(PIPELINE_FILE_CFG);
		m_imageColumns = settings.getStringArray(IMAGE_COLUMNS_CFG);
		m_inputParameters = settings.getStringArray(INPUT_PARAMETERS_CFG);
	}

	/**
	 * Loads previously saved node settings.
	 * 
	 * @param settings
	 *            The settings to load from.
	 */
	public void loadConfigInDialog(NodeSettingsRO settings) {
		m_pipelineFile = settings.getString(PIPELINE_FILE_CFG, "");
		m_imageColumns = settings.getStringArray(IMAGE_COLUMNS_CFG,
				new String[0]);
		m_inputParameters = settings.getStringArray(INPUT_PARAMETERS_CFG,
				new String[0]);
	}

	/**
	 * Saves node settings.
	 * 
	 * @param settings
	 *            The settings to save to.
	 */
	public void saveConfig(NodeSettingsWO settings) {
		settings.addString(PIPELINE_FILE_CFG, m_pipelineFile);
		settings.addStringArray(IMAGE_COLUMNS_CFG, m_imageColumns);
		settings.addStringArray(INPUT_PARAMETERS_CFG, m_inputParameters);
	}

	/**
	 * @return The pipeline file.
	 */
	public String getPipelineFile() {
		return m_pipelineFile;
	}

	/**
	 * @param pipelineFile
	 *            The pipeline file.
	 */
	public void setPipelineFile(String pipelineFile) {
		m_pipelineFile = pipelineFile;
	}

	/**
	 * @return The image columns.
	 */
	public String[] getImageColumns() {
		return m_imageColumns;
	}

	/**
	 * @param imageColumns
	 *            The image columns.
	 */
	public void setImageColumns(String[] imageColumns) {
		m_imageColumns = imageColumns;
	}

	/**
	 * @return The input parameters
	 */
	public String[] getInputParameters() {
		return m_inputParameters;
	}

	/**
	 * @param inputParameters
	 *            The input parameters.
	 */
	public void setInputParameters(String[] inputParameters) {
		m_inputParameters = inputParameters;
	}

}
