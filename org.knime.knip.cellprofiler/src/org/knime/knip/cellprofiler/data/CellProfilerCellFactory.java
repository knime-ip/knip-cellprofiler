package org.knime.knip.cellprofiler.data;

import org.knime.core.data.DataCellFactory;
import org.knime.core.data.DataType;

public class CellProfilerCellFactory implements DataCellFactory {

	@Override
	public DataType getDataType() {
		return CellProfilerCell.TYPE;
	}

}
