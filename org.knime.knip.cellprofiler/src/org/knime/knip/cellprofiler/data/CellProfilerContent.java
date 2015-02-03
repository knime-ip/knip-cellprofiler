package org.knime.knip.cellprofiler.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class CellProfilerContent implements Serializable {

	private static final long serialVersionUID = 7735517345148180658L;

	private final String parentKey;

	private final CellProfilerMeasurement measurement;

	public CellProfilerContent(final String parentKey,
			final CellProfilerMeasurement measurement) {
		this.measurement = measurement;
		this.parentKey = parentKey;
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

	@Override
	public String toString() {
		return parentKey + "#" + measurement.toString();
	}

	public String getParentKey() {
		return parentKey;
	}

	public void save(final DataOutput output) throws IOException {
		output.writeUTF(parentKey);
		measurement.save(output);
	}

	public static CellProfilerContent load(final DataInput input)
			throws IOException {
		return new CellProfilerContent(input.readUTF(),
				CellProfilerMeasurement.load(input));
	}

}
