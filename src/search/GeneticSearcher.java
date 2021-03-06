package search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import dbio.QueryManager;
import gui.GUI;
import gui.singletons.ParameterSingleton;
import utils.CalcUtils;
import utils.CalendarUtils;

public class GeneticSearcher {

	private static float MAX_FITNESS = 1500;
	
	private HashMap<String, Float> bullishBuyMetricFitnessHash = new HashMap<String, Float>();
	private HashMap<String, Float> bullishSellMetricFitnessHash = new HashMap<String, Float>();
	private HashMap<String, Float> bullishStopMetricFitnessHash = new HashMap<String, Float>();
	
	private HashMap<String, Float> bearishBuyMetricFitnessHash = new HashMap<String, Float>();
	private HashMap<String, Float> bearishSellMetricFitnessHash = new HashMap<String, Float>();
	private HashMap<String, Float> bearishStopMetricFitnessHash = new HashMap<String, Float>();
	
	private HashMap<String, ArrayList<Float>> bullMetricDiscreteValueList = new HashMap<String, ArrayList<Float>>();
	private HashMap<String, ArrayList<Float>> bearMetricDiscreteValueList = new HashMap<String, ArrayList<Float>>();
	private HashMap<String, ArrayList<Integer>> stopDiscreteValueList = new HashMap<String, ArrayList<Integer>>();
	private Random r;
	
	private String searchType = "bull"; // or "bear"
	
	public static void main(String[] args) {
		GeneticSearcher searcher = new GeneticSearcher();
		searcher.r = new Random();
		
		// Initialize
		searcher.bullishBuyMetricFitnessHash = searcher.initBullMetricFitnessHashFromDB("buy");
		searcher.bullishSellMetricFitnessHash = searcher.initBullMetricFitnessHashFromDB("sell");
		searcher.bullishStopMetricFitnessHash = searcher.initBullMetricFitnessHashFromDB("stop");
		
		searcher.bearishBuyMetricFitnessHash = searcher.initBearMetricFitnessHashFromDB("buy");
		searcher.bearishSellMetricFitnessHash = searcher.initBearMetricFitnessHashFromDB("sell");
		searcher.bearishStopMetricFitnessHash = searcher.initBearMetricFitnessHashFromDB("stop");
		
		searcher.printMetricFitnesses("buy");
		searcher.printMetricFitnesses("sell");
		searcher.printMetricFitnesses("stop");

		System.out.print("Loading Metric Discrete Value Lists. ");
		int[] percentiles = {1, 5, 10, 20, 33, 50, 70};
		searcher.bullMetricDiscreteValueList = loadBullMetricDiscreteValueLists(percentiles, Constants.METRICS);
		searcher.bearMetricDiscreteValueList = loadBearMetricDiscreteValueLists(percentiles, Constants.METRICS);
		searcher.stopDiscreteValueList = loadStopDiscreteValueLists();
		System.out.println("Done.");
		
		for (int a = 0; a < 4000; a++) {
			if (a % 2 == 1) {
				searcher.searchType = "bull";
			}
			else {
				searcher.searchType = "bear";
			}
			System.out.println("----- #" + a + " as " + searcher.searchType + " search -----");
			searcher.search(a);
			System.gc();
		}
	}
	
	private void search(int runNumber) {	
		// Don't run from 4-5 AM cause that's when I update basicr & re-calculate the metrics.
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		while (hour == 4) {
			try {
				Thread.sleep(1000*60); // Wait a minute
				now = Calendar.getInstance();
				hour = now.get(Calendar.HOUR_OF_DAY);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
			
		// Run and test a set of maps
		String[] params = new String[23];
		String buy1 = null;
		String buy2 = null;
		String sell = null;
		String sellOp = null;
		float sellValue = 0f;
		String stop = null;
		int stopValue = 0;
		
		Calendar fromC = Calendar.getInstance();
		fromC.add(Calendar.DATE, -500);
		
		Calendar toC = Calendar.getInstance();
		
		boolean used = true;
		while (used) {
			// Buy
			buy1 = getRandomBuyMetricWeightedByFitness(searchType);
			buy2 = getRandomBuyMetricWeightedByFitness(searchType);
			while (buy2.equals(buy1)) {
				buy2 = getRandomBuyMetricWeightedByFitness(searchType);
			}
			
			if (buy1.compareTo(buy2) < 0) {
				String buyt = buy1;
				buy1 = buy2;
				buy2 = buyt;
			}
			
			// Sell
			sell = getRandomSellMetricWeightedByFitness(searchType);
			while (sell.equals(buy1) || sell.equals(buy2)) {
				sell = getRandomSellMetricWeightedByFitness(searchType);
			}
			if (searchType.equals("bull")) {
				sellOp = ">=";
				sellValue = getRandomFloatFromList(bullMetricDiscreteValueList.get(sell));
			}
			else {
				sellOp = "<=";
				sellValue = getRandomFloatFromList(bearMetricDiscreteValueList.get(sell));
			}

			// Stop
//			stop = getRandomStopWeightedByFitness(searchType);
			stop = Constants.STOP_METRIC_NUM_BARS;
			if (!stop.equals(Constants.STOP_METRIC_NONE)) {
				stopValue = getRandomIntFromList(stopDiscreteValueList.get(stop));
			}
			
			// Fuck with the values here:
//			buy1 = "consecutivedowndays";
//			buy2 = "rsi5";
//			sell = "williamsr50";
//			sellValue = 80;
//			stop = Constants.STOP_METRIC_NUM_BARS;
//			stopValue = 14;
			
			used = QueryManager.seeIfSearchComboHasBeenUsed(buy1, buy2, sell, sellOp, sellValue, stop, (float)stopValue, 
					new java.sql.Date(fromC.getTime().getTime()), new java.sql.Date(toC.getTime().getTime()));
		}
		
		System.out.println("Buy: " + buy1 + ", " + buy2);
		System.out.println("Sell: " + sell + " " + sellOp + " " + sellValue);
		System.out.println("Stop: " + stop + ", " + stopValue);
		
		params[0] = buy1;
		params[1] = buy2;
		params[2] = sell;
		params[3] = sellOp;
		params[4] = new Float(sellValue).toString();
		params[5] = stop;
		if (stop.equals("None"))
			params[6] = "\"\"";
		else
			params[6] = new Integer(stopValue).toString();
		params[7] = CalendarUtils.getGUIDateString(fromC);
		params[8] = CalendarUtils.getGUIDateString(toC);
		params[9] = "20";
		params[10] = "20";
		params[11] = "1"; // 2000000 for stocks (liquidity)
		params[12] = "5"; // 1 for stocks (volatility)
		params[13] = ".01"; // 3 for stocks (price)
		params[14] = "All";
		params[15] = "All";
		params[16] = "false";
		params[17] = "false";
		params[18] = "false";
		params[19] = "false";
		params[20] = "false";
		params[21] = "true";
		params[22] = "BAR_15M - bitstampBTCUSD"; // If you want more, separate with commas with no spaces

		ParameterSingleton.getInstance().setRunFinished(false);
		
		GUI gui = new GUI();
		gui.runBackdoor(params);
		
		while (!ParameterSingleton.getInstance().isRunFinished()) {
			try {
				Thread.sleep(100);
				System.out.println("Waiting for GUI to finish before recording scores & starting new run");
			}
			catch (Exception e) {}
		}
		
		ScoreSingleton ss = ScoreSingleton.getInstance();
		float bullScore = ss.getBullScore();
		float bearScore = ss.getBearScore();
		
		// Every metric starts with a fitness of 100.  Bad maps -4.  Good Maps + 1/10th score
		float adjBullScore = bullScore / 10f;
		if (adjBullScore <= 0) {
			adjBullScore = -4;
		}
		float adjBearScore = bearScore / 10f;
		if (adjBearScore >= 0) {
			adjBearScore = -4;
		}
		else {
			adjBearScore = -adjBearScore; // Make it +
		}
		System.out.println("Bull Score: " + bullScore);
		System.out.println("Bear Score: " + bearScore);
		
		if (searchType.equals("bull") || adjBullScore > 0) {
			Float preExistingBullFitness1 = bullishBuyMetricFitnessHash.get(buy1);
			Float newBullFitness1 = preExistingBullFitness1 + adjBullScore;
			if (newBullFitness1 > MAX_FITNESS) newBullFitness1 = MAX_FITNESS;
			bullishBuyMetricFitnessHash.put(buy1, newBullFitness1);
			
			Float preExistingBullFitness2 = bullishBuyMetricFitnessHash.get(buy2);
			Float newBullFitness2 = preExistingBullFitness2 + adjBullScore;
			if (newBullFitness2 > MAX_FITNESS) newBullFitness2 = MAX_FITNESS;
			bullishBuyMetricFitnessHash.put(buy2, newBullFitness2);
			
			Float preExistingBullFitness3 = bullishSellMetricFitnessHash.get(sell);
			Float newBullFitness3 = preExistingBullFitness3 + adjBullScore;
			if (newBullFitness3 > MAX_FITNESS) newBullFitness3 = MAX_FITNESS;
			bullishSellMetricFitnessHash.put(sell, newBullFitness3);
			
			Float preExistingBullFitness4 = bullishStopMetricFitnessHash.get(stop);
			Float newBullFitness4 = preExistingBullFitness4 + adjBullScore;
			if (newBullFitness4 > MAX_FITNESS) newBullFitness4 = MAX_FITNESS;
			bullishStopMetricFitnessHash.put(stop, newBullFitness4);
			
			saveBullishSearchFitness();
		}
		if (searchType.equals("bear") || adjBearScore > 0) {
			Float preExistingBearFitness1 = bearishBuyMetricFitnessHash.get(buy1);
			Float newBearFitness1 = preExistingBearFitness1 + adjBearScore;
			if (newBearFitness1 > MAX_FITNESS) newBearFitness1 = MAX_FITNESS;
			bearishBuyMetricFitnessHash.put(buy1, newBearFitness1);
			
			Float preExistingBearFitness2 = bearishBuyMetricFitnessHash.get(buy2);
			Float newBearFitness2 = preExistingBearFitness2 + adjBearScore;
			if (newBearFitness2 > MAX_FITNESS) newBearFitness2 = MAX_FITNESS;
			bearishBuyMetricFitnessHash.put(buy2, newBearFitness2);
			
			Float preExistingBearFitness3 = bearishSellMetricFitnessHash.get(sell);
			Float newBearFitness3 = preExistingBearFitness3 + adjBearScore;
			if (newBearFitness3 > MAX_FITNESS) newBearFitness3 = MAX_FITNESS;
			bearishSellMetricFitnessHash.put(sell, newBearFitness3);
			
			Float preExistingBearFitness4 = bearishStopMetricFitnessHash.get(stop);
			Float newBearFitness4 = preExistingBearFitness4 + adjBearScore;
			if (newBearFitness4 > MAX_FITNESS) newBearFitness4 = MAX_FITNESS;
			bearishStopMetricFitnessHash.put(stop, newBearFitness4);
			
			saveBearishSearchFitness();
		}
	}
	
	private void saveBullishSearchFitness() {
		Iterator i = bullishBuyMetricFitnessHash.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry pairs = (Map.Entry)i.next();
			String metric = pairs.getKey().toString();
			Float fitness = (Float)pairs.getValue();
			QueryManager.updateBullFitness("buy", metric, fitness);
		}
		
		Iterator i2 = bullishSellMetricFitnessHash.entrySet().iterator();
		while (i2.hasNext()) {
			Map.Entry pairs = (Map.Entry)i2.next();
			String metric = pairs.getKey().toString();
			Float fitness = (Float)pairs.getValue();
			QueryManager.updateBullFitness("sell", metric, fitness);
		}
		
		Iterator i3 = bullishStopMetricFitnessHash.entrySet().iterator();
		while (i3.hasNext()) {
			Map.Entry pairs = (Map.Entry)i3.next();
			String metric = pairs.getKey().toString();
			Float fitness = (Float)pairs.getValue();
			QueryManager.updateBullFitness("stop", metric, fitness);
		}
	}
	
	private void saveBearishSearchFitness() {
		Iterator i = bearishBuyMetricFitnessHash.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry pairs = (Map.Entry)i.next();
			String metric = pairs.getKey().toString();
			Float fitness = (Float)pairs.getValue();
			QueryManager.updateBearFitness("buy", metric, fitness);
		}
		
		Iterator i2 = bearishSellMetricFitnessHash.entrySet().iterator();
		while (i2.hasNext()) {
			Map.Entry pairs = (Map.Entry)i2.next();
			String metric = pairs.getKey().toString();
			Float fitness = (Float)pairs.getValue();
			QueryManager.updateBearFitness("sell", metric, fitness);
		}
		
		Iterator i3 = bearishStopMetricFitnessHash.entrySet().iterator();
		while (i3.hasNext()) {
			Map.Entry pairs = (Map.Entry)i3.next();
			String metric = pairs.getKey().toString();
			Float fitness = (Float)pairs.getValue();
			QueryManager.updateBearFitness("stop", metric, fitness);
		}
	}

	private float getRandomFloatFromList(ArrayList<Float> list) {
		int index = r.nextInt(list.size());
		return list.get(index);
	}
	
	private int getRandomIntFromList(ArrayList<Integer> list) {
		int index = r.nextInt(list.size());
		return list.get(index);
	}
	
	private HashMap<String, Float> initBullMetricFitnessHashFromDB(String type) {
		HashMap<String, Float> metricFitnessHash = new HashMap<String, Float>();
		
		if (type.equals("buy") || type.equals("sell")) {
			for (String metric:Constants.METRICS) {
				if (!metric.startsWith("cdl")) {
					Float fitness = QueryManager.loadBullFitness(type, metric);
					metricFitnessHash.put(metric, fitness);
				}
				else {
					metricFitnessHash.put(metric, 0f);
				}
			}
		}
		if (type.equals("stop")) {
			for (String metric:Constants.STOP_METRICS) {
				Float fitness = QueryManager.loadBullFitness(type, metric);
				metricFitnessHash.put(metric, fitness);
			}
		}
		
		return metricFitnessHash;
	}
	
	private HashMap<String, Float> initBearMetricFitnessHashFromDB(String type) {
		HashMap<String, Float> metricFitnessHash = new HashMap<String, Float>();
		
		if (type.equals("buy") || type.equals("sell")) {
			for (String metric:Constants.METRICS) {
				if (!metric.startsWith("cdl")) {
					Float fitness = QueryManager.loadBearFitness(type, metric);
					metricFitnessHash.put(metric, fitness);
				}
				else {
					metricFitnessHash.put(metric, 0f);
				}
			}
		}
		if (type.equals("stop")) {
			for (String metric:Constants.STOP_METRICS) {
				Float fitness = QueryManager.loadBearFitness(type, metric);
				metricFitnessHash.put(metric, fitness);
			}
		}
		
		return metricFitnessHash;
	}
		
	private void printMetricFitnesses(String type) {
		if (type.equals("buy")) {
			System.out.println(("--------- BUY METRICS ---------"));
			for (String metric:Constants.METRICS) {
				float fitness = bullishBuyMetricFitnessHash.get(metric);
				System.out.println( metric + " has buy fitness of " + fitness);
			}
		}
		if (type.equals("sell")) {
			System.out.println(("--------- SELL METRICS ---------"));
			for (String metric:Constants.METRICS) {
				float fitness = bullishSellMetricFitnessHash.get(metric);
				System.out.println( metric + " has sell fitness of " + fitness);
			}
		}
		if (type.equals("stop")) {
			System.out.println(("--------- STOP METRICS ---------"));
			for (String metric:Constants.STOP_METRICS) {
				float fitness = bullishStopMetricFitnessHash.get(metric);
				System.out.println( metric + " has stop fitness of " + fitness);
			}
		}
	}
	
	private String getRandomBuyMetricWeightedByFitness(String type) {
		// Fill a gene pool with all the weighted metrics
		ArrayList<String> genePool = new ArrayList<String>();
		for (String metric:Constants.METRICS) {
			if (!metric.startsWith("cdl")) {
				float fitness = 0f;
				if (type.equals("bull")) {
					fitness = bullishBuyMetricFitnessHash.get(metric);
				}
				else if (type.equals("bear")) {
					fitness = bearishBuyMetricFitnessHash.get(metric);
				}
				if (fitness < 10) {
					fitness = 10;
				}
				for (int a = 0; a < fitness; a++) {
					genePool.add(metric);
				}
			}
		}
		
		int index = r.nextInt(genePool.size());
		return genePool.get(index);
	}
	
	private String getRandomSellMetricWeightedByFitness(String type) {
		// Fill a gene pool with all the weighted metrics
		ArrayList<String> genePool = new ArrayList<String>();
		for (String metric:Constants.METRICS) {
			if (!metric.startsWith("cdl")) {
				float fitness = 0f;
				if (type.equals("bull")) {
					fitness = bullishSellMetricFitnessHash.get(metric);
				}
				else if (type.equals("bear")) {
					fitness = bearishSellMetricFitnessHash.get(metric);
				}
				if (fitness < 10) {
					fitness = 10;
				}
				for (int a = 0; a < fitness; a++) {
					genePool.add(metric);
				}
			}
		}
		
		int index = r.nextInt(genePool.size());
		return genePool.get(index);
	}
	
	private String getRandomStopWeightedByFitness(String type) {
		// Fill a gene pool with all the weighted metrics
		ArrayList<String> genePool = new ArrayList<String>();
		for (String metric:Constants.STOP_METRICS) {
			float fitness = 0f;
			if (type.equals("bull") && !metric.equals(Constants.OTHER_SELL_METRIC_PERCENT_DOWN)) {
				fitness = bullishStopMetricFitnessHash.get(metric);
			}
			else if (type.equals("bear") && !metric.equals(Constants.OTHER_SELL_METRIC_PERCENT_UP)) {
				fitness = bearishStopMetricFitnessHash.get(metric);
			}
			if (fitness < 10) {
				fitness = 10;
			}
			for (int a = 0; a < fitness; a++) {
				genePool.add(metric);
			}
		}
		
		int index = r.nextInt(genePool.size());
		return genePool.get(index);
	}
	
	private static HashMap<String, ArrayList<Integer>> loadStopDiscreteValueLists() {
		HashMap<String, ArrayList<Integer>> stopDiscreteValueLists = new HashMap<String, ArrayList<Integer>>();
	
		ArrayList<Integer> dayVariants = new ArrayList<Integer>();
		dayVariants.add(4);
		dayVariants.add(12);
		dayVariants.add(24);
		stopDiscreteValueLists.put(Constants.STOP_METRIC_NUM_BARS, dayVariants);
		
		ArrayList<Integer> percentVariants = new ArrayList<Integer>();
		percentVariants.add(2);
		percentVariants.add(4);
		percentVariants.add(7);
		percentVariants.add(10);
		stopDiscreteValueLists.put(Constants.STOP_METRIC_PERCENT_DOWN, percentVariants);
		stopDiscreteValueLists.put(Constants.STOP_METRIC_PERCENT_UP, percentVariants);
		
		stopDiscreteValueLists.put(Constants.STOP_METRIC_NONE, new ArrayList<Integer>());
		
		return stopDiscreteValueLists;
	}
		
	public static HashMap<String, ArrayList<Float>> loadBullMetricDiscreteValueLists(int[] percentiles, ArrayList<String> metrics) {
		HashMap<String, ArrayList<Float>> metricDiscreteValueLists = new HashMap<String, ArrayList<Float>>();
		
		BarKey dummyBK = new BarKey("fart", BAR_SIZE.BAR_15M);
		
		ArrayList<Float> values = new ArrayList<Float>();
		for (String metric : metrics) {
			if (!metric.startsWith("cdl")) {
				for (int percentile : percentiles) {
					float maxValue = QueryManager.getMetricValueAtPercentile(metric, dummyBK, "max", percentile);	
					maxValue = CalcUtils.round(maxValue, 2);
					if (!values.contains(maxValue)) {
						values.add(maxValue);
					}
				}
				metricDiscreteValueLists.put(metric, values);
				values = new ArrayList<Float>();
			}
		}
		
		return metricDiscreteValueLists;
	}
	
	public static HashMap<String, ArrayList<Float>> loadBearMetricDiscreteValueLists(int[] percentiles, ArrayList<String> metrics) {
		HashMap<String, ArrayList<Float>> metricDiscreteValueLists = new HashMap<String, ArrayList<Float>>();
		
		BarKey dummyBK = new BarKey("fart", BAR_SIZE.BAR_15M);
		
		ArrayList<Float> values = new ArrayList<Float>();
		for (String metric : metrics) {
			if (!metric.startsWith("cdl")) {
				for (int percentile : percentiles) {
					float minValue = QueryManager.getMetricValueAtPercentile(metric, dummyBK, "min", percentile);					
					minValue = CalcUtils.round(minValue, 2);
					if (!values.contains(minValue)) {
						values.add(minValue);
					}
				}
				metricDiscreteValueLists.put(metric, values);
				values = new ArrayList<Float>();
			}
		}
		
		return metricDiscreteValueLists;
	}
}