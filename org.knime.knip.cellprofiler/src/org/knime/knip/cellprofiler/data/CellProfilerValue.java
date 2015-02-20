package org.knime.knip.cellprofiler.data;

import javax.swing.Icon;

import org.knime.core.data.DataValue;
import org.knime.core.data.ExtensibleUtilityFactory;

/**
 * {@link DataValue} storing {@link CellProfilerContent}
 * 
 * @author Patrick Winter (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * 
 */
public interface CellProfilerValue extends DataValue {

	static final UtilityFactory UTILITY = new CellProfilerUtilityFactory();

	CellProfilerContent getCellProfilerContent();

	static class CellProfilerUtilityFactory extends ExtensibleUtilityFactory {

		private static final Icon ICON = loadIcon(CellProfilerValue.class,
				"cellprofiler.png");

		protected CellProfilerUtilityFactory() {
			super(CellProfilerValue.class);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Icon getIcon() {
			return ICON;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getName() {
			return "CellProfiler Measurements";
		}
	}

}
