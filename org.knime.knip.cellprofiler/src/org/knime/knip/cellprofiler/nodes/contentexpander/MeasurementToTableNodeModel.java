package org.knime.knip.cellprofiler.nodes.contentexpander;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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
import org.knime.knip.cellprofiler.data.CellProfilerContent;
import org.knime.knip.cellprofiler.data.CellProfilerValue;

/**
 * CellProfiler Pipeline Executor node model.
 * 
 * @author Christian Dietz, University of Konstanz
 * @author Patrick Winter, University of Konstanz
 */
public class MeasurementToTableNodeModel extends NodeModel {

	/**
	 * 
	 * SettingsModelString to store selected column
	 * 
	 * @return
	 */
	static SettingsModelString createSettingsModelColumnSelection() {
		return new SettingsModelString("measurement_column", "");
	}

	private final SettingsModelString measurementColumnModel;

	protected MeasurementToTableNodeModel() {
		super(1, 1);

		measurementColumnModel = createSettingsModelColumnSelection();
	}

	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		int measurementColumnIndex = inData[0].getDataTableSpec()
				.findColumnIndex(measurementColumnModel.getStringValue());
		if (measurementColumnIndex == -1) {
			measurementColumnIndex = autoGuessColumnIdx(inData[0]
					.getDataTableSpec());
		}

		BufferedDataContainer outData = null;

		for (final DataRow row : inData[0]) {
			final CellProfilerContent content = ((CellProfilerValue) row
					.getCell(measurementColumnIndex)).getCellProfilerContent();
			if (outData == null) {
				outData = exec.createDataContainer(content.getMeasurement()
						.getSpec());
			}

			content.getMeasurement().addRows(outData);
		}

		outData.close();
		return new BufferedDataTable[] { outData.getTable() };
	}

	private int autoGuessColumnIdx(final DataTableSpec tableSpec)
			throws InvalidSettingsException {

		int i = 0;
		for (final DataColumnSpec spec : tableSpec) {
			if (spec.getType().isCompatible(CellProfilerValue.class)) {
				return i;
			}
			i++;
		}

		throw new InvalidSettingsException(
				"No compatible column found: CellProfilerContent.");
	}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// check if there exists any column
		autoGuessColumnIdx(inSpecs[0]);

		return new DataTableSpec[] { null };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		measurementColumnModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		measurementColumnModel.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		measurementColumnModel.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// Empty
	}

}
