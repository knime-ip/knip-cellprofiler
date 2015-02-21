package org.knime.knip.cellprofiler.nodes.pipelineexecutor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.cellprofiler.knimebridge.PipelineException;
import org.cellprofiler.knimebridge.ProtocolException;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.FilesHistoryPanel;
import org.knime.core.util.pathresolve.ResolverUtil;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.cellprofiler.CellProfilerInstance;
import org.zeromq.ZMQException;

/**
 * CellProfiler Pipeline Executor node dialog.
 * 
 * @author Patrick Winter, University of Konstanz
 * @author Christian Dietz, University of Konstanz
 */
public class PipelineExecutorNodeDialog extends NodeDialogPane {

	private static final String WORKFLOW_DIR = "knime://knime.workflow";

	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(PipelineExecutorNodeDialog.class);

	private JPanel m_panel = new JPanel(new GridBagLayout());

	private FilesHistoryPanel m_pipelineFile = new FilesHistoryPanel(
			"pipelineFile", "cppipe");

	private List<ColumnSelectionComboxBox> m_imageColumns = new ArrayList<ColumnSelectionComboxBox>();

	private DataTableSpec m_spec = new DataTableSpec(new DataColumnSpec[0]);

	private String[] m_inputParameters = new String[0];

	private CellProfilerInstance m_cellProfiler;

	private JButton m_update = new JButton("Update");

	private List<String> m_objectNames;

	/**
	 * Constructor.
	 */
	public PipelineExecutorNodeDialog() {
		m_update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					pipelineFileChanged();
				} catch (InvalidPathException | MalformedURLException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		m_pipelineFile.setBorder(BorderFactory
				.createTitledBorder("Pipeline file"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		m_panel.add(m_pipelineFile, gbc);
		gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE;
		m_panel.add(m_update, gbc);
		// We put the panel into a border layout panel to keep it from being
		// centered
		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(m_panel, BorderLayout.NORTH);
		addTab("Config", outerPanel);
	}

	private static String initWorkflowPath() {
		try {
			return ResolverUtil.resolveURItoLocalFile(new URI(WORKFLOW_DIR))
					.getCanonicalPath();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates the column selection to fit to the newly selected pipeline file.
	 * 
	 * @throws MalformedURLException
	 * @throws InvalidPathException
	 */
	private void pipelineFileChanged() throws InvalidPathException,
			MalformedURLException {
		// We get the number of columns from a CellProfiler instance, if
		// anything goes wrong we default to 0
		String[] inputParameters = new String[0];

		// TODO is there an more elegant way?
		final String path = PipelineExecutorNodeDialog
				.resolveToLocalPath(m_pipelineFile.getSelectedFile());

		if (!path.isEmpty()) {
			if (new File(path).exists()) {
				try {
					if (m_cellProfiler == null) {
						initCellProfiler();
					}
					m_cellProfiler.loadPipeline(path);
					inputParameters = m_cellProfiler.getInputParameters();
					m_objectNames = m_cellProfiler.getObjectNames();
				} catch (ZMQException | PipelineException | ProtocolException
						| IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
		updateColumnSelection(inputParameters);
	}

	/**
	 * Updates the column selection panel.
	 * 
	 * @param inputParameters
	 *            Parameter names of the expected input images.
	 */
	public void updateColumnSelection(String[] inputParameters) {
		// Remove old column selectors
		while (m_imageColumns.size() > 0) {
			m_panel.remove(m_imageColumns.get(0));
			m_imageColumns.remove(0);
		}
		if (inputParameters.length > 0) {
			// Add new column selectors
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.gridwidth = 1;
			gbc.gridx = 0;
			gbc.gridy = m_panel.getComponentCount();
			for (int i = 0; i < inputParameters.length; i++) {
				@SuppressWarnings("unchecked")
				ColumnSelectionComboxBox imageColumn = new ColumnSelectionComboxBox(
						inputParameters[i], ImgPlusValue.class);
				try {
					imageColumn.update(m_spec, null);
				} catch (NotConfigurableException e) {
					LOGGER.error(e.getMessage(), e);
				}
				m_panel.add(imageColumn, gbc);
				m_imageColumns.add(imageColumn);
				gbc.gridy++;
			}
		}
		m_inputParameters = inputParameters;
		// Repaint
		m_panel.revalidate();
		m_panel.repaint();
	}

	private void initCellProfiler() {
		try {
			m_cellProfiler = new CellProfilerInstance();
		} catch (ZMQException | IOException | ProtocolException
				| URISyntaxException | PipelineException e1) {
			LOGGER.error(e1.getMessage(), e1);
		}
	}

	@Override
	public void onClose() {
		if (m_cellProfiler != null) {
			m_cellProfiler.close();
			m_cellProfiler = null;
		}
		super.onClose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		m_spec = specs[0];
		PipelineExecutorNodeConfig config = new PipelineExecutorNodeConfig();
		config.loadConfigInDialog(settings);
		// We apply the previous column selection before the pipeline file is
		// loaded (which might have changed)
		String[] imageColumns = config.getImageColumns();
		// updateColumnSelection will automatically set m_inputParameters
		updateColumnSelection(config.getInputParameters());
		for (int i = 0; i < imageColumns.length; i++) {
			m_imageColumns.get(i).setSelectedColumn(imageColumns[i]);
		}
		m_pipelineFile.setSelectedFile(config.getPipelineFile());
		m_objectNames = config.getObjectNames();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		PipelineExecutorNodeConfig config = new PipelineExecutorNodeConfig();
		config.setPipelineFile(m_pipelineFile.getSelectedFile().replace(initWorkflowPath(), "knime://knime.workflow"));
		config.setInputParameters(m_inputParameters);
		config.setObjectNames(m_objectNames);
		String[] imageColumns = new String[m_imageColumns.size()];
		for (int i = 0; i < imageColumns.length; i++) {
			imageColumns[i] = m_imageColumns.get(i).getSelectedColumn();
		}
		config.setImageColumns(imageColumns);
		config.saveConfig(settings);
	}

	public static String resolveToLocalPath(String pipelineFile) {
		return pipelineFile.replace(WORKFLOW_DIR, initWorkflowPath());
	}

}
