package org.knime.knip.cp.nodes.headless;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class CPHeadlessNodeModel extends NodeModel {

	protected static SettingsModelString createPathToCellProfilerModel() {
		return new SettingsModelString("path_to_cellprofiler", "");
	}

	protected static SettingsModelString createPathToPipelineModel() {
		return new SettingsModelString("path_to_pipeline", "");
	}

	public static SettingsModelString createImageColumnNameModel() {
		return new SettingsModelString("image_column", "");
	}

	protected CPHeadlessNodeModel() {
		// first port contains images
		// second port contains measurements of source images
		// third node contains per cell statistics
		super(1, 3);
	}

	private SettingsModelString m_pathToCellProfilerModel = createPathToCellProfilerModel();

	private SettingsModelString m_pathToPipelineModel = createPathToPipelineModel();

	private SettingsModelString m_imgColumnNameModel = createImageColumnNameModel();

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

		// a. read in hdf project file and parse the output information. like
		// file names etc

		// b. run cellprofiler headless and create results in java tmp.dir

		// c. convert results into knime tables

		// d. delete tmp results.

		return super.execute(inData, exec);
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
		m_pathToCellProfilerModel.saveSettingsTo(settings);
		m_pathToPipelineModel.saveSettingsTo(settings);
		m_imgColumnNameModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_pathToCellProfilerModel.validateSettings(settings);
		m_pathToPipelineModel.validateSettings(settings);
		m_imgColumnNameModel.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_pathToCellProfilerModel.loadSettingsFrom(settings);
		m_pathToPipelineModel.loadSettingsFrom(settings);
		m_imgColumnNameModel.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
