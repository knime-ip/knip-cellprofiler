package org.knime.knip.cp.nodes.headless;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.knip.base.data.img.ImgPlusValue;

@SuppressWarnings("unchecked")
public class CPHeadLessNodeDialog extends DefaultNodeSettingsPane {

	{
		// path to cell profiler instance

		addDialogComponent(new DialogComponentFileChooser(
				CPHeadlessNodeModel.createPathToCPInstallationModel(),
				"Path to CellProfiler Installation", 0, true));

		addDialogComponent(new DialogComponentFileChooser(
				CPHeadlessNodeModel.createPathToCPPipelineModel(),
				"Path to CellProfiler Pipeline", 1, true));

		// column containing images
		addDialogComponent(new DialogComponentColumnNameSelection(
				CPHeadlessNodeModel.createImageColumnNameModel(),
				"Select Column Containing Images", 0, ImgPlusValue.class));
		
		addDialogComponent(new DialogComponentColumnNameSelection(
				CPHeadlessNodeModel.createMoreImageColumnNameModel(),
				"Select Column Containing More Images", 0, ImgPlusValue.class));

	}

}
