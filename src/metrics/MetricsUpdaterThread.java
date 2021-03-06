package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.Metric;
import data.MetricKey;
import dbio.QueryManager;
import gui.singletons.MetricSingleton;

public class MetricsUpdaterThread extends Thread {
	
	private boolean running = false;
	
	/**
	 * Parameters have to come in sets of 2.
	 * First is symbol
	 * Second is bar duration
	 * @param args
	 */
	public static void main (String[] args) {
		// Get params
		ArrayList<String[]> params = new ArrayList<String[]>();
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		if (args != null) {
			for (int a = 0; a < args.length; a += 2) {
				String symbol = args[a];
				String duration = args[a + 1];

				String[] param = new String[2];
				param[0] = symbol;
				param[1] = duration;
				params.add(param);
				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
		}
		
		// What metrics do we want
		ArrayList<String> metrics = new ArrayList<String>(); // Constants.METRICS
		metrics.add("atr10");
		metrics.add("atr20");
		metrics.add("atr40");

		if (params.size() > 0) {
			MetricSingleton metricSingleton = MetricSingleton.getInstance();
			System.out.println(Calendar.getInstance().getTime().toString() + " - Initializing MetricSingleton");
			metricSingleton.init(barKeys, metrics);
			System.out.println(Calendar.getInstance().getTime().toString() + " - Initializing MetricSingleton done");
			System.out.println(Calendar.getInstance().getTime().toString() + " - Metric Calculations Starting");
			MetricsUpdaterThread.calculateMetrics();
			System.out.println(Calendar.getInstance().getTime().toString() + " - Metric Calculations Done");		
		}
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		try {
			Map.Entry<MetricKey, ArrayList<Metric>> singleMetricSequence = MetricSingleton.getInstance().popSingleMetricSequence();
			while (singleMetricSequence != null && running) {
				MetricKey mk = (MetricKey)singleMetricSequence.getKey();
				ArrayList<Metric> ms = (ArrayList<Metric>)singleMetricSequence.getValue();
				
				switch (mk.name) {
					// RSI
					case "rsi2":
						MetricFunctionUtil.fillInRSI(ms, 2);
						break;
					case "rsi5":
						MetricFunctionUtil.fillInRSI(ms, 5);					
						break;
					case "rsi14":
						MetricFunctionUtil.fillInRSI(ms, 14);
						break;
					
					// MFI
					case "mfi8":
						MetricFunctionUtil.fillInMFI(ms, 8);
						break;
					case "mfi12":
						MetricFunctionUtil.fillInMFI(ms, 12);					
						break;
					case "mfi16":
						MetricFunctionUtil.fillInMFI(ms, 16);
						break;
						
					// Consecutive Bars
					case "consecutiveups":
						MetricFunctionUtil.fillInConsecutiveUps(ms);
						break;
					case "consecutivedowns":
						MetricFunctionUtil.fillInConsecutiveDowns(ms);
						break;
						
					// Consecutive Percents
					case "cps":
						MetricFunctionUtil.fillInCPS(ms);
						break;
						
					// CCI
					case "cci10":
						MetricFunctionUtil.fillInCCI(ms, 10);
						break;
					case "cci20":
						MetricFunctionUtil.fillInCCI(ms, 20);					
						break;
					case "cci40":	
						MetricFunctionUtil.fillInCCI(ms, 40);
						break;
						
					// Williams R
					case "williamsr10":
						MetricFunctionUtil.fillInWilliamsR(ms, 10);
						break;
					case "williamsr20":
						MetricFunctionUtil.fillInWilliamsR(ms, 20);					
						break;
					case "williamsr50":
						MetricFunctionUtil.fillInWilliamsR(ms, 50);
						break;	
						
					// PSAR
					case "psar":
						MetricFunctionUtil.fillInPSAR(ms);
						break;
						
					// Ultimate
					case "ultimateoscillator4_10_25":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 4, 10, 25);
						break;
					case "ultimateoscillator8_20_50":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 8, 20, 50);
						break;
					
					// Aroon
					case "aroonoscillator10":
						MetricFunctionUtil.fillInAroonOscillator(ms, 10);
						break;
					case "aroonoscillator25":
						MetricFunctionUtil.fillInAroonOscillator(ms, 25);
						break;
					case "aroonoscillator50":
						MetricFunctionUtil.fillInAroonOscillator(ms, 50);
						break;
					
					// Price Boll using SMA
					case "pricebolls20":
						MetricFunctionUtil.fillInPriceBollS(ms, 20);
						break;
					case "pricebolls50":
						MetricFunctionUtil.fillInPriceBollS(ms, 50);					
						break;
					case "pricebolls100":
						MetricFunctionUtil.fillInPriceBollS(ms, 100);
						break;	
						
					// Volume Boll using SMA
					case "volumebolls20":
						MetricFunctionUtil.fillInVolumeBollS(ms, 20);
						break;
					case "volumebolls50":
						MetricFunctionUtil.fillInVolumeBollS(ms, 50);					
						break;
					case "volumebolls100":
						MetricFunctionUtil.fillInVolumeBollS(ms, 100);
						break;
	
					// MACD
					case "macd12_26_9":
						MetricFunctionUtil.fillInMACD(ms, 12, 26, 9);
						break;
					case "macd20_40_9":
						MetricFunctionUtil.fillInMACD(ms, 20, 40, 9);					
						break;
	
					// MACD Signal
					case "macdsignal12_26_9":
						MetricFunctionUtil.fillInMACDSignal(ms, 12, 26, 9);
						break;
					case "macdsignal20_40_9":
						MetricFunctionUtil.fillInMACDSignal(ms, 20, 40, 9);					
						break;
	
					// MACD History
					case "macdhistory12_26_9":
						MetricFunctionUtil.fillInMACDHistory(ms, 12, 26, 9);
						break;
					case "macdhistory20_40_9":
						MetricFunctionUtil.fillInMACDHistory(ms, 20, 40, 9);							
						break;
						
					// Time Series Forecast
					case "tsf10":
						MetricFunctionUtil.fillInTSF(ms, 10);
						break;
						
					case "tsf20":
						MetricFunctionUtil.fillInTSF(ms, 20);
						break;
						
					case "tsf40":
						MetricFunctionUtil.fillInTSF(ms, 40);
						break;
						
					// Stochastic RSI
					case "stochasticdrsi14_3_3":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 14, 3, 3);
						break;
					case "stochasticdrsi20_5_5":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 20, 5, 5);
						break;
						
					// Stochastic
					case "stochasticd14_3_3":
						MetricFunctionUtil.fillInStochasticD(ms, 14, 3, 3);
						break;
					case "stochastick14_3_3":
						MetricFunctionUtil.fillInStochasticK(ms, 14, 3, 3);
						break;	
					case "stochasticd20_5_5":
						MetricFunctionUtil.fillInStochasticD(ms, 20, 5, 5);
						break;
					case "stochastick20_5_5":
						MetricFunctionUtil.fillInStochasticK(ms, 20, 5, 5);
						break;	
						
					// Average True Range
					case "atr10":
						MetricFunctionUtil.fillInATR(ms, 10);
						break;
						
					case "atr20":
						MetricFunctionUtil.fillInATR(ms, 20);
						break;
						
					case "atr40":
						MetricFunctionUtil.fillInATR(ms, 40);
						break;	
						
					// Candlestick Patterns
					case "cdlhammer":
						MetricFunctionUtil.fillInPattern(ms, "cdlhammer");
						break;
					case "cdldoji":
						MetricFunctionUtil.fillInPattern(ms, "cdldoji");
						break;
					case "cdlmorningstar":
						MetricFunctionUtil.fillInPattern(ms, "cdlmorningstar");
						break;
				}
				
				QueryManager.insertOrUpdateIntoMetrics(ms);
				
				singleMetricSequence = MetricSingleton.getInstance().popSingleMetricSequence();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public static void calculateMetrics() {
		try {
			MetricSingleton metricSingleton = MetricSingleton.getInstance();
			metricSingleton.updateMetricSequenceHash();
			HashMap<MetricKey, ArrayList<Metric>>metricSequenceHash = metricSingleton.getMetricSequenceHash();

			// Go through each MetricKey, calculate the metric values in the sequence,
			Iterator i = metricSequenceHash.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry pair = (Map.Entry)i.next();
				// Get this MetricKey
				MetricKey mk = (MetricKey)pair.getKey();
//				System.out.println("Processing " + mk.toString());
				// Get this MetricSequence
				ArrayList<Metric> ms = (ArrayList<Metric>)pair.getValue();

				switch (mk.name) {
					// RSI
					case "rsi2":
						MetricFunctionUtil.fillInRSI(ms, 2);
						break;
					case "rsi5":
						MetricFunctionUtil.fillInRSI(ms, 5);					
						break;
					case "rsi14":
						MetricFunctionUtil.fillInRSI(ms, 14);
						break;
					
					// MFI
					case "mfi8":
						MetricFunctionUtil.fillInMFI(ms, 8);
						break;
					case "mfi12":
						MetricFunctionUtil.fillInMFI(ms, 12);					
						break;
					case "mfi16":
						MetricFunctionUtil.fillInMFI(ms, 16);
						break;
						
					// Consecutive Bars
					case "consecutiveups":
						MetricFunctionUtil.fillInConsecutiveUps(ms);
						break;
					case "consecutivedowns":
						MetricFunctionUtil.fillInConsecutiveDowns(ms);
						break;
						
					// Consecutive Percents
					case "cps":
						MetricFunctionUtil.fillInCPS(ms);
						break;
						
					// CCI
					case "cci10":
						MetricFunctionUtil.fillInCCI(ms, 10);
						break;
					case "cci20":
						MetricFunctionUtil.fillInCCI(ms, 20);					
						break;
					case "cci40":	
						MetricFunctionUtil.fillInCCI(ms, 40);
						break;
						
					// Williams R
					case "williamsr10":
						MetricFunctionUtil.fillInWilliamsR(ms, 10);
						break;
					case "williamsr20":
						MetricFunctionUtil.fillInWilliamsR(ms, 20);					
						break;
					case "williamsr50":
						MetricFunctionUtil.fillInWilliamsR(ms, 50);
						break;	
						
					// PSAR
					case "psar":
						MetricFunctionUtil.fillInPSAR(ms);
						break;
						
					// Ultimate
					case "ultimateoscillator4_10_25":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 4, 10, 25);
						break;
					case "ultimateoscillator8_20_50":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 8, 20, 50);
						break;
					
					// Aroon
					case "aroonoscillator10":
						MetricFunctionUtil.fillInAroonOscillator(ms, 10);
						break;
					case "aroonoscillator25":
						MetricFunctionUtil.fillInAroonOscillator(ms, 25);
						break;
					case "aroonoscillator50":
						MetricFunctionUtil.fillInAroonOscillator(ms, 50);
						break;
					
					// Price Boll using SMA
					case "pricebolls20":
						MetricFunctionUtil.fillInPriceBollS(ms, 20);
						break;
					case "pricebolls50":
						MetricFunctionUtil.fillInPriceBollS(ms, 50);					
						break;
					case "pricebolls100":
						MetricFunctionUtil.fillInPriceBollS(ms, 100);
						break;	
						
					// Volume Boll using SMA
					case "volumebolls20":
						MetricFunctionUtil.fillInVolumeBollS(ms, 20);
						break;
					case "volumebolls50":
						MetricFunctionUtil.fillInVolumeBollS(ms, 50);					
						break;
					case "volumebolls100":
						MetricFunctionUtil.fillInVolumeBollS(ms, 100);
						break;

					// MACD
					case "macd12_26_9":
						MetricFunctionUtil.fillInMACD(ms, 12, 26, 9);
						break;
					case "macd20_40_9":
						MetricFunctionUtil.fillInMACD(ms, 20, 40, 9);					
						break;

					// MACD Signal
					case "macdsignal12_26_9":
						MetricFunctionUtil.fillInMACDSignal(ms, 12, 26, 9);
						break;
					case "macdsignal20_40_9":
						MetricFunctionUtil.fillInMACDSignal(ms, 20, 40, 9);					
						break;

					// MACD History
					case "macdhistory12_26_9":
						MetricFunctionUtil.fillInMACDHistory(ms, 12, 26, 9);
						break;
					case "macdhistory20_40_9":
						MetricFunctionUtil.fillInMACDHistory(ms, 20, 40, 9);							
						break;
						
					// Time Series Forecast
					case "tsf10":
						MetricFunctionUtil.fillInTSF(ms, 10);
						break;
						
					case "tsf20":
						MetricFunctionUtil.fillInTSF(ms, 20);
						break;
						
					case "tsf40":
						MetricFunctionUtil.fillInTSF(ms, 40);
						break;
						
					// Stochastic RSI
					case "stochasticdrsi14_3_3":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 14, 3, 3);
						break;
					case "stochasticdrsi20_5_5":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 20, 5, 5);
						break;
						
					// Stochastic
					case "stochasticd14_3_3":
						MetricFunctionUtil.fillInStochasticD(ms, 14, 3, 3);
						break;
					case "stochastick14_3_3":
						MetricFunctionUtil.fillInStochasticK(ms, 14, 3, 3);
						break;	
					case "stochasticd20_5_5":
						MetricFunctionUtil.fillInStochasticD(ms, 20, 5, 5);
						break;
					case "stochastick20_5_5":
						MetricFunctionUtil.fillInStochasticK(ms, 20, 5, 5);
						break;	
						
					// Average True Range
					case "atr10":
						MetricFunctionUtil.fillInATR(ms, 10);
						break;
						
					case "atr20":
						MetricFunctionUtil.fillInATR(ms, 20);
						break;
						
					case "atr40":
						MetricFunctionUtil.fillInATR(ms, 40);
						break;	
						
					// Candlestick Patterns
					case "cdlhammer":
						MetricFunctionUtil.fillInPattern(ms, "cdlhammer");
						break;
					case "cdldoji":
						MetricFunctionUtil.fillInPattern(ms, "cdldoji");
						break;
					case "cdlmorningstar":
						MetricFunctionUtil.fillInPattern(ms, "cdlmorningstar");
						break;
				}
				
				// Insert the MetricSequence
				QueryManager.insertOrUpdateIntoMetrics(ms);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}