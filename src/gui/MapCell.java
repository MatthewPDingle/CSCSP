package gui;

import java.io.Serializable;
import java.util.HashMap;

public class MapCell implements Serializable {

	private HashMap<String, Float> metricValueHash = new HashMap<String, Float>();
	
	public MapCell(MapCell copy) {
		if (copy != null)
			this.metricValueHash = copy.metricValueHash;
	}
	
	public MapCell() {
		super();
	}
	
	public void addToMetricValueHash(String key, Float value) {
		this.metricValueHash.put(key, value);
	}
	
	public HashMap<String, Float> getMetricValueHash() {
		return metricValueHash;
	}

	public void setMetricValueHash(HashMap<String, Float> metricValueHash) {
		this.metricValueHash = metricValueHash;
	}
}