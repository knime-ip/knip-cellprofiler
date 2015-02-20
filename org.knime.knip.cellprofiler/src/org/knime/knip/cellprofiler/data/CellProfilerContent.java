package org.knime.knip.cellprofiler.data;

import java.io.IOException;
import java.io.Serializable;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;

/**
 * Stores results from execution of CellProfiler Pipeline.
 * 
 * @author Patrick Winter (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 *
 */
public class CellProfilerContent implements Serializable {

	private static final long serialVersionUID = 7735517345148180658L;

	private final String parentKey;

	private final CellProfilerMeasurementTable measurement;

	private boolean isImageMeasurement;

	public CellProfilerContent(final String parentKey,
			final CellProfilerMeasurementTable measurement,
			boolean isImageMeasurements) {
		this.measurement = measurement;
		this.parentKey = parentKey;
		this.isImageMeasurement = isImageMeasurements;
	}

	@Override
	public int hashCode() {
		return measurement.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CellProfilerContent)) {
			return false;
		}
		return measurement.equals(((CellProfilerContent) obj).measurement);
	}

	/**
	 * @return measurements for this content
	 */
	public CellProfilerMeasurementTable getMeasurement() {
		return measurement;
	}

	@Override
	public String toString() {
		return parentKey + "#" + measurement.toString();
	}

	public String getParentKey() {
		return parentKey;
	}

	public boolean isImageMeasurement() {
		return isImageMeasurement;
	}

	public void save(final DataCellDataOutput output) throws IOException {
		output.writeUTF(parentKey);
		measurement.save(output);
		output.writeBoolean(isImageMeasurement);
	}

	public static CellProfilerContent load(final DataCellDataInput input)
			throws IOException, ClassNotFoundException {
		return new CellProfilerContent(input.readUTF(),
				CellProfilerMeasurementTable.load(input), input.readBoolean());
	}

}
