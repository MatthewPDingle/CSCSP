package ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import dbio.QueryManager;
import search.GeneticSearcher;

public class TrainingSetCreator {

	public static void main(String[] args) {

		Calendar periodStart = Calendar.getInstance();
		periodStart.set(Calendar.YEAR, 2014);
		periodStart.set(Calendar.MONTH, 0);
		periodStart.set(Calendar.DAY_OF_MONTH, 1);
		periodStart.set(Calendar.HOUR_OF_DAY, 0);
		periodStart.set(Calendar.MINUTE, 0);
		periodStart.set(Calendar.SECOND, 0);
		periodStart.set(Calendar.MILLISECOND, 0);
		
		Calendar periodEnd = Calendar.getInstance();
		periodEnd.set(Calendar.YEAR, 2014);
		periodEnd.set(Calendar.MONTH, 7);
		periodEnd.set(Calendar.DAY_OF_MONTH, 1);
		periodEnd.set(Calendar.HOUR_OF_DAY, 0);
		periodEnd.set(Calendar.MINUTE, 0);
		periodEnd.set(Calendar.SECOND, 0);
		periodEnd.set(Calendar.MILLISECOND, 0);
		
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
		
		BarKey bk = new BarKey("bitstampBTCUSD", BAR_SIZE.BAR_15M);
		
		create(periodStart, periodEnd, 1f, .5f, 10, bk, metricNames);
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
	 */
	private static void create(Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, int numPeriods, BarKey bk, ArrayList<String> metricNames) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Float>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			// We want to bucket the metric values by these percentiles yielding 14 buckets.
			int[] percentiles = {1, 2, 5, 10, 20, 35, 50, 65, 80, 90, 95, 98, 99};
			HashMap<String, ArrayList<Float>> metricDiscreteValueHash = GeneticSearcher.loadBullMetricDiscreteValueLists(percentiles, metricNames);
			
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			for (HashMap<String, Float> record : rawTrainingSet) {
				float close = record.get("close");
				nextXCloses.add(close);
				if (nextXCloses.size() > numPeriods) {
					nextXCloses.remove(0);
				}
				
				System.out.println("-------");
				System.out.println(close);
				System.out.println(Arrays.toString(nextXCloses.toArray()));
				
				boolean minLossOK = false;
				float minClose = findMin(nextXCloses);
				if (minClose >= close * (100f - minLoss) / 100f) {
					minLossOK = true;
				}
				
				boolean targetGainOK = false;
				float maxClose = findMax(nextXCloses);
				if (maxClose >= close * (100f + targetGain) / 100f) {
					targetGainOK = true;
				}
				
				System.out.println(minLossOK + ", " + targetGainOK);
				
				for (String metricName : metricNames) {
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(metricName);
					float metricValue = record.get(metricName);
					
					int bucketNum = 0;
					for (int a = bucketCutoffValues.size() - 1; a >= 0; a--) {
						float bucketCutoffValue = bucketCutoffValues.get(a);
						if (metricValue < bucketCutoffValue) {
							break;
						}
						bucketNum++;
					}
					
					System.out.println(metricName + " - " + metricValue + " in bucket #" + bucketNum);
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static float findMin(ArrayList<Float> list) {
		float min = 1000000000f;
		for (float f : list) {
			if (f < min) {
				min = f;
			}
		}
		return min;
	}
	
	private static float findMax(ArrayList<Float> list) {
		float max = -1f;
		for (float f : list) {
			if (f > max) {
				max = f;
			}
		}
		return max;
	}
}