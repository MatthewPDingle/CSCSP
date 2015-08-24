package ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import dbio.QueryManager;
import search.GeneticSearcher;

public class ARFF {

	public static void main(String[] args) {

		// Training Data Timeframe
		Calendar trainStart = Calendar.getInstance();
		trainStart.set(Calendar.YEAR, 2014); 
		trainStart.set(Calendar.MONTH, 0); 
		trainStart.set(Calendar.DAY_OF_MONTH, 1);
		trainStart.set(Calendar.HOUR_OF_DAY, 0);
		trainStart.set(Calendar.MINUTE, 0);
		trainStart.set(Calendar.SECOND, 0);
		trainStart.set(Calendar.MILLISECOND, 0);
		
		Calendar trainEnd = Calendar.getInstance();
		trainEnd.set(Calendar.YEAR, 2015);
		trainEnd.set(Calendar.MONTH, 2);
		trainEnd.set(Calendar.DAY_OF_MONTH, 31); 
		trainEnd.set(Calendar.HOUR_OF_DAY, 0);
		trainEnd.set(Calendar.MINUTE, 0);
		trainEnd.set(Calendar.SECOND, 0);
		trainEnd.set(Calendar.MILLISECOND, 0);
		
		// Test Data Timeframe
		Calendar testStart = Calendar.getInstance();
		testStart.set(Calendar.YEAR, 2015); 
		testStart.set(Calendar.MONTH, 3); 
		testStart.set(Calendar.DAY_OF_MONTH, 1);
		testStart.set(Calendar.HOUR_OF_DAY, 0);
		testStart.set(Calendar.MINUTE, 0);
		testStart.set(Calendar.SECOND, 0);
		testStart.set(Calendar.MILLISECOND, 0);
		
		Calendar testEnd = Calendar.getInstance();
		testEnd.set(Calendar.YEAR, 2015);
		testEnd.set(Calendar.MONTH, 5);
		testEnd.set(Calendar.DAY_OF_MONTH, 3);
		testEnd.set(Calendar.HOUR_OF_DAY, 0);
		testEnd.set(Calendar.MINUTE, 0);
		testEnd.set(Calendar.SECOND, 0);
		testEnd.set(Calendar.MILLISECOND, 0);
		
		ArrayList<String> metricNames = new ArrayList<String>();
		metricNames.add("consecutivedowns");
		metricNames.add("pricebolls50");
		metricNames.add("williamsr50");
		metricNames.add("psar");
		metricNames.add("mfi16");
		metricNames.add("stochasticdrsi20_5_5");
		metricNames.add("williamsr10");
		metricNames.add("rsi5");
		metricNames.add("aroonoscillator50");
		metricNames.add("atr20");
		metricNames.add("cci10");
		metricNames.add("volumebolls50");
		metricNames.add("ultimateoscillator4_10_25");
		metricNames.add("stochasticd14_3_3");
		metricNames.add("macd12_26_9");
		
		BarKey bk = new BarKey("okcoinBTCCNY", BAR_SIZE.BAR_15M);
		
		System.out.print("Creating MetricDiscreteValueHash...");
		int[] percentiles = {1, 2, 5, 10, 20, 35, 50, 65, 80, 90, 95, 98, 99};
		HashMap<String, ArrayList<Float>> metricDiscreteValueHash = GeneticSearcher.loadBullMetricDiscreteValueLists(percentiles, metricNames);
		System.out.println("Complete.");
		
		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, 1.2f, .2f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("RandomForest", 	null, trainStart, trainEnd, testStart, testEnd, 1.2f, .2f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("SimpleLogistic", 	null, trainStart, trainEnd, testStart, testEnd, 1.2f, .2f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("Bagging",		 	null, trainStart, trainEnd, testStart, testEnd, 1.2f, .2f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("J48", 				null, trainStart, trainEnd, testStart, testEnd, 1.2f, .2f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("BayesNet", 		null, trainStart, trainEnd, testStart, testEnd, 1.2f, .2f, 48, bk, metricNames, metricDiscreteValueHash);
		
		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, 2.0f, .5f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("RandomForest", 	null, trainStart, trainEnd, testStart, testEnd, 2.0f, .5f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("SimpleLogistic", 	null, trainStart, trainEnd, testStart, testEnd, 2.0f, .5f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("Bagging", 			null, trainStart, trainEnd, testStart, testEnd, 2.0f, .5f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("J48", 				null, trainStart, trainEnd, testStart, testEnd, 2.0f, .5f, 48, bk, metricNames, metricDiscreteValueHash);
		Modelling.buildAndEvaluateModel("BayesNet", 		null, trainStart, trainEnd, testStart, testEnd, 2.0f, .5f, 48, bk, metricNames, metricDiscreteValueHash);
	}

	/**
	 * 
	 * @param periodStart
	 * @param periodEnd
	 * @param targetGain - %
	 * @param minLoss - %
	 * @param numPeriods
	 * @param bk
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * 
	 * Returns a list that looks exactly like the @data section of a WEKA .arff file
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffData(Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, int numPeriods, BarKey bk, ArrayList<String> metricNames, HashMap<String, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				float close = (float)record.get("close");
				float hour = (int)record.get("hour");
				nextXCloses.add(close);
				if (nextXCloses.size() > numPeriods) {
					nextXCloses.remove(0);
				}
		
				boolean targetGainOK = false;
				int targetGainIndex = findTargetGainIndex(nextXCloses, close, targetGain);

				boolean minLossOK = false;
				if (targetGainIndex != -1) {
					targetGainOK = true;
					float minClose = findMin(nextXCloses, targetGainIndex);
					if (minClose >= close * (100f - minLoss) / 100f) {
						minLossOK = true;
					}
				}

				// Non-Metric Values
				String refrencePart = close + ", " + hour + ", ";

				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(metricName);
					if (bucketCutoffValues != null) {
						float metricValue = (float)record.get(metricName);
						
						int bucketNum = 0;
						for (int a = bucketCutoffValues.size() - 1; a >= 0; a--) {
							float bucketCutoffValue = bucketCutoffValues.get(a);
							if (metricValue < bucketCutoffValue) {
								break;
							}
							bucketNum++;
						}
						
						metricPart += ("BUCKET" + bucketNum + ", ");
//						metricPart += metricValue + ", ";
					}
				}
				
				// Class
				String classPart = "";
				if (minLossOK && targetGainOK) {
					classPart = "Buy";
				}
				else {
					classPart = "No";
				}
				
				if (!metricPart.equals("")) {
					String recordLine = refrencePart + metricPart + classPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					valuesList.add(valueList);
				}
			}
			
			return valuesList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<ArrayList<Object>> createUnlabeledWekaArffData(Calendar periodStart, Calendar periodEnd, BarKey bk, ArrayList<String> metricNames, HashMap<String, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				// Non-Metric Values
				float close = (float)record.get("close");
				float hour = (int)record.get("hour");
				String refrencePart = close + ", " + hour + ", ";
				
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(metricName);
					if (bucketCutoffValues != null) {
						float metricValue = (float)record.get(metricName);
						
						int bucketNum = 0;
						for (int a = bucketCutoffValues.size() - 1; a >= 0; a--) {
							float bucketCutoffValue = bucketCutoffValues.get(a);
							if (metricValue < bucketCutoffValue) {
								break;
							}
							bucketNum++;
						}
						
						metricPart += ("BUCKET" + bucketNum + ", ");
//						metricPart += metricValue + ", ";
					}
				}
				// Class
				String classPart = "?";
				
				if (!metricPart.equals("")) {
					String recordLine = refrencePart + metricPart + classPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					valuesList.add(valueList);
				}
			}
			return valuesList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static float findMin(ArrayList<Float> list, int targetGainIndex) {
		float min = 1000000000f;
		for (int a = 0; a <= targetGainIndex; a++) {
			if (list.get(a) < min) {
				min = list.get(a);
			}
		}
		return min;
	}
	
	private static float findMax(ArrayList<Float> list) {
		float max = -1f;
		for (int a = 0; a < list.size(); a++) {
			if (list.get(a) > max) {
				max = list.get(a);
			}
		}
		return max;
	}
	
	private static int findTargetGainIndex(ArrayList<Float> list, float close, float targetGain) {
		for (int a = 0; a < list.size(); a++) {
			float targetClose = close * (100f + targetGain) / 100f;
			if (list.get(a) >= targetClose) {
				return a;
			}
		}
		return -1;
	}
	
	private static int findMaxIndex(ArrayList<Float> list) {
		float max = -1f;
		int maxIndex = -1;
		for (int a = 0; a < list.size(); a++) {
			if (list.get(a) > max) {
				max = list.get(a);
				maxIndex = a;
			}
		}
		return maxIndex;
	}
}