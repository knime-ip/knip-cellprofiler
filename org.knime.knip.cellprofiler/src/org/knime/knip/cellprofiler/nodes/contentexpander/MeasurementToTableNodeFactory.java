package org.knime.knip.cellprofiler.nodes.contentexpander;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * CellProfiler Pipeline Executor node factory.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class MeasurementToTableNodeFactory extends
		NodeFactory<MeasurementToTableNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MeasurementToTableNodeModel createNodeModel() {
		return new MeasurementToTableNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<MeasurementToTableNodeModel> createNodeView(int viewIndex,
			MeasurementToTableNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new MeasurementToTableNodeDialog();
	}

}
