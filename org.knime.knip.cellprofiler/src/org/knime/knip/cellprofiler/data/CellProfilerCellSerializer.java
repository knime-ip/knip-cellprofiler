package org.knime.knip.cellprofiler.data;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

public class CellProfilerCellSerializer implements DataCellSerializer<CellProfilerCell> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final CellProfilerCell cell, final DataCellDataOutput output) throws IOException {
		output.writeInt(0); // version
		CellProfilerContent cpc = cell.getCellProfilerContent();
		cpc.save(output);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CellProfilerCell deserialize(final DataCellDataInput input) throws IOException {
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