package gui.singletons;

import java.util.ArrayList;
import java.util.HashMap;

import data.BarKey;
import data.Metric;
import data.MetricKey;
import dbio.QueryManager;

public class MetricSingleton {

	private static MetricSingleton instance = null;
	
	// For holding all the BarKeys
	private ArrayList<BarKey> barKeys = null;

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

	public void init(ArrayList<BarKey> barKeys) {
		this.barKeys = barKeys;
	}
	
	public void updateMetricSequenceHash() {
		this.metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys);
	}

	public HashMap<MetricKey, ArrayList<Metric>> getMetricSequenceHash() {
		return metricSequenceHash;
	}
}