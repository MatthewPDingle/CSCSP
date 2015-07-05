package metrics;

import java.util.ArrayList;
import java.util.LinkedList;

import constants.Constants;

public class MetricSequenceDataSingleton {

	private static MetricSequenceDataSingleton instance = null;
	
	private ArrayList<LinkedList<Metric>> metricSequences = new ArrayList<LinkedList<Metric>>();
	private LinkedList<String> metrics = new LinkedList<String>();
	
	
	protected MetricSequenceDataSingleton() {
	}
	
	public static MetricSequenceDataSingleton getInstance() {
		if (instance == null) {
			instance = new MetricSequenceDataSingleton();
		}
		return instance;
	}
	
	public synchronized void loadMetricList() {
		metrics.addAll(Constants.METRICS);
	}
	
	public synchronized String popMetric() {
		try {
			if (metrics != null && metrics.size() > 0) {
				return metrics.remove(0);
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized LinkedList<Metric> popMetricSequence() {
		try {
			if (metricSequences != null && metricSequences.size() > 0) {
				return metricSequences.remove(0);
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized void addMetricSequence(LinkedList<Metric> metricSequence) {
		try {
			if (metricSequences != null) {
				metricSequences.add(metricSequence);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}