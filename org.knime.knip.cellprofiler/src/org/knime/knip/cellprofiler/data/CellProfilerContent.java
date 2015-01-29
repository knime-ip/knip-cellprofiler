package org.knime.knip.cellprofiler.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CellProfilerContent implements Serializable {

	private static final long serialVersionUID = 7735517345148180658L;

	private Map<String, CellProfilerSegmentation> m_segmentations = new HashMap<String, CellProfilerSegmentation>();

	public void addSegmentation(final String segmentationName,
			final CellProfilerSegmentation segmentation) {
		m_segmentations.put(segmentationName, segmentation);
	}

	public Set<String> getSegmentations() {
		return m_segmentations.keySet();
	}

	public CellProfilerSegmentation getSegmentation(
			final String segmantationName) {
		return m_segmentations.get(segmantationName);
	}

	@Override
	public int hashCode() {
		return m_segmentations.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CellProfilerContent)) {
			return false;
		}
		return m_segmentations
				.equals(((CellProfilerContent) obj).m_segmentations);
	}

	@Override
	public String toString() {
		return m_segmentations.toString();
	}

	public void save(final DataOutput output) throws IOException {
		output.writeInt(m_segmentations.size());
		for (Entry<String, CellProfilerSegmentation> entry : m_segmentations
				.entrySet()) {
			output.writeUTF(entry.getKey());
			entry.getValue().save(output);
		}
	}

	public static CellProfilerContent load(final DataInput input)
			throws IOException {
		CellProfilerContent content = new CellProfilerContent();
		int numberOfSegmentations = input.readInt();
		for (int i = 0; i < numberOfSegmentations; i++) {
			String segmentationName = input.readUTF();
			CellProfilerSegmentation segmentation = CellProfilerSegmentation
					.load(input);
			content.addSegmentation(segmentationName, segmentation);
		}
		return content;
	}

}
