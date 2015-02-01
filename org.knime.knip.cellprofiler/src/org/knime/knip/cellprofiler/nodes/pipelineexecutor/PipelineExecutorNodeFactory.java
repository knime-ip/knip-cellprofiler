package org.knime.knip.cellprofiler.nodes.pipelineexecutor;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * CellProfiler Pipeline Executor node factory.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class PipelineExecutorNodeFactory extends
		NodeFactory<PipelineExecutorNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PipelineExecutorNodeModel createNodeModel() {
		return new PipelineExecutorNodeModel();
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
	public NodeView<PipelineExecutorNodeModel> createNodeView(int viewIndex,
			PipelineExecutorNodeModel nodeModel) {
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
		return new PipelineExecutorNodeDialog();
	}

}
