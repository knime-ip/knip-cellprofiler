package org.knime.knip.cp.nodes.headless;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory class to provice {@link CPHeadLessNodeDialog} and
 * {@link CPHeadlessNodeModel}
 * 
 * @author Christian Dietz (University of Konstanz)
 * 
 */
public class CPHeadlessNodeFactory extends NodeFactory<CPHeadlessNodeModel> {

	@Override
	public CPHeadlessNodeModel createNodeModel() {
		return new CPHeadlessNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<CPHeadlessNodeModel> createNodeView(int viewIndex,
			CPHeadlessNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new CPHeadLessNodeDialog();
	}

}
