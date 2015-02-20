package org.knime.knip.cellprofiler.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.knip.base.KNIPConstants;

/**
 * Table representing calculations from a CellProfiler Pipeline
 * 
 * @author Patrick Winter (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 *
 */
public class CellProfilerMeasurementTable implements Serializable {

	protected enum InternalFormat {
		DOUBLE(DoubleCell.TYPE), INT(IntCell.TYPE), STRING(StringCell.TYPE);

		private DataType type;

		private InternalFormat(DataType type) {
			this.type = type;
		}

		public DataType getType() {
			return type;
		}
	}

	private static final long serialVersionUID = 1148694872280436208L;

	private final List<DataCell[]> dataAsColumns;

	private final List<Pair<String, InternalFormat>> outSpec;

	private final String parentKey;

	private int numRows = -1;

	/**
	 * @param parentKey
	 *            key of parent object
	 */
	public CellProfilerMeasurementTable(final String parentKey) {
		this.parentKey = parentKey;
		this.outSpec = new ArrayList<Pair<String, InternalFormat>>();
		this.dataAsColumns = new ArrayList<DataCell[]>();
	}

	/**
	 * @param parentKey
	 *            key of parent object
	 */
	protected CellProfilerMeasurementTable(final String parentKey,
			final int numRows, final int numCols,
			final List<Pair<String, InternalFormat>> outSpec,
			final List<DataCell[]> dataAsColumns) {
		this.parentKey = parentKey;
		this.numRows = numRows;
		this.outSpec = outSpec;
		this.dataAsColumns = dataAsColumns;
	}

	public DataTableSpec getSpec() {
		final DataColumnSpec[] spec = new DataColumnSpec[outSpec.size()];

		int k = 0;
		for (final Pair<String, InternalFormat> value : outSpec) {
			spec[k++] = new DataColumnSpecCreator(value.getA(), value.getB()
					.getType()).createSpec();
		}

		return new DataTableSpec(spec);
	}

	public void addRows(final BufferedDataContainer container) {

		for (int r = 0; r < numRows; r++) {

			final DataCell[] cells = new DataCell[outSpec.size()];

			for (int i = 0; i < cells.length; i++) {
				cells[i] = dataAsColumns.get(i)[r];
			}

			container.addRowToTable(new DefaultRow(parentKey
					+ KNIPConstants.IMGID_LABEL_DELIMITER + r, cells));

		}
	}

	/**
	 * @return number of rows in this measurement table
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * @return key of the parent object
	 */
	public String getParentKey() {
		return parentKey;
	}

	/**
	 * Tries to determine the number of rows in this table. fails if the
	 * provided columns have different sizes
	 * 
	 * @param
	 */
	private void trySetNumRows(int numRows) {
		if (this.numRows == -1)
			this.numRows = numRows;
		else if (this.numRows != numRows)
			throw new IllegalStateException(
					"Inconsistent measurements. This shouldn't happen! Please contact us: http://tech.knime.org/forum");

	}

	private void addToSpec(final String featureName, final InternalFormat type) {
		outSpec.add(new ValuePair<String, InternalFormat>(featureName, type));
	}

	public void addDoubleFeature(final String featureName,
			final double[] featureValues) {
		trySetNumRows(featureValues.length);
		final DoubleCell[] cells = new DoubleCell[featureValues.length];

		for (int i = 0; i < featureValues.length; i++) {
			cells[i] = new DoubleCell(featureValues[i]);
		}
		this.dataAsColumns.add(cells);

		addToSpec(featureName, InternalFormat.DOUBLE);
	}

	public void addFloatFeature(final String featureName,
			final float[] featureValues) {
		trySetNumRows(featureValues.length);
		final DoubleCell[] cells = new DoubleCell[featureValues.length];

		for (int i = 0; i < featureValues.length; i++) {
			cells[i] = new DoubleCell(featureValues[i]);
		}
		this.dataAsColumns.add(cells);

		addToSpec(featureName, InternalFormat.DOUBLE);
	}

	public void addIntegerFeature(final String featureName,
			final int[] featureValues) {
		trySetNumRows(featureValues.length);
		final IntCell[] cells = new IntCell[featureValues.length];

		for (int i = 0; i < featureValues.length; i++) {
			cells[i] = new IntCell(featureValues[i]);
		}
		this.dataAsColumns.add(cells);

		addToSpec(featureName, InternalFormat.INT);
	}

	public void addStringFeature(final String featureName,
			final String featureValue) {
		trySetNumRows(1);
		this.dataAsColumns
				.add(new StringCell[] { new StringCell(featureValue) });

		addToSpec(featureName, InternalFormat.STRING);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numRows;
		result = prime * result + ((outSpec == null) ? 0 : outSpec.hashCode());
		result = prime * result
				+ ((parentKey == null) ? 0 : parentKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CellProfilerMeasurementTable other = (CellProfilerMeasurementTable) obj;
		if (numRows != other.numRows)
			return false;
		if (outSpec == null) {
			if (other.outSpec != null)
				return false;
		} else if (!outSpec.equals(other.outSpec))
			return false;
		if (parentKey == null) {
			if (other.parentKey != null)
				return false;
		} else if (!parentKey.equals(other.parentKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CellProfiler Content: \n" + "Number of Rows: [" + numRows
				+ "] \n" + "Number of Columns: [" + dataAsColumns.size() + "]";
	}

	public void save(final DataCellDataOutput output) throws IOException {
		output.writeUTF(parentKey);
		output.writeInt(dataAsColumns.size());
		output.writeInt(numRows);

		for (final Pair<String, InternalFormat> spec : outSpec) {
			output.writeUTF(spec.getA());
			output.writeInt(spec.getB().ordinal());
		}

		for (final DataCell[] cells : dataAsColumns) {
			for (final DataCell cell : cells) {
				output.writeDataCell(cell);
			}
		}
	}

	public static CellProfilerMeasurementTable load(
			final DataCellDataInput input) throws IOException,
			ClassNotFoundException {
		final String parentKey = input.readUTF();
		final int numColumns = input.readInt();
		final int numRows = input.readInt();

		final List<DataCell[]> dataAsColumns = new ArrayList<>();
		final List<Pair<String, InternalFormat>> spec = new ArrayList<>();

		for (int i = 0; i < numColumns; i++) {
			spec.add(new ValuePair<String, CellProfilerMeasurementTable.InternalFormat>(
					input.readUTF(), InternalFormat.values()[input.readInt()]));
		}

		for (int i = 0; i < numColumns; i++) {
			final DataCell[] column = new DataCell[numRows];

			for (int j = 0; j < column.length; j++) {
				column[j] = (DataCell) input.readDataCell();
			}
			dataAsColumns.add(column);
		}

		return new CellProfilerMeasurementTable(parentKey, numRows, numColumns,
				spec, dataAsColumns);
	}

}
