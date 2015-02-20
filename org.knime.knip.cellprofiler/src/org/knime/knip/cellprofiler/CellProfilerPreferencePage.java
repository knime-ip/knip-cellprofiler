/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   Sep 25, 2014 (Patrick Winter): created
 */
package org.knime.knip.cellprofiler;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knime.core.node.NodeLogger;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Preference page for configurations.
 * 
 * @author Patrick Winter, University of Konstanz
 * @author Christian Dietz, University of Konstanz
 */
public class CellProfilerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String DEFAULT_PATH = doAutoGuessCellProfilerPath();

	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(CellProfilerPreferencePage.class);

	private ScrolledComposite m_sc;

	private Composite m_container;

	private FileFieldEditor m_pathEditor;

	/**
	 * Gets the currently configured path.
	 * 
	 * @return Path to the CellProfiler module
	 */
	public static String getPath() {
		return Platform.getPreferencesService().getString(
				"org.knime.knip.cellprofiler", "path", DEFAULT_PATH, null);
	}

	private static String doAutoGuessCellProfilerPath() {
		final String OS = System.getProperty("os.name", "generic");
		if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
			return "/Applications/CellProfiler.app";
		} else if (OS.indexOf("win") >= 0) {
			return "C:\\Program Files\\CellProfiler";
		} else if (OS.indexOf("nux") >= 0) {
			return "/usr/bin/cellprofiler";
		} else {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench) {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performOk() {
		setPath(m_pathEditor.getStringValue());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performApply() {
		setPath(m_pathEditor.getStringValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performDefaults() {
		m_pathEditor.setStringValue(DEFAULT_PATH);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createContents(Composite parent) {
		m_sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		m_container = new Composite(m_sc, SWT.NONE);
		m_container.setLayout(new GridLayout());
		m_pathEditor = new FileFieldEditor("org.knime.knip.cellprofiler",
				"Path to CellProfiler module", m_container);
		m_pathEditor.setStringValue(getPath());
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.verticalIndent = 20;
		m_sc.setContent(m_container);
		m_sc.setExpandHorizontal(true);
		m_sc.setExpandVertical(true);
		return m_sc;
	}

	/**
	 * Saves the given path.
	 * 
	 * @param path
	 *            Path to the CellProfiler module
	 */
	private void setPath(final String path) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode("org.knime.knip.cellprofiler");
		prefs.put("path", path);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			LOGGER.error("Could not save preferences: " + e.getMessage(), e);
		}
	}

}
