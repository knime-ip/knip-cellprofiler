package org.knime.knip.cellprofiler.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CellProfilerSegmentation implements Serializable {

	private static final long serialVersionUID = 1148694872280436207L;

	private Map<String, double[]> m_doubleFeatures = new HashMap<String, double[]>();

	private Map<String, float[]> m_floatFeatures = new HashMap<String, float[]>();

	private Map<String, int[]> m_integerFeatures = new HashMap<String, int[]>();

	private Map<String, String> m_stringFeatures = new HashMap<String, String>();

	public void addDoubleFeature(final String featureName,
			final double[] featureValues) {
		m_doubleFeatures.put(featureName, featureValues);
	}

	public Set<String> getDoubleFeatures() {
		return m_doubleFeatures.keySet();
	}

	public double[] getDoubleFeatureValues(final String featureName) {
		return m_doubleFeatures.get(featureName);
	}

	public void addFloatFeature(final String featureName,
			final float[] featureValues) {
		m_floatFeatures.put(featureName, featureValues);
	}

	public Set<String> getFloatFeatures() {
		return m_floatFeatures.keySet();
	}

	public float[] getFloatFeatueValues(final String featureName) {
		return m_floatFeatures.get(featureName);
	}

	public void addIntegerFeature(final String featureName,
			final int[] featureValues) {
		m_integerFeatures.put(featureName, featureValues);
	}

	public Set<String> getIntegerFeatures() {
		return m_integerFeatures.keySet();
	}

	public int[] getIntegerFeatureValues(final String featureName) {
		return m_integerFeatures.get(featureName);
	}

	public void addStringFeature(final String featureName,
			final String featureValue) {
		m_stringFeatures.put(featureName, featureValue);
	}

	public Set<String> getStringFeatures() {
		return m_stringFeatures.keySet();
	}

	public String getStringFeatureValue(final String featureName) {
		return m_stringFeatures.get(featureName);
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(m_doubleFeatures);
		hcb.append(m_floatFeatures);
		hcb.append(m_integerFeatures);
		hcb.append(m_stringFeatures);
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CellProfilerSegmentation)) {
			return false;
		}
		final CellProfilerSegmentation seg = (CellProfilerSegmentation) obj;
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(m_doubleFeatures, seg.m_doubleFeatures);
		eb.append(m_floatFeatures, seg.m_floatFeatures);
		eb.append(m_integerFeatures, seg.m_integerFeatures);
		eb.append(m_stringFeatures, seg.m_stringFeatures);
		return eb.isEquals();
	}

	@Override
	public String toString() {
		return m_doubleFeatures + "\n" + m_floatFeatures + "\n"
				+ m_integerFeatures + "\n" + m_stringFeatures;
	}

	public void save(final DataOutput output) throws IOException {
		output.writeInt(m_doubleFeatures.size());
		for (Entry<String, double[]> entry : m_doubleFeatures.entrySet()) {
			output.writeUTF(entry.getKey());
			double[] values = entry.getValue();
			output.writeInt(values.length);
			for (double value : values) {
				output.writeDouble(value);
			}
		}
		output.writeInt(m_floatFeatures.size());
		for (Entry<String, float[]> entry : m_floatFeatures.entrySet()) {
			output.writeUTF(entry.getKey());
			float[] values = entry.getValue();
			output.writeInt(values.length);
			for (float value : values) {
				output.writeFloat(value);
			}
		}
		output.writeInt(m_integerFeatures.size());
		for (Entry<String, int[]> entry : m_integerFeatures.entrySet()) {
			output.writeUTF(entry.getKey());
			int[] values = entry.getValue();
			output.writeInt(values.length);
			for (int value : values) {
				output.writeInt(value);
			}
		}
		output.writeInt(m_stringFeatures.size());
		for (Entry<String, String> entry : m_stringFeatures.entrySet()) {
			output.writeUTF(entry.getKey());
			output.writeUTF(entry.getValue());
		}
	}

	public static CellProfilerSegmentation load(final DataInput input)
			throws IOException {
		CellProfilerSegmentation segmentation = new CellProfilerSegmentation();
		int numberOfDoubleFeatures = input.readInt();
		for (int i = 0; i < numberOfDoubleFeatures; i++) {
			String featureName = input.readUTF();
			double[] values = new double[input.readInt()];
			for (int j = 0; j < values.length; j++) {
				values[i] = input.readDouble();
			}
			segmentation.addDoubleFeature(featureName, values);
		}
		int numberOfFloatFeatures = input.readInt();
		for (int i = 0; i < numberOfFloatFeatures; i++) {
			String featureName = input.readUTF();
			float[] values = new float[input.readInt()];
			for (int j = 0; j < values.length; j++) {
				values[i] = input.readFloat();
			}
			segmentation.addFloatFeature(featureName, values);
		}
		int numberOfIntegerFeatures = input.readInt();
		for (int i = 0; i < numberOfIntegerFeatures; i++) {
			String featureName = input.readUTF();
			int[] values = new int[input.readInt()];
			for (int j = 0; j < values.length; j++) {
				values[i] = input.readInt();
			}
			segmentation.addIntegerFeature(featureName, values);
		}
		int numberOfStringFeatures = input.readInt();
		for (int i = 0; i < numberOfStringFeatures; i++) {
			String featureName = input.readUTF();
			String value = input.readUTF();
			segmentation.addStringFeature(featureName, value);
		}
		return segmentation;
	}

}
