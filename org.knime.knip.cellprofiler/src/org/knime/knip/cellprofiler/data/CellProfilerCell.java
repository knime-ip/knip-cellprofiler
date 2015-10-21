package org.knime.knip.cellprofiler.data;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

/**
 * {@link DataCell} storing {@link CellProfilerContent}
 * 
 * @author Patrick Winter (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * 
 */
public class CellProfilerCell extends DataCell implements CellProfilerValue {

	public static final DataType TYPE = DataType.getType(CellProfilerCell.class);

	private static final long serialVersionUID = 6042209678689675852L;

	private CellProfilerContent m_content;

	public CellProfilerCell(final CellProfilerContent cellProfilerContent) {
		if (cellProfilerContent == null) {
			throw new NullPointerException("Argument must not be null.");
		}
		m_content = cellProfilerContent;
	}

	@Override
	public CellProfilerContent getCellProfilerContent() {
		return m_content;
	}

	@Override
	public String toString() {
		return m_content.toString();
	}

	@Override
	protected boolean equalsDataCell(DataCell dc) {
		if (dc == null || !(dc instanceof CellProfilerCell)) {
			return false;
		}
		return m_content.equals(((CellProfilerCell) dc).m_content);
	}

	@Override
	public int hashCode() {
		return m_content.hashCode();
	}

}
