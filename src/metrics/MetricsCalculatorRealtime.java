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
import utils.CalendarUtils;

public class MetricsCalculatorRealtime {
	
	public static void calculateMetricsRealtime() {
		try {
			MetricSingleton metricSingleton = MetricSingleton.getInstance();
			metricSingleton.updateMetricSequenceHash();
			HashMap<MetricKey, LinkedList<Metric>>metricSequenceHash = metricSingleton.getMetricSequenceHash();
			HashMap<MetricKey, HashMap<String, Object>> metricCalcEssentials = metricSingleton.getMetricCalcEssentialsHash();
			
			// Go through each MetricKey, calculate the metric values in the sequence,
			Iterator i = metricSequenceHash.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry pair = (Map.Entry)i.next();
				// Get this MetricKey
				MetricKey mk = (MetricKey)pair.getKey();
				System.out.println("Processing " + mk.toString());
				// Get this MetricSequence
				LinkedList<Metric> ms = (LinkedList<Metric>)pair.getValue();
				// Get this MetricCalcEssentials
				HashMap<String, Object> mce = metricCalcEssentials.get(mk);
				if (mce == null) { // If it's null, see if it's in the DB
					mce = QueryManager.getMetricCalcEssentials(mk);
					if (mce == null) { // If it's not in the DB, make a new one
						mce = new HashMap<String, Object>();
					}
				}
				
				// Go through the MetricSequence, only calculating values for the ones that don't already have them.
				int index = 0;
				boolean last = false;
				for (Metric metric : ms) {
					if (index == ms.size() - 1) {
						last = true;
					}
					if (!metric.calculated) {
						// Need to check to make sure that the Metric Calc Essential contains values for the bar right before this metric.
						Calendar mceStart = (Calendar)mce.get("start");
						if (mceStart != null) {
							Calendar mceEnd = CalendarUtils.getBarEnd(mceStart, metric.duration);
//							System.out.println("Comparing " + mceEnd.getTime().toString() + "     to     " + metric.start.getTime().toString());
							if (!CalendarUtils.areSame(mceEnd, metric.start)) {
								// This is bad.  I'm somehow trying to calculate a metric for a bar that I don't have a last MCE for.
								throw new Exception("Trying to calcualte a metric for a bar that doesn't have a previous MCE.");
							}
						}
						
						// It has successfully gotten past the previous MCE timing check.  We have nothing to worry about and can proceed calculating metrics.
						switch (metric.name) {
							// DV EMA
							case "dv10ema":
//								System.out.println("Filling in dv10ema at                             " + metric.start.getTime().toString());
								MetricsCalculator.fillInWeightedDVEMA(mce, last, metric, 10);
								break;
							case "dv25ema":
								MetricsCalculator.fillInWeightedDVEMA(mce, last, metric, 25);
								break;
							case "dv50ema":
								MetricsCalculator.fillInWeightedDVEMA(mce, last, metric, 50);
								break;
							case "dv75ema":
								MetricsCalculator.fillInWeightedDVEMA(mce, last, metric, 75);
								break;
								
						}
					}
					index++;
				}
				
				// Save the MetricCalcEssentials to the DB
				if (mce != null && mce.size() > 0) {
					QueryManager.insertOrUpdateIntoMetricCalcEssentials(mk, mce);
				}
				
				// Insert the MetricSequence
				QueryManager.insertOrUpdateIntoMetrics(ms);
				
				switch (mk.name) {

					// Other DV
					case "dv2":
						
						break;
					case "dvfading4":
						
						break;
						
					// RSI
					case "rsi2":
						
						break;
					case "rsi5":
											
						break;
					case "rsi14":
						
						break;
						
					// RSI Alpha
					case "rsi2alpha":
						
						break;
					case "rsi5alpha":
											
						break;
					case "rsi14alpha":
						
						break;
						
					// RSI Alpha
					case "rsi75ema":
						
						break;
					case "rsi50ema":
											
						break;
					case "rsi25ema":
						
						break;
					case "rsi10ema":
						
						break;
					
						// MFI
					case "mfi2":
						
						break;
					case "mfi5":
											
						break;
					case "mfi14":
						
						break;
						
					// Consecutive Bars
					case "consecutiveupdays":
						
						break;
					case "consecutivedowndays":
						
						break;
						
					// Price Boll
					case "priceboll20":
						
						break;
					case "priceboll50":
											
						break;
					case "priceboll100":
						
						break;
					case "priceboll200":
						
						break;	
						
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
						
					// Williams R
					case "williamsr10":
						
						break;
					case "williamsr20":
											
						break;
					case "williamsr50":
						
						break;

					// Williams R Alpha
					case "williamsralpha10":
						
						break;
					case "williamsralpha20":
											
						break;
					case "williamsralpha50":
						
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
						
					// CCI
					case "cci10":
						
						break;
					case "cci20":
											
						break;
					case "cci40":
						
						break;
						
					// Other
					case "psar":
						
						break;
					case "ultimateoscillator":
											
						break;
					case "aroonoscillator":
						
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
						
						// Other DV
						if (metric.equals("dv2")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDV2(ms));
						}
						if (metric.equals("dvfading4")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDVFading4(ms));
						}
						
						// RSI
						if (metric.equals("rsi2")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(ms, 2));
						}
						if (metric.equals("rsi5")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(ms, 5));
						}
						if (metric.equals("rsi14")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(ms, 14));
						}
						
						// RSI Alpha
						if (metric.equals("rsi2alpha")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(ms, 2));
						}
						if (metric.equals("rsi5alpha")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(ms, 5));
						}
						if (metric.equals("rsi14alpha")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(ms, 14));
						}
						
						// RSI EMA
						if (metric.equals("rsi75ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 75));
						}
						if (metric.equals("rsi50ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 50));
						}
						if (metric.equals("rsi25ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 25));
						}
						if (metric.equals("rsi10ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(ms, 10));
						}

						// DVOL
						if (metric.equals("dvol75ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 75));	
						}
						if (metric.equals("dvol50ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 50));	
						}
						if (metric.equals("dvol25ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 25));	
						}
						if (metric.equals("dvol10ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(ms, 10));	
						}

						// Breakout
						if (metric.equals("breakout20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 20));	
						}
						if (metric.equals("breakout50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 50));	
						}
						if (metric.equals("breakout100")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 100));	
						}
						if (metric.equals("breakout200")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(ms, 200));	
						}
						
						// Streaks
						if (metric.equals("consecutiveupdays")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInUpStreaks(ms));
						}
						if (metric.equals("consecutivedowndays")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDownStreaks(ms));
						}
							
						// Price Boll
						if (metric.equals("priceboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 20));
						}
						if (metric.equals("priceboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 50));
						}
						if (metric.equals("priceboll100")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 100));
						}
						if (metric.equals("priceboll200")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(ms, 200));
						}

						// Gap Boll
						if (metric.equals("gapboll10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(ms, 10));
						}
						if (metric.equals("gapboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(ms, 20));
						}
						if (metric.equals("gapboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(ms, 50));
						}

						// Intraday Boll
						if (metric.equals("intradayboll10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(ms, 10));
						}
						if (metric.equals("intradayboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(ms, 20));
						}
						if (metric.equals("intradayboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(ms, 50));
						}
						
						// Volume Boll
						if (metric.equals("volumeboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 20));
						}
						if (metric.equals("volumeboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 50));
						}
						if (metric.equals("volumeboll100")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 100));
						}
						if (metric.equals("volumeboll200")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(ms, 200));
						}
						
						// Williams R
						if (metric.equals("williamsr10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(ms, 10));
						}
						if (metric.equals("williamsr20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(ms, 20));
						}
						if (metric.equals("williamsr50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(ms, 50));
						}
						
						// Williams R Alpha
						if (metric.equals("williamsralpha10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(ms, 10));
						}
						if (metric.equals("williamsralpha20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(ms, 20));
						}
						if (metric.equals("williamsralpha50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(ms, 50));
						}

						// MACD
						if (metric.equals("macd12_26_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(ms, 12, 26, 9));
						}
						if (metric.equals("macd20_40_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(ms, 20, 40, 9));
						}
						if (metric.equals("macd40_80_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(ms, 40, 80, 9));
						}
						
						// MACD Divergence
						if (metric.equals("macddivergence12_26_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(ms, 12, 26, 9));
						}
						if (metric.equals("macddivergence20_40_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(ms, 20, 40, 9));
						}
						if (metric.equals("macddivergence40_80_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(ms, 40, 80, 9));
						}
						
						// MFI
						if (metric.equals("mfi2")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(ms, 2));
						}
						if (metric.equals("mfi5")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(ms, 5));
						}
						if (metric.equals("mfi14")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(ms, 14));
						}
						
						// PSAR
						if (metric.equals("psar")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPSAR(ms));
						}
						
						// Aroon
						if (metric.equals("aroonoscillator")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInAroonOscillator(ms));
						}
						
						// Ultimate
						if (metric.equals("ultimateoscillator")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInUltimateOscillator(ms));
						}
						
						// CCI
						if (metric.equals("cci10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(ms, 10));
						}
						if (metric.equals("cci20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(ms, 20));
						}
						if (metric.equals("cci40")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(ms, 40));
						}
						
					} // End for metricSequences
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}