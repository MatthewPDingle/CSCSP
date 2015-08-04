package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import constants.Constants;
import data.Metric;
import dbio.QueryManager;

public class MetricsCalculatorRealtime {
	
	public static void calculateMetricsRealtime(ArrayList<String> metrics, ArrayList<String[]> durationSymbols) {
		try {
			ArrayList<LinkedList<Metric>> metricSequences = QueryManager.loadMetricSequencesForRealtimeUpdates(durationSymbols);
			Calendar maxStartFromBar = QueryManager.getMaxStartFromBar();
			
			// Unfortunately we have to go through all the metrics to see which ones need to be run
			for (String metric:Constants.METRICS) {
				if (metrics.contains(metric)) {
					for (LinkedList<Metric> metricSequence:metricSequences) {
						// AV EMA
						if (metric.equals("av10ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedAVEMA(metricSequence, 10));
						}
						if (metric.equals("av25ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedAVEMA(metricSequence, 25));
						}
						if (metric.equals("av50ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedAVEMA(metricSequence, 50));
						}
						if (metric.equals("av75ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedAVEMA(metricSequence, 75));
						}
								
						// DV EMA
						if (metric.equals("dv10ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVEMA(metricSequence, 10));
						}
						if (metric.equals("dv25ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVEMA(metricSequence, 25));
						}
						if (metric.equals("dv50ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVEMA(metricSequence, 50));
						}
						if (metric.equals("dv75ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVEMA(metricSequence, 75));
						}
						
						// Other DV
						if (metric.equals("dv2")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDV2(metricSequence));
						}
						if (metric.equals("dvfading4")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDVFading4(metricSequence));
						}
						
						// RSI
						if (metric.equals("rsi2")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(metricSequence, 2));
						}
						if (metric.equals("rsi5")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(metricSequence, 5));
						}
						if (metric.equals("rsi14")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSI(metricSequence, 14));
						}
						
						// RSI Alpha
						if (metric.equals("rsi2alpha")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(metricSequence, 2));
						}
						if (metric.equals("rsi5alpha")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(metricSequence, 5));
						}
						if (metric.equals("rsi14alpha")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInRSIAlpha(metricSequence, 14));
						}
						
						// RSI EMA
						if (metric.equals("rsi75ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(metricSequence, 75));
						}
						if (metric.equals("rsi50ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(metricSequence, 50));
						}
						if (metric.equals("rsi25ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(metricSequence, 25));
						}
						if (metric.equals("rsi10ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedRSI(metricSequence, 10));
						}

						// DVOL
						if (metric.equals("dvol75ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(metricSequence, 75));	
						}
						if (metric.equals("dvol50ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(metricSequence, 50));	
						}
						if (metric.equals("dvol25ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(metricSequence, 25));	
						}
						if (metric.equals("dvol10ema")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWeightedDVol(metricSequence, 10));	
						}

						// Breakout
						if (metric.equals("breakout20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(metricSequence, 20));	
						}
						if (metric.equals("breakout50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(metricSequence, 50));	
						}
						if (metric.equals("breakout100")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(metricSequence, 100));	
						}
						if (metric.equals("breakout200")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInBreakouts(metricSequence, 200));	
						}
						
						// Streaks
						if (metric.equals("consecutiveupdays")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInUpStreaks(metricSequence));
						}
						if (metric.equals("consecutivedowndays")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInDownStreaks(metricSequence));
						}
							
						// Price Boll
						if (metric.equals("priceboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(metricSequence, 20));
						}
						if (metric.equals("priceboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(metricSequence, 50));
						}
						if (metric.equals("priceboll100")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(metricSequence, 100));
						}
						if (metric.equals("priceboll200")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPriceBoll(metricSequence, 200));
						}

						// Gap Boll
						if (metric.equals("gapboll10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(metricSequence, 10));
						}
						if (metric.equals("gapboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(metricSequence, 20));
						}
						if (metric.equals("gapboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInGapBoll(metricSequence, 50));
						}

						// Intraday Boll
						if (metric.equals("intradayboll10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(metricSequence, 10));
						}
						if (metric.equals("intradayboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(metricSequence, 20));
						}
						if (metric.equals("intradayboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInIntradayBoll(metricSequence, 50));
						}
						
						// Volume Boll
						if (metric.equals("volumeboll20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(metricSequence, 20));
						}
						if (metric.equals("volumeboll50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(metricSequence, 50));
						}
						if (metric.equals("volumeboll100")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(metricSequence, 100));
						}
						if (metric.equals("volumeboll200")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInVolumeBoll(metricSequence, 200));
						}
						
						// Williams R
						if (metric.equals("williamsr10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(metricSequence, 10));
						}
						if (metric.equals("williamsr20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(metricSequence, 20));
						}
						if (metric.equals("williamsr50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsR(metricSequence, 50));
						}
						
						// Williams R Alpha
						if (metric.equals("williamsralpha10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(metricSequence, 10));
						}
						if (metric.equals("williamsralpha20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(metricSequence, 20));
						}
						if (metric.equals("williamsralpha50")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInWilliamsRAlpha(metricSequence, 50));
						}

						// MACD
						if (metric.equals("macd12_26_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(metricSequence, 12, 26, 9));
						}
						if (metric.equals("macd20_40_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(metricSequence, 20, 40, 9));
						}
						if (metric.equals("macd40_80_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACD(metricSequence, 40, 80, 9));
						}
						
						// MACD Divergence
						if (metric.equals("macddivergence12_26_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(metricSequence, 12, 26, 9));
						}
						if (metric.equals("macddivergence20_40_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(metricSequence, 20, 40, 9));
						}
						if (metric.equals("macddivergence40_80_9")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMACDDivergence(metricSequence, 40, 80, 9));
						}
						
						// MFI
						if (metric.equals("mfi2")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(metricSequence, 2));
						}
						if (metric.equals("mfi5")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(metricSequence, 5));
						}
						if (metric.equals("mfi14")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInMFI(metricSequence, 14));
						}
						
						// PSAR
						if (metric.equals("psar")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInPSAR(metricSequence));
						}
						
						// Aroon
						if (metric.equals("aroonoscillator")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInAroonOscillator(metricSequence));
						}
						
						// Ultimate
						if (metric.equals("ultimateoscillator")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInUltimateOscillator(metricSequence));
						}
						
						// CCI
						if (metric.equals("cci10")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(metricSequence, 10));
						}
						if (metric.equals("cci20")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(metricSequence, 20));
						}
						if (metric.equals("cci40")) {
							QueryManager.insertRealtimeMetrics(maxStartFromBar, MetricsCalculator.fillInCCI(metricSequence, 40));
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