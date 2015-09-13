package gui.singletons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import data.BarKey;
import data.Metric;
import data.MetricKey;
import dbio.QueryManager;

public class MetricSingleton {

	private static MetricSingleton instance = null;
	
	// For holding all the BarKeys
	private ArrayList<BarKey> barKeys = null;
	
	// List of metrics I need updated
	ArrayList<String> neededMetrics = null;

	// For holding all the Metric Sequences
	private HashMap<MetricKey, ArrayList<Metric>> metricSequenceHash = new HashMap<MetricKey, ArrayList<Metric>>();
	
	protected MetricSingleton() {
	}
	
	public static MetricSingleton getInstance() {
		if (instance == null) {
			instance = new MetricSingleton();
		}
		return instance;
	}

	public ArrayList<BarKey> getBarKeys() {
		return barKeys;
	}

	public void init(ArrayList<BarKey> barKeys, ArrayList<String> neededMetrics) {
		this.barKeys = barKeys;
		this.neededMetrics = neededMetrics;
		this.metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys, neededMetrics);
	}
	
	@Deprecated
	public void updateMetricSequenceHash() {
		this.metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys, neededMetrics);
	}

	public HashMap<MetricKey, ArrayList<Metric>> getMetricSequenceHash() {
		return metricSequenceHash;
	}
	
	public synchronized Map.Entry<MetricKey, ArrayList<Metric>> popSingleMetricSequence() {
		for (Iterator<Map.Entry<MetricKey, ArrayList<Metric>>> it = metricSequenceHash.entrySet().iterator(); it.hasNext();) {
			Map.Entry<MetricKey, ArrayList<Metric>> entry = it.next();
			it.remove();
			return entry;
		}
		return null;
	}
}