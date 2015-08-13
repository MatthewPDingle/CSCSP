package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import constants.Constants;
import data.BarKey;
import data.Metric;
import data.MetricKey;
import dbio.QueryManager;
import gui.singletons.MetricSingleton;

public class MetricsCalculatorRealtime {
	
	public static void calculateMetricsRealtime() {
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
						MetricsCalculator.fillInRSI(ms, 2);
						break;
					case "rsi5":
						MetricsCalculator.fillInRSI(ms, 5);					
						break;
					case "rsi14":
						MetricsCalculator.fillInRSI(ms, 14);
						break;
					
					// MFI
					case "mfi2":
						MetricsCalculator.fillInMFI(ms, 2);
						break;
					case "mfi5":
						MetricsCalculator.fillInMFI(ms, 5);					
						break;
					case "mfi14":
						MetricsCalculator.fillInMFI(ms, 14);
						break;
						
					// Consecutive Bars
					case "consecutiveups":
						MetricsCalculator.fillInConsecutiveUps(ms);
						break;
					case "consecutivedowns":
						MetricsCalculator.fillInConsecutiveDowns(ms);
						break;
						
					// Consecutive Percents
					case "cps":
						MetricsCalculator.fillInCPS(ms);
						break;
						
					// CCI
					case "cci10":
						MetricsCalculator.fillInCCI(ms, 10);
						break;
					case "cci20":
						MetricsCalculator.fillInCCI(ms, 20);					
						break;
					case "cci40":	
						MetricsCalculator.fillInCCI(ms, 40);
						break;
						
					// Williams R
					case "williamsr10":
						MetricsCalculator.fillInWilliamsR(ms, 10);
						break;
					case "williamsr20":
						MetricsCalculator.fillInWilliamsR(ms, 20);					
						break;
					case "williamsr50":
						MetricsCalculator.fillInWilliamsR(ms, 50);
						break;	
						
					// PSAR
					case "psar":
						MetricsCalculator.fillInPSAR(ms);
						break;
						
					// Ultimate
					case "ultimateoscillator4_10_25":
						MetricsCalculator.fillInUltimateOscillator(ms, 4, 10, 25);
						break;
					case "ultimateoscillator8_20_50":
						MetricsCalculator.fillInUltimateOscillator(ms, 8, 20, 50);
						break;
					
					// Aroon
					case "aroonoscillator10":
						MetricsCalculator.fillInAroonOscillator(ms, 10);
						break;
					case "aroonoscillator25":
						MetricsCalculator.fillInAroonOscillator(ms, 25);
						break;
					case "aroonoscillator50":
						MetricsCalculator.fillInAroonOscillator(ms, 50);
						break;
					
					// Price Boll using SMA
					case "pricebolls20":
						MetricsCalculator.fillInPriceBollS(ms, 20);
						break;
					case "pricebolls50":
						MetricsCalculator.fillInPriceBollS(ms, 50);					
						break;
					case "pricebolls100":
						MetricsCalculator.fillInPriceBollS(ms, 100);
						break;
					case "pricebolls200":
						MetricsCalculator.fillInPriceBollS(ms, 200);
						break;	
				}
				
				
				// Insert the MetricSequence
				QueryManager.insertOrUpdateIntoMetrics(ms);
				
				switch (mk.name) {

					// Gap Boll
					case "gapboll10":
						
						break;
					case "gapboll20":
											
						break;
					case "gapboll50":
						
						break;

					// Intraday Boll
					case "intradayboll10":
						
						break;
					case "intradayboll20":
											
						break;
					case "intradayboll50":
						
						break;
					
					// Volume Boll
					case "volumeboll20":
						
						break;
					case "volumeboll50":
											
						break;
					case "volumeboll100":
						
						break;
					case "volumeboll200":
						
						break;	
						
					// DVOL
					case "dvol10ema":
						
						break;
					case "dvol25ema":
											
						break;
					case "dvol50ema":
						
						break;
					case "dvol75ema":
						
						break;	
						


					// MACD
					case "macd12_26_9":
						
						break;
					case "macd20_40_9":
											
						break;
					case "macd_40_80_9":
					
						break;
						
					// MACD Divergence
					case "macddivergence12_26_9":
						
						break;
					case "macddivergence20_40_9":
											
						break;
					case "macddivergence_40_80_9":
						
						break;	
		
						
					default:
						break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void calculateMetricsRealtime(ArrayList<String> metrics, ArrayList<BarKey> barKeys) {
		try {
			ArrayList<LinkedList<Metric>> metricSequences = QueryManager.loadMetricSequencesForRealtimeUpdates(barKeys);

			Calendar maxStartFromBar = QueryManager.getMaxStartFromBar();
			
			// Unfortunately we have to go through all the metrics to see which ones need to be run
			for (String metric:Constants.METRICS) {
				if (metrics.contains(metric)) {
					for (LinkedList<Metric> ms:metricSequences) {

								
//						// DV EMA
//						if (metric.equals("dv10ema")) {
//							MetricsCalculator.fillInWeightedDVEMA(null, ms, 10);
//							QueryManager.insertIntoMetrics(ms);
//						}
//						if (metric.equals("dv25ema")) {
//							MetricsCalculator.fillInWeightedDVEMA(null, ms, 25);
//							QueryManager.insertIntoMetrics(ms);
//						}
//						if (metric.equals("dv50ema")) {
//							MetricsCalculator.fillInWeightedDVEMA(null, ms, 50);
//							QueryManager.insertIntoMetrics(ms);
//						}
//						if (metric.equals("dv75ema")) {
//							MetricsCalculator.fillInWeightedDVEMA(null, ms, 75);
//							QueryManager.insertIntoMetrics(ms);
//						}
//						
//						// Other DV
//						if (metric.equals("dv2")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDV2(ms));
//						}
//						
//						// RSI
//						if (metric.equals("rsi2")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(ms, 2));
//						}
//						if (metric.equals("rsi5")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(ms, 5));
//						}
//						if (metric.equals("rsi14")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(ms, 14));
//						}
						
//						// RSI Alpha
//						if (metric.equals("rsi2alpha")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(ms, 2));
//						}
//						if (metric.equals("rsi5alpha")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(ms, 5));
//						}
//						if (metric.equals("rsi14alpha")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(ms, 14));
//						}
//						
//						// RSI EMA
//						if (metric.equals("rsi75ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 75));
//						}
//						if (metric.equals("rsi50ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 50));
//						}
//						if (metric.equals("rsi25ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 25));
//						}
//						if (metric.equals("rsi10ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 10));
//						}
//
//						// DVOL
//						if (metric.equals("dvol75ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 75));	
//						}
//						if (metric.equals("dvol50ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 50));	
//						}
//						if (metric.equals("dvol25ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 25));	
//						}
//						if (metric.equals("dvol10ema")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 10));	
//						}
//
//						// Breakout
//						if (metric.equals("breakout20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 20));	
//						}
//						if (metric.equals("breakout50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 50));	
//						}
//						if (metric.equals("breakout100")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 100));	
//						}
//						if (metric.equals("breakout200")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 200));	
//						}
//						
//						// Streaks
//						if (metric.equals("consecutiveupdays")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInUpStreaks(ms));
//						}
//						if (metric.equals("consecutivedowndays")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDownStreaks(ms));
//						}
//							
//						// Price Boll
//						if (metric.equals("priceboll20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 20));
//						}
//						if (metric.equals("priceboll50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 50));
//						}
//						if (metric.equals("priceboll100")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 100));
//						}
//						if (metric.equals("priceboll200")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 200));
//						}
//
//						// Gap Boll
//						if (metric.equals("gapboll10")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(ms, 10));
//						}
//						if (metric.equals("gapboll20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(ms, 20));
//						}
//						if (metric.equals("gapboll50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(ms, 50));
//						}
//
//						// Intraday Boll
//						if (metric.equals("intradayboll10")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(ms, 10));
//						}
//						if (metric.equals("intradayboll20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(ms, 20));
//						}
//						if (metric.equals("intradayboll50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(ms, 50));
//						}
//						
//						// Volume Boll
//						if (metric.equals("volumeboll20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 20));
//						}
//						if (metric.equals("volumeboll50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 50));
//						}
//						if (metric.equals("volumeboll100")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 100));
//						}
//						if (metric.equals("volumeboll200")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 200));
//						}
//						
//						// Williams R
//						if (metric.equals("williamsr10")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(ms, 10));
//						}
//						if (metric.equals("williamsr20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(ms, 20));
//						}
//						if (metric.equals("williamsr50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(ms, 50));
//						}
//						
//						// Williams R Alpha
//						if (metric.equals("williamsralpha10")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(ms, 10));
//						}
//						if (metric.equals("williamsralpha20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(ms, 20));
//						}
//						if (metric.equals("williamsralpha50")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(ms, 50));
//						}
//
//						// MACD
//						if (metric.equals("macd12_26_9")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(ms, 12, 26, 9));
//						}
//						if (metric.equals("macd20_40_9")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(ms, 20, 40, 9));
//						}
//						if (metric.equals("macd40_80_9")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(ms, 40, 80, 9));
//						}
//						
//						// MACD Divergence
//						if (metric.equals("macddivergence12_26_9")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(ms, 12, 26, 9));
//						}
//						if (metric.equals("macddivergence20_40_9")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(ms, 20, 40, 9));
//						}
//						if (metric.equals("macddivergence40_80_9")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(ms, 40, 80, 9));
//						}
//						
//						// MFI
//						if (metric.equals("mfi2")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(ms, 2));
//						}
//						if (metric.equals("mfi5")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(ms, 5));
//						}
//						if (metric.equals("mfi14")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(ms, 14));
//						}
//						
//						// PSAR
//						if (metric.equals("psar")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPSAR(ms));
//						}
//						
//						// Aroon
//						if (metric.equals("aroonoscillator")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInAroonOscillator(ms));
//						}
//						
//						// Ultimate
//						if (metric.equals("ultimateoscillator")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInUltimateOscillator(ms));
//						}
//						
//						// CCI
//						if (metric.equals("cci10")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(ms, 10));
//						}
//						if (metric.equals("cci20")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(ms, 20));
//						}
//						if (metric.equals("cci40")) {
//							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(ms, 40));
//						}
						
					} // End for metricSequences
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}