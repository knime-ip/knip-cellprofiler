package org.knime.knip.cellprofiler.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CellProfilerContent implements Serializable {

	private static final long serialVersionUID = 7735517345148180658L;

	private final Map<String, CellProfilerMeasurementSet> measurements;

	private final String parentKey;

	public CellProfilerContent(final String parentKey) {
		this.parentKey = parentKey;
		this.measurements = new HashMap<String, CellProfilerMeasurementSet>();
	}

	public void addMeasurement(final String measurementName,
			final CellProfilerMeasurementSet measurement) {
		measurements.put(measurementName, measurement);
	}

	public Set<String> getMeasurements() {
		return measurements.keySet();
	}

	public CellProfilerMeasurementSet getMeasurements(final String measurement) {
		return measurements.get(measurement);
	}

	@Override
	public int hashCode() {
		return measurements.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CellProfilerContent)) {
			return false;
		}
		return measurements
				.equals(((CellProfilerContent) obj).measurements);
	}

	@Override
	public String toString() {
		return parentKey + "#" + measurements.toString();
	}
	
	public String getParentKey() {
		return parentKey;
	}
	
	public void save(final DataOutput output) throws IOException {
		output.writeUTF(parentKey);
		output.writeInt(measurements.size());
		for (Entry<String, CellProfilerMeasurementSet> entry : measurements
				.entrySet()) {
			output.writeUTF(entry.getKey());
			entry.getValue().save(output);
		}
	}

	public static CellProfilerContent load(final DataInput input)
			throws IOException {
		String parentKey = input.readUTF();
		CellProfilerContent content = new CellProfilerContent(parentKey);
		int numberOfMeasurements = input.readInt();
		for (int i = 0; i < numberOfMeasurements; i++) {
			String measurementNamenName = input.readUTF();
			CellProfilerMeasurementSet measurement = CellProfilerMeasurementSet
					.load(input);
			content.addMeasurement(measurementNamenName, measurement);
		}
		return content;
	}

}
