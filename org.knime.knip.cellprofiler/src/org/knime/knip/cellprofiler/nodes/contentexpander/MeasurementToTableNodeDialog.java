package org.knime.knip.cellprofiler.nodes.contentexpander;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.knip.cellprofiler.data.CellProfilerValue;

/**
 * CellProfiler Pipeline Executor node dialog.
 * 
 * @author Christian Dietz, University of Konstanz
 * @author Patrick Winter, University of Konstanz
 */
public class MeasurementToTableNodeDialog extends DefaultNodeSettingsPane {

	@SuppressWarnings("unchecked")
	public MeasurementToTableNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				MeasurementToTableNodeModel
						.createSettingsModelColumnSelection(),
				"Column with CellProfiler Measurement", 0,
				CellProfilerValue.class));
	}
}
