package org.knime.knip.cellprofiler.data;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;

public class CellProfilerCell extends DataCell implements CellProfilerValue {

	public static final DataType TYPE = DataType
			.getType(CellProfilerCell.class);

	private static final long serialVersionUID = 6042209678689675852L;

	private static final CellProfilerCellSerializer SERIALIZER = new CellProfilerCellSerializer();

	private CellProfilerContent m_content;

	public CellProfilerCell(final CellProfilerContent cellProfilerContent) {
		if (cellProfilerContent == null) {
			throw new NullPointerException("Argument must not be null.");
		}
		m_content = cellProfilerContent;
	}

	public static final Class<? extends DataValue> getPreferredValueClass() {
		return CellProfilerValue.class;
	}

	public static final DataCellSerializer<CellProfilerCell> getCellSerializer() {
		return SERIALIZER;
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

	private static final class CellProfilerCellSerializer implements
			DataCellSerializer<CellProfilerCell> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void serialize(final CellProfilerCell cell,
				final DataCellDataOutput output) throws IOException {
			output.writeInt(0); // version
			CellProfilerContent cpc = cell.getCellProfilerContent();
			cpc.save(output);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public CellProfilerCell deserialize(final DataCellDataInput input)
				throws IOException {
			input.readInt(); // version
			CellProfilerContent cpc = null;
			try {
				cpc = CellProfilerContent.load(input);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Class not found");
			}
			return new CellProfilerCell(cpc);
		}

	}

}
