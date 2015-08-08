package gui.singletons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import data.BarKey;
import data.Metric;
import data.MetricKey;
import dbio.QueryManager;

public class MetricSingleton {

	private static MetricSingleton instance = null;
	
	// For holding all the BarKeys
	private ArrayList<BarKey> barKeys = null;

	// For holding all the Metric Sequences
	private HashMap<MetricKey, LinkedList<Metric>> metricSequenceHash = new HashMap<MetricKey, LinkedList<Metric>>();
	
	// For holding all the variables needed to quickly calculate metrics without recomputing a long chain
	private HashMap<MetricKey, HashMap<String, Object>> metricCalcEssentialsHash = new HashMap<MetricKey, HashMap<String, Object>>();

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

	public HashMap<MetricKey, LinkedList<Metric>> getMetricSequenceHash() {
		return metricSequenceHash;
	}

	public HashMap<MetricKey, HashMap<String, Object>> getMetricCalcEssentialsHash() {
		return metricCalcEssentialsHash;
	}
}