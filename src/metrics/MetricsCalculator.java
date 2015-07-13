package metrics;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import utils.ConnectionSingleton;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import constants.Constants;
import dbio.QueryManager;

public class MetricsCalculator {

	private static MetricSequenceDataSingleton msds = MetricSequenceDataSingleton.getInstance();
	private static String metricSpanInDays = "400";
	private static final int NUM_THREADS = 5;
	
	public static void main (String[] args) {
		try {
			System.out.println("Dropping Metric Table");
//			QueryManager.dropMetricTable();
			
			System.out.println("Creating Metric Table");
//			QueryManager.createMetricTable();
			
			System.out.println("Loading Metric Sequence Data");
			MetricsCalculator mc = new MetricsCalculator();
			mc.loadMetricSequenceData();
			
			System.out.println("Calculating Metrics");
			ArrayList<MetricsCalculatorThreadHelper> threads = new ArrayList<MetricsCalculatorThreadHelper>();
			
			for (int n = 0; n < NUM_THREADS ; n++) {
				MetricsCalculatorThreadHelper thread = new MetricsCalculatorThreadHelper();
				threads.add(thread);
				thread.start();
			}
			for (MetricsCalculatorThreadHelper thread:threads) {
				if (thread != null)
					thread.join();
			}
			
			System.out.println("Creating Metric Index");
//			QueryManager.createMetricTableIndexes();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadMetricSequenceData() {
		try {
			String q1 = "SELECT symbol, to_char(MAX(start::date) + INTEGER '-" + metricSpanInDays + "', 'YYYY-MM-DD') AS baseDate FROM " + Constants.BAR_TABLE + " WHERE symbol = 'bitfinexUSD' GROUP BY symbol";
			Connection c1 = ConnectionSingleton.getInstance().getConnection();
			Statement s1 = c1.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs1 = s1.executeQuery(q1);
			
			Statement s2 = c1.createStatement();
			while (rs1.next()) {
				String symbol = rs1.getString("symbol");
				String baseDate = rs1.getString("baseDate"); // X days before latest date

				String alphaComparison = "SPY";
				if (symbol.equals("bitfinexUSD")) {
					alphaComparison = "bitfinexUSD";
				}
				if (symbol.equals("bitstampUSD")) {
					alphaComparison = "bitstampUSD";
				}
				if (symbol.equals("btceUSD")) {
					alphaComparison = "btceUSD";
				}
				if (symbol.equals("btcnCNY")) {
					alphaComparison = "btcnCNY";
				}
				if (symbol.equals("krakenEUR")) {
					alphaComparison = "krakenEUR";
				}
				if (symbol.equals("okcoinCNY")) {
					alphaComparison = "okcoinCNY";
				}
				
				// Fill a "metricSequence" with the price data for the last X days + however many days I need stats for
				String q2 = "SELECT r.*, r2.close AS spyadjclose, r2.change AS spychange " +
							"FROM bar r " +
							"INNER JOIN bar r2 " +
							"ON r.start = r2.start AND r2.symbol = '" + alphaComparison + "' " +
							"WHERE r.symbol = '" + symbol + "' " +
							"AND r.start >= '" + baseDate + "' " +
							"ORDER BY start ASC";
					
				LinkedList<Metric> metricSequence = new LinkedList<Metric>();
				ResultSet rs2 = s2.executeQuery(q2);
				while (rs2.next()) {
					Timestamp dStart = rs2.getTimestamp("start");
					Calendar start = Calendar.getInstance();
					start.setTime(dStart);
					start.set(Calendar.SECOND, 0);
					Timestamp dEnd = rs2.getTimestamp("end");
					Calendar end = Calendar.getInstance();
					end.setTime(dEnd);
					end.set(Calendar.SECOND, 0);
					String duration = rs2.getString("duration");
					long volume = rs2.getLong("volume");
					float adjOpen = rs2.getFloat("open");
					float adjClose = rs2.getFloat("close");
					float adjHigh = rs2.getFloat("high");
					float adjLow = rs2.getFloat("low");	
					float spyAdjClose = rs2.getFloat("spyadjclose");
					float spyChange = rs2.getFloat("spychange");
					float gap = rs2.getFloat("gap");
					float change = rs2.getFloat("change");
					
					metricSequence.add(new Metric(symbol, start, end, duration, volume, adjOpen, adjClose, adjHigh, adjLow, gap, change, spyAdjClose, spyChange));
				}
				rs2.close();
				msds.addMetricSequence(metricSequence);
			}
			rs1.close();
			s1.close();
			s2.close();
			c1.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Normalizes the metric values so that they range from 0 to 100.
	 * 
	 * This initial implementation is going to be inefficient as fuck.
	 * 
	 * @param metricSequence
	 */
	public static void normalizeMetricValues(LinkedList<Metric> metricSequence) {
		// Get the min and max denormalized values first
		float minValue = 1000000000f;
		float maxValue = -1000000000f;
		for (Metric metric:metricSequence) {
			float value = metric.getValue();
			if (value < minValue) {
				minValue = value;
			}
			if (value > maxValue) {
				maxValue = value;
			}
		}
		
		// Normalize based on the range
		float denormalizedRange = maxValue - minValue;
		float scaleFactor = 100f / denormalizedRange;
		for (Metric metric:metricSequence) {
			// Shift unscaled values so the min becomes zero, then apply scale
			float value = metric.getValue();
			float zeroBasedValue = value - minValue;
			float normalizedValue = zeroBasedValue * scaleFactor;
			metric.setValue(normalizedValue);
			System.out.println(normalizedValue);
		}
	}
	
	/**
	 * Related to DV, AV is exponentially weighted with a weight of parameter "weight"
	 * 
	 * @param metricSequence
	 * @param weight
	 * @return
	 */
	public static LinkedList<Metric> fillInWeightedAVEMA(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float yesterdaysAV = 0;
		float yesterdaysAdjClose = 0;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjClose = metric.getAdjClose();
	  		
			if (c > 1) {
				if (yesterdaysAdjClose > adjHigh) {
					adjHigh = yesterdaysAdjClose;
				}
				if (yesterdaysAdjClose < adjLow) {
					adjLow = yesterdaysAdjClose;
				}
				
				float todaysValue = adjClose / ((adjHigh + adjLow) / 2f);
				float todaysAV = (todaysValue - 1f) * 100f;
			  	if (c > 2) {
			  		todaysAV = ((todaysAV * weight / 100f) + (yesterdaysAV * (1 - (weight / 100f))));
			  	}

			  	// Set this day's AVEMA value and add it to the new sequence
			  	if (c >= 10) {
			  		metric.setValue(todaysAV);
			  	}
			  	else {
			  		metric.setValue(null);
			  	}
			  	
			  	yesterdaysAV = todaysAV;
			}
			metric.setName("av" + weight + "ema");
			
		  	yesterdaysAdjClose = adjClose;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	/**
	 * Related to DV, BV is exponentially weighted with a weight of parameter "weight"
	 * 
	 * @param metricSequence
	 * @param weight
	 * @return
	 */
	public static LinkedList<Metric> fillInWeightedBVEMA(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float yesterdaysBV = 0;
		float yesterdaysAdjClose = 0;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjClose = metric.getAdjClose();
	  		
			if (c > 1) {
				if (yesterdaysAdjClose > adjHigh) {
					adjHigh = yesterdaysAdjClose;
				}
				if (yesterdaysAdjClose < adjLow) {
					adjLow = yesterdaysAdjClose;
				}
				
				float todaysValue = adjClose / ((adjHigh + adjLow + yesterdaysAdjClose) / 3f);
				float todaysBV = (todaysValue - 1f) * 100f;
			  	if (c > 2) {
			  		todaysBV = ((todaysBV * weight / 100f) + (yesterdaysBV * (1 - (weight / 100f))));
			  	}

			  	// Set this day's BVEMA value and add it to the new sequence
			  	if (c >= 10) {
			  		metric.setValue(todaysBV);
			  	}
			  	else {
			  		metric.setValue(null);
			  	}
			  	
			  	yesterdaysBV = todaysBV;
			}
			metric.setName("bv" + weight + "ema");
			
		  	yesterdaysAdjClose = adjClose;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	/**
	 * Related to DV, CV is exponentially weighted with a weight of parameter "weight"
	 * 
	 * @param metricSequence
	 * @param weight
	 * @return
	 */
	public static LinkedList<Metric> fillInWeightedCVEMA(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float yesterdaysCV = 0;
		float yesterdaysAdjClose = 0;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjClose = metric.getAdjClose();
	  		
			if (c > 1) {
				if (yesterdaysAdjClose > adjHigh) {
					adjHigh = yesterdaysAdjClose;
				}
				if (yesterdaysAdjClose < adjLow) {
					adjLow = yesterdaysAdjClose;
				}
				
				float todaysValue = adjClose / ((adjHigh + adjLow + yesterdaysAdjClose + adjClose) / 4f);
				float todaysCV = (todaysValue - 1f) * 100f;
			  	if (c > 2) {
			  		todaysCV = ((todaysCV * weight / 100f) + (yesterdaysCV * (1 - (weight / 100f))));
			  	}

			  	// Set this day's CVEMA value and add it to the new sequence
			  	if (c >= 10) {
			  		metric.setValue(todaysCV);
			  	}
			  	else {
			  		metric.setValue(null);
			  	}
			  	
			  	yesterdaysCV = todaysCV;
			}
			metric.setName("cv" + weight + "ema");
			
		  	yesterdaysAdjClose = adjClose;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	/**
	 * A variation of DV that is exponentially weighted with a weight of parameter "weight"
	 * 
	 * @param metricSequence
	 * @param weight
	 * @return
	 */
	public static LinkedList<Metric> fillInWeightedDVEMA(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float yesterdaysDV = 0;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjClose = metric.getAdjClose();
	  		
			float todaysValue = adjClose / ((adjHigh + adjLow) / 2f);
			float todaysDV = (todaysValue - 1f) * 100f;
		  	if (c > 1) {
		  		todaysDV = ((todaysDV * weight / 100f) + (yesterdaysDV * (1 - (weight / 100f))));
		  	}

		  	// Set this day's DV2EMA value and add it to the new sequence
		  	metric.setName("dv" + weight + "ema");
		  	if (c >= 10) {
		  		metric.setValue(todaysDV);
		  	}
		  	else {
		  		metric.setValue(null);
		  	}
		  	
		  	yesterdaysDV = todaysDV;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	/**
	 * Related to DV, EV is exponentially weighted with a weight of parameter "weight"
	 * 
	 * @param metricSequence
	 * @param weight
	 * @return
	 */
	public static LinkedList<Metric> fillInWeightedEVEMA(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float yesterdaysEV = 0;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjOpen = metric.getAdjOpen();
			float adjClose = metric.getAdjClose();
	  		
			float todaysValue = adjClose / ((adjHigh + adjLow + adjOpen) / 3f);
			float todaysEV = (todaysValue - 1f) * 100f;
		  	if (c > 1) {
		  		todaysEV = ((todaysEV * weight / 100f) + (yesterdaysEV * (1 - (weight / 100f))));
		  	}

		  	// Set this day's EVEMA value and add it to the new sequence
		  	metric.setName("ev" + weight + "ema");
		  	if (c >= 10) {
		  		metric.setValue(todaysEV);
		  	}
		  	else {
		  		metric.setValue(null);
		  	}
		  	
		  	yesterdaysEV = todaysEV;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	/**
	 * Related to DV, FV is exponentially weighted with a weight of parameter "weight"
	 * 
	 * @param metricSequence
	 * @param weight
	 * @return
	 */
	public static LinkedList<Metric> fillInWeightedFVEMA(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float yesterdaysFV = 0;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjOpen = metric.getAdjOpen();
			float adjClose = metric.getAdjClose();
	  		
			float todaysValue = adjClose / ((adjHigh + adjLow + adjOpen + adjClose) / 4f);
			float todaysFV = (todaysValue - 1f) * 100f;
		  	if (c > 1) {
		  		todaysFV = ((todaysFV * weight / 100f) + (yesterdaysFV * (1 - (weight / 100f))));
		  	}

		  	// Set this day's FVEMA value and add it to the new sequence
		  	metric.setName("fv" + weight + "ema");
		  	if (c >= 10) {
		  		metric.setValue(todaysFV);
		  	}
		  	else {
		  		metric.setValue(null);
		  	}
		  	
		  	yesterdaysFV = todaysFV;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	/**
	 * Same as regular MACD Divergence, but I normalize it as a percentage of stock price
	 * 
	 * @param metricSequence
	 * @param shortPeriod
	 * @param longPeriod
	 * @param macdPeriod
	 * @return
	 */
	public static LinkedList<Metric> fillInMACDDivergence(LinkedList<Metric> metricSequence, int shortPeriod, int longPeriod, int macdPeriod) {
		// Initialize Variables
		float previousShortPeriodEMA = -1f;
		float previousLongPeriodEMA = -1f;
		float previousMACDPeriodEMA = -1f;
		float shortPeriodEMAMultiplier = (2f / (float)(shortPeriod + 1));
		float longPeriodEMAMultiplier = (2f / (float)(longPeriod + 1));
		float macdPeriodEMAMultiplier = (2f / (float)(macdPeriod + 1));
		int c = 1;
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			float shortPeriodEMA = -1f;
			float longPeriodEMA = -1f;
			float macdPeriodEMA = -1f;
			
			if (c == 1) {
				previousShortPeriodEMA = adjClose;
				previousLongPeriodEMA = adjClose;
			}
			shortPeriodEMA = ((adjClose - previousShortPeriodEMA) * shortPeriodEMAMultiplier) + previousShortPeriodEMA;
			longPeriodEMA = ((adjClose - previousLongPeriodEMA) * longPeriodEMAMultiplier) + previousLongPeriodEMA;
			float macd = shortPeriodEMA - longPeriodEMA;
			float macdAsPercentOfPrice = macd / adjClose * 100f;
			
			if (c == 1) {
				previousMACDPeriodEMA = macdAsPercentOfPrice;
			}
			macdPeriodEMA = ((macdAsPercentOfPrice - previousMACDPeriodEMA) * macdPeriodEMAMultiplier) + previousMACDPeriodEMA;
			float divergence = macdAsPercentOfPrice - macdPeriodEMA;
			
			// Set the MACD values and add the day to the new day sequence
			if (c >= longPeriod) {
				metric.setValue(divergence);
			}
			else {
				metric.setValue(null);
			}
			metric.setName("macddivergence" + shortPeriod + "_" + longPeriod + "_" + macdPeriod);
			
			previousShortPeriodEMA = shortPeriodEMA;
			previousLongPeriodEMA = longPeriodEMA;
			previousMACDPeriodEMA = macdPeriodEMA;
			c++;
		}
		
		return metricSequence;
	}
	
	/**
	 * Same as regular MACD, but I normalize it as a percentage of stock price
	 * 
	 * @param metricSequence
	 * @param shortPeriod
	 * @param longPeriod
	 * @param macdPeriod
	 * @return
	 */
	public static LinkedList<Metric> fillInMACD(LinkedList<Metric> metricSequence, int shortPeriod, int longPeriod, int macdPeriod) {
		// Initialize Variables
		float previousShortPeriodEMA = -1f;
		float previousLongPeriodEMA = -1f;
		float shortPeriodEMAMultiplier = (2f / (float)(shortPeriod + 1));
		float longPeriodEMAMultiplier = (2f / (float)(longPeriod + 1));
		int c = 1;
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			float shortPeriodEMA = -1f;
			float longPeriodEMA = -1f;
			
			if (c == 1) {
				previousShortPeriodEMA = adjClose;
				previousLongPeriodEMA = adjClose;
			}
			shortPeriodEMA = ((adjClose - previousShortPeriodEMA) * shortPeriodEMAMultiplier) + previousShortPeriodEMA;
			longPeriodEMA = ((adjClose - previousLongPeriodEMA) * longPeriodEMAMultiplier) + previousLongPeriodEMA;
			float macd = shortPeriodEMA - longPeriodEMA;
			float macdAsPercentOfPrice = macd / adjClose * 100f;

			// Set the MACD values and add the day to the new day sequence
			if (c >= longPeriod) {
				metric.setValue(macdAsPercentOfPrice);
			}
			else {
				metric.setValue(null);
			}
			metric.setName("macd" + shortPeriod + "_" + longPeriod + "_" + macdPeriod);
			
			previousShortPeriodEMA = shortPeriodEMA;
			previousLongPeriodEMA = longPeriodEMA;
			c++;
		}
		
		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInDVFading4(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		Metric yesterday = null;
		Metric yesterday2 = null;
		Metric yesterday3 = null;
		
		for (Metric metric:metricSequence) {
			float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjClose = metric.getAdjClose();
			
			if (yesterday3 != null) {
				float yesterdayAdjHigh = yesterday.getAdjHigh();
				float yesterdayAdjLow = yesterday.getAdjLow();
				float yesterdayAdjClose = yesterday.getAdjClose();
				float yesterday2AdjHigh = yesterday2.getAdjHigh();
				float yesterday2AdjLow = yesterday2.getAdjLow();
				float yesterday2AdjClose = yesterday2.getAdjClose();
				float yesterday3AdjHigh = yesterday3.getAdjHigh();
				float yesterday3AdjLow = yesterday3.getAdjLow();
				float yesterday3AdjClose = yesterday3.getAdjClose();
				
				float todaysValue = adjClose / ((adjHigh + adjLow) / 2f);
				float yesterdaysValue = yesterdayAdjClose / ((yesterdayAdjHigh + yesterdayAdjLow) / 2f);
				float yesterday2sValue = yesterday2AdjClose / ((yesterday2AdjHigh + yesterday2AdjLow) / 2f);
				float yesterday3sValue = yesterday3AdjClose / ((yesterday3AdjHigh + yesterday3AdjLow) / 2f);
				float dvFading4 = (((todaysValue * .4f) + (yesterdaysValue * .3f) + (yesterday2sValue * .2f) + (yesterday3sValue * .1f)) - 1f) * 100f;
				
				// Set the DVFading4 value and add the day to the new day sequence
				metric.setValue(dvFading4);
				metric.setName("dvfading4");
			}
			else {
				metric.setValue(null);
				metric.setName("dvfading4");
			}
			
			yesterday3 = yesterday2;
			yesterday2 = yesterday;
			yesterday = metric;
		}
		
		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInWeightedDVol(LinkedList<Metric> metricSequence, int weight) { 
		// Initialize Variables
		float yesterdaysDVol = 0f;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjClose = metric.getAdjClose();
	  		float adjOpen = metric.getAdjOpen();
	  		float adjHigh = metric.getAdjHigh();
	  		float adjLow = metric.getAdjLow();
	  		
	  		float todaysAvg = (adjClose + adjOpen + adjHigh + adjLow) / 4f;
	  		float todaysRange = adjHigh - adjLow;
	  		float todaysDVol = todaysRange / todaysAvg * 100f;
	  	
		  	if (c > 1) {
		  		todaysDVol = ((todaysDVol * weight / 100f) + (yesterdaysDVol * (1 - (weight / 100f))));
		  	}

		  	// Set this day's RSI value and add it to the new sequence
		  	if (c >= 10) {
			  	metric.setValue(todaysDVol);
			  	
		  	}
		  	else {
		  		metric.setValue(null);
		  	}
		  	metric.setName("dvol" + weight + "ema");
		  	
		  	yesterdaysDVol = todaysDVol;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInBreakouts(LinkedList<Metric> metricSequence, int period) { 
		// Initialize Variables
	  	LinkedList<Float> closes = new LinkedList<Float>();

	  	for (Metric metric:metricSequence) {
	  		float adjClose = metric.getAdjClose();
	  		if (closes.size() < period) {
	  			closes.add(adjClose);
	  			metric.setValue(0f);
	  		}

	  		else if (closes.size() == period) {
	  			float highestClose = closes.getFirst();
	  			float lowestClose = closes.getFirst();
	  			int numDaysSinceToday = 0;
	  			int highNumDaysSincePeriodStart = 0;
	  			int lowNumDaysSincePeriodStart = 0;
	  			for (Float close:closes) {
	  				if (close > highestClose) {
	  					highNumDaysSincePeriodStart = numDaysSinceToday;
	  					highestClose = close;
	  				}
	  				if (close < lowestClose) {
	  					lowNumDaysSincePeriodStart = numDaysSinceToday;
	  					lowestClose = close;
	  				}
	  				numDaysSinceToday++;
	  			}
	  			
	  			float breakout = 0f;
	  			if (adjClose > highestClose) {
	  				breakout = ((adjClose - highestClose) / highestClose * 100f) * (1 + ((period - highNumDaysSincePeriodStart) / 3f));
	  			}
	  			else if (adjClose < lowestClose) {
	  				breakout = ((adjClose - lowestClose) / lowestClose * 100f) * (1 + ((period - lowNumDaysSincePeriodStart) / 3f));
	  			}
	  			// Normalize the results a bit to bunch them mostly in a -1 to 1 range
	  			float breakoutABS = Math.abs(breakout);
	  			float breakoutABSp1 = breakoutABS + 1;
	  			float sign = Math.signum(breakout);
	  			float log = (float)Math.log10(breakoutABSp1);
	  			if (log > 1) log = 1;
	  			float adjustedBreakout = log * sign;
	  			
	  			
	  			metric.setValue(adjustedBreakout);
	  			
	  			// Toss the oldest, add the latest
	  			closes.remove();
	  			closes.add(adjClose);
	  		}

	  		metric.setName("breakout" + period);
	  	}
	
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInDV2(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		Metric yesterday = null;
		
		for (Metric metric:metricSequence) {
			float adjHigh = metric.getAdjHigh();
			float adjLow = metric.getAdjLow();
			float adjClose = metric.getAdjClose();
			
			if (yesterday != null) {
				float yesterdayAdjHigh = yesterday.getAdjHigh();
				float yesterdayAdjLow = yesterday.getAdjLow();
				float yesterdayAdjClose = yesterday.getAdjClose();
				
				float todaysValue = adjClose / ((adjHigh + adjLow) / 2f);
				float yesterdaysValue = yesterdayAdjClose / ((yesterdayAdjHigh + yesterdayAdjLow) / 2f);
				float dv2 = (((todaysValue + yesterdaysValue) / 2f) - 1f) * 100f;
				
				// Set the DV2 value and add the day to the new day sequence
				metric.setValue(dv2);
				metric.setName("dv2");
			}
			else {
				metric.setValue(null);
				metric.setName("dv2");
			}
			
			yesterday = metric;
		}
		
		return metricSequence;
	}
	
	/**
	 * Same definition of Williams % R, but I add 100 to it to give a range between 0-100 instead of -100-0
	 * 
	 * @param metricSequence
	 * @param period
	 * @return
	 */
	public static LinkedList<Metric> fillInWilliamsR(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();

			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.setValue(null);
		  		metric.setName("williamsr" + period);
		  	}
			else {
				periodsAdjCloses.add(adjClose);
				float periodsHigh = 0f;
				float periodsLow = 1000000f;
				for (Float p:periodsAdjCloses) {
					if (p > periodsHigh) {
						periodsHigh = p;
					}
					if (p < periodsLow) {
						periodsLow = p;
					}
				}
				
				float odin = periodsHigh - adjClose;
				float shiva = periodsLow - periodsHigh;
				float william = (odin / shiva * 100f) + 100f;

				// Set the WilliamsR value and add the day to the new day sequence
				metric.setValue(william);
				metric.setName("williamsr" + period);
				
				periodsAdjCloses.remove();
			}
		}
		
		return metricSequence;
	}
	
	/**
	 * Same definition of Williams % R, but I add 100 to it to give a range between 0-100 instead of -100-0
	 * 
	 * @param metricSequence
	 * @param period
	 * @return
	 */
	public static LinkedList<Metric> fillInWilliamsRAlpha(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		LinkedList<Float> periodsSPYAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			float spyAdjClose = metric.getAdjClose();

			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		periodsSPYAdjCloses.add(spyAdjClose);
		  		metric.setValue(null);
		  		metric.setName("williamsralpha" + period);
		  	}
			else {
				periodsAdjCloses.add(adjClose);
				periodsSPYAdjCloses.add(spyAdjClose);
				
				float periodsHigh = 0f;
				float periodsLow = 1000000f;
				for (Float p:periodsAdjCloses) {
					if (p > periodsHigh) {
						periodsHigh = p;
					}
					if (p < periodsLow) {
						periodsLow = p;
					}
				}
				
				float spyPeriodsHigh = 0f;
				float spyPeriodsLow = 1000000f;
				for (Float p:periodsSPYAdjCloses) {
					if (p > spyPeriodsHigh) {
						spyPeriodsHigh = p;
					}
					if (p < spyPeriodsLow) {
						spyPeriodsLow = p;
					}
				}
				
				// WilliamsR for the stock
				float a1 = periodsHigh - adjClose;
				float b1 = periodsLow - periodsHigh;
				float william1 = (a1 / b1 * 100f) + 100f;
				
				// WilliamsR for the SPY
				float a2 = spyPeriodsHigh - spyAdjClose;
				float b2 = spyPeriodsLow - spyPeriodsHigh;
				float william2 = (a2 / b2 * 100f) + 100f;

				// Williams adjusted for Alpha (SPY)
				float a3 = 50 - Math.abs(50 - william1);
				float d3 = (william1 - william2) / 100f;
				float b3 = a3 * d3;
				float william3 = william1 + b3;
				
				// Set the WilliamsR value and add the day to the new day sequence
				metric.setValue(william3);
				metric.setName("williamsralpha" + period);
				
				periodsAdjCloses.remove();
			}
		}
		
		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInWeightedRSI(LinkedList<Metric> metricSequence, int weight) {
		// Initialize Variables
		float lastAvgUp = -1f; 
	  	float lastAvgDown = -1f;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float change = metric.getChange();
	  		
		  	float up = (change > 0) ? change : 0;
		  	float down = (change < 0) ? -change : 0;
		  	float avgUp = up;
		  	float avgDown = down;
		  	if (c > 1) {
		  		avgUp = (up * weight / 100f) + (lastAvgUp * (1 - (weight / 100f)));
		  		avgDown = (down * weight / 100f) + (lastAvgDown * (1 - (weight / 100f)));
		  	}
		  	
		  	float rsi = 100f;
		  	if (avgDown != 0) {
		  		float rs = (avgUp / avgDown) + 1f;
		  		rsi = 100f - (100f / rs);
		  	}

		  	// Set this day's RSI value and add it to the new sequence
		  	if (c >= 10) {
			  	metric.setValue(rsi);
		  	}
		  	else {
		  		metric.setValue(null);
		  	}
		  	metric.setName("rsi" + weight + "ema");
		  	
		  	lastAvgUp = avgUp;
		  	lastAvgDown = avgDown;
		  	c++;
	  	}
	  	
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInRSI(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
	  	LinkedList<Float> changes = new LinkedList<Float>();
	  	
	  	for (Metric metric:metricSequence) {
	  		float change = metric.getChange();
	  		changes.add(change);
	  		
	  		if (changes.size() == period) {
	  			float upSum = 0f;
	  			float downSum = 0f;
	  			for (Float ch:changes) {
	  				if (ch > 0) upSum += ch;
	  				else downSum += -ch;
	  			}
	  			float avgUp = upSum / (float)period;
	  			float avgDown = downSum / (float)period;
	  			
	  			float rsi = 100f;
			  	if (avgDown != 0) {
			  		float rs = (avgUp / avgDown) + 1f;
				  	rsi = 100f - (100f / rs);
			  	}
			  	
			  	metric.setValue(rsi);
	  			
	  			changes.remove();
	  		}
	  		else {
	  			metric.setValue(null);
	  		}
	  		metric.setName("rsi" + period);
	  	}
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInMFI(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
	  	LinkedList<Double> moneyFlows = new LinkedList<Double>();
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjClose = metric.getAdjClose();
	  		float adjHigh = metric.getAdjHigh();
	  		float adjLow = metric.getAdjLow();
	  		float change = metric.getChange();
	  		float sign = 1;
	  		if (change < 0) sign = -1;
	  		float typicalPrice = (adjClose + adjHigh + adjLow) / 3f;
	  		float volume = metric.getVolume();
	  		
	  		double moneyflow = typicalPrice * volume * sign;
	  		moneyFlows.add(moneyflow);
	  		
	  		if (moneyFlows.size() == period) {
	  			double upSum = 0f;
	  			double downSum = 0f;
	  			for (Double mf:moneyFlows) {
	  				if (mf > 0) upSum += mf;
	  				else downSum += -mf;
	  			}
	  			double avgUp = upSum / (double)period;
	  			double avgDown = downSum / (double)period;
	  			
	  			float mfi = 100f;
			  	if (avgDown != 0) {
			  		double rs = (avgUp / avgDown) + 1f;
				  	mfi = 100f - (float)(100d / rs);
			  	}
			  	
			  	metric.setValue(mfi);
	  			
	  			moneyFlows.remove();
	  		}
	  		else {
	  			metric.setValue(null);
	  		}
	  		metric.setName("mfi" + period);
	  	}
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInPSAR(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		Core core = new Core();
		double[] highs = new double[metricSequence.size()];
		double[] lows = new double[metricSequence.size()];
		double[] output = new double[metricSequence.size()];
		
		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		
		int c = 0;
		for (Metric metric:metricSequence) {
			highs[c] = metric.getAdjHigh();
			lows[c] = metric.getAdjLow();
			c++;
		}
		
		RetCode retCode = core.sar(0, metricSequence.size() - 1, highs, lows, .02, .2, begin, length, output);
		if (retCode == RetCode.Success) { 
			int c2 = begin.value; 
			for (Metric metric:metricSequence) {
				metric.setName("psar");
				metric.setValue(null);
				if (metricSequence.indexOf(metric) >= begin.value) {
					float adjClose = metric.getAdjClose();
					float psar = (float)output[c2 - begin.value];
					float delta = psar - adjClose;
					float percentAboveOrBelowClose = delta / adjClose * 100f;
					metric.setValue(percentAboveOrBelowClose);
					c2++;
				}
			}
		}
		
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInUltimateOscillator(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		Core core = new Core();
		double[] highs = new double[metricSequence.size()];
		double[] lows = new double[metricSequence.size()];
		double[] closes = new double[metricSequence.size()];
		double[] output = new double[metricSequence.size()];
		
		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		
		int c = 0;
		for (Metric metric:metricSequence) {
			highs[c] = metric.getAdjHigh();
			lows[c] = metric.getAdjLow();
			closes[c] = metric.getAdjClose();
			c++;
		}
		
		RetCode retCode = core.ultOsc(0, metricSequence.size() - 1, highs, lows, closes, 4, 10, 25, begin, length, output);
		if (retCode == RetCode.Success) { 
			int c2 = begin.value; 
			for (Metric metric:metricSequence) {
				metric.setName("ultimateoscillator");
				metric.setValue(null);
				if (metricSequence.indexOf(metric) >= begin.value) {
					metric.setValue((float)output[c2 - begin.value]);
					c2++;
				}
			}
		}
		
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInAroonOscillator(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		Core core = new Core();
		double[] highs = new double[metricSequence.size()];
		double[] lows = new double[metricSequence.size()];
		double[] output = new double[metricSequence.size()];
		
		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		
		int c = 0;
		for (Metric metric:metricSequence) {
			highs[c] = metric.getAdjHigh();
			lows[c] = metric.getAdjLow();
			c++;
		}
		
		RetCode retCode = core.aroonOsc(0, metricSequence.size() - 1, highs, lows, 25, begin, length, output);
		if (retCode == RetCode.Success) { 
			int c2 = begin.value; 
			for (Metric metric:metricSequence) {
				metric.setName("aroonoscillator");
				metric.setValue(null);
				if (metricSequence.indexOf(metric) >= begin.value) {
					metric.setValue((float)output[c2 - begin.value]);
					c2++;
				}
			}
		}
		
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInCCI(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		Core core = new Core();

		double[] highs = new double[metricSequence.size()];
		double[] lows = new double[metricSequence.size()];
		double[] closes = new double[metricSequence.size()];
		double[] output = new double[metricSequence.size()];
		
		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		
		int c = 0;
		for (Metric metric:metricSequence) {
			highs[c] = metric.getAdjHigh();
			lows[c] = metric.getAdjLow();
			closes[c] = metric.getAdjClose();
			c++;
		}
		
		RetCode retCode = core.cci(0, metricSequence.size() - 1, highs, lows, closes, period, begin, length, output);
		if (retCode == RetCode.Success) { 
			int c2 = begin.value; 
			for (Metric metric:metricSequence) {
				metric.setName("cci" + period);
				metric.setValue(null);
				if (metricSequence.indexOf(metric) >= begin.value) {
					metric.setValue((float)(output[c2 - begin.value]) / 4f);
					c2++;
				}
			}
		}
		
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInBeta(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		Core core = new Core();

		double[] sp500closes = new double[metricSequence.size()];
		double[] closes = new double[metricSequence.size()];
		double[] output = new double[metricSequence.size()];
		
		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		
		int c = 0;
		for (Metric metric:metricSequence) {
			closes[c] = metric.getAdjClose();
			sp500closes[c] = metric.getSpyAdjClose();
			c++;
		}
		
		RetCode retCode = core.beta(0, metricSequence.size() - 1, sp500closes, closes, period, begin, length, output);
		if (retCode == RetCode.Success) { 
			int c2 = begin.value; 
			for (Metric metric:metricSequence) {
				metric.setName("beta" + period);
				metric.setValue(null);
				if (metricSequence.indexOf(metric) >= begin.value) {
					metric.setValue((float)(output[c2 - begin.value]));
					c2++;
				}
			}
		}
		
	  	return metricSequence;
	}
	
	public static LinkedList<Metric> fillInRSIAlpha(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
	  	LinkedList<Float> changes = new LinkedList<Float>();
	  	
	  	for (Metric metric:metricSequence) {
	  		float change = metric.getChange();
	  		float spyChange = metric.getSpyChange();
	  		float adjclose = metric.getAdjClose();
	  		float spyAdjClose = metric.getSpyAdjClose();
	  		
	  		// Normalize changes to perchanges
	  		float perchange = change / (adjclose - change) * 100;
	  		float spyperchange = spyChange / (spyAdjClose - spyChange) * 100;
	  		
	  		changes.add(perchange - spyperchange);
	  		
	  		if (changes.size() == period) {
	  			float upSum = 0f;
	  			float downSum = 0f;
	  			for (Float ch:changes) {
	  				if (ch > 0) upSum += ch;
	  				else downSum += -ch;
	  			}
	  			float avgUp = upSum / (float)period;
	  			float avgDown = downSum / (float)period;
	  			
	  			float rsi = 100f;
			  	if (avgDown != 0) {
			  		float rs = (avgUp / avgDown) + 1f;
				  	rsi = 100f - (100f / rs);
			  	}
			  	
			  	metric.setValue(rsi);
	  			
	  			changes.remove();
	  		}
	  		else {
	  			metric.setValue(null);
	  		}
	  		metric.setName("rsi" + period + "alpha");
	  	}
	  	return metricSequence;
	}
		
	public static LinkedList<Metric> fillInUpStreaks(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		float lastAdjClose = -1f;
		int consecutiveUpMetrics = 0;
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics++;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics = 0;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics = 0;
		  	}
			
			metric.setValue((float)consecutiveUpMetrics);
			metric.setName("consecutiveupdays");
			
		  	lastAdjClose = adjClose;
		}
		
		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInDownStreaks(LinkedList<Metric> metricSequence) {
		// Initialize Variables
		float lastAdjClose = -1f;
	    int consecutiveDownMetrics = 0;
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics = 0;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics++;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics = 0;
		  	}

			metric.setValue((float)consecutiveDownMetrics);
			metric.setName("consecutivedowndays");
			
		  	lastAdjClose = adjClose;
		}
		
		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInPriceDMAs(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();

			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.setValue(null);
		  		metric.setName("pricedma" + period);
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float priceSum = 0;
		  		for (Float price:periodsAdjCloses) {
		  			priceSum += price;
		  		}
		  		float dma = priceSum / (float)period;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.setValue(dma);
		  		metric.setName("pricedma" + period);
		  		
		  		periodsAdjCloses.remove();
		  	}
		}

		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInPriceBoll(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();

			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.setValue(null);
		  		metric.setName("priceboll" + period);
		  	}
		  	else {
		  		// DMA
		  		periodsAdjCloses.add(adjClose);
		  		float priceSum = 0;
		  		for (Float price:periodsAdjCloses) {
		  			priceSum += price;
		  		}
		  		float dma = priceSum / (float)period;
		  		
		  		// SD
		  		float periodsAdjClosesSum = 0;
		  		for (Float p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		float averagePrice = periodsAdjClosesSum / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// 
		  		float boll = (adjClose - dma) / sd;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.setValue(boll);
		  		metric.setName("priceboll" + period);
		  		
		  		periodsAdjCloses.remove();
		  	}
		}

		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInGapBoll(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodGPCs = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float gap = metric.getGap();
			float adjOpen = metric.getAdjOpen();
			float gpc = gap / (adjOpen - gap) * 100f;

			if (periodGPCs.size() < (period - 1)) {
		  		periodGPCs.add(gpc);
		  		metric.setValue(null);
		  		metric.setName("gapboll" + period);
		  	}
		  	else {
		  		// DMA
		  		periodGPCs.add(gpc);
		  		float gpcSum = 0;
		  		for (Float thisGPC:periodGPCs) {
		  			gpcSum += thisGPC;
		  		}
		  		float dma = gpcSum / (float)period;
		  		
		  		// SD
		  		float gpcSum2 = 0;
		  		for (Float thisGPC:periodGPCs) {
		  			gpcSum2 += thisGPC;
		  		}
		  		float averageGPC = gpcSum2 / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float thisGPC:periodGPCs) {
		  			sumOfDifferenceFromAverageSquares += ((thisGPC - averageGPC) * (thisGPC - averageGPC));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		float boll = (gpc - dma) / sd;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.setValue(boll);
		  		metric.setName("gapboll" + period);
		  		
		  		periodGPCs.remove();
		  	}
		}

		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInIntradayBoll(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodIDPCs = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float gap = metric.getGap();
			float change = metric.getChange();
			float adjOpen = metric.getAdjOpen();
			float idpc = (change - gap) / (adjOpen - gap) * 100f;

			if (periodIDPCs.size() < (period - 1)) {
		  		periodIDPCs.add(idpc);
		  		metric.setValue(null);
		  		metric.setName("intradayboll" + period);
		  	}
		  	else {
		  		// DMA
		  		periodIDPCs.add(idpc);
		  		float idpcSum = 0;
		  		for (Float thisIDPC:periodIDPCs) {
		  			idpcSum += thisIDPC;
		  		}
		  		float dma = idpcSum / (float)period;
		  		
		  		// SD
		  		float idpcSum2 = 0;
		  		for (Float thisIDPC:periodIDPCs) {
		  			idpcSum2 += thisIDPC;
		  		}
		  		float averageIDPC = idpcSum2 / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float thisIDPC:periodIDPCs) {
		  			sumOfDifferenceFromAverageSquares += ((thisIDPC - averageIDPC) * (thisIDPC - averageIDPC));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// 
		  		float boll = (idpc - dma) / sd;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.setValue(boll);
		  		metric.setName("intradayboll" + period);
		  		
		  		periodIDPCs.remove();
		  	}
		}

		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInVolumeBoll(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Long> periodsVolumes = new LinkedList<Long>();
		
		for (Metric metric:metricSequence) {
			long volume = metric.getVolume();

			if (periodsVolumes.size() < (period - 1)) {
		  		periodsVolumes.add(volume);
		  		metric.setValue(null);
		  		metric.setName("volumeboll" + period);
		  	}
		  	else {
		  		// DMA
		  		periodsVolumes.add(volume);
		  		long volumeSum = 0;
		  		for (Long price:periodsVolumes) {
		  			volumeSum += price;
		  		}
		  		long dma = volumeSum / period;
		  		
		  		// SD
		  		long periodsVolumesSum = 0;
		  		for (Long v:periodsVolumes) {
		  			periodsVolumesSum += v;
		  		}
		  		long averageVolume = periodsVolumesSum / period;
		  		long sumOfDifferenceFromAverageSquares = 0;
		  		for (Long v:periodsVolumes) {
		  			sumOfDifferenceFromAverageSquares += ((v - averageVolume) * (v - averageVolume));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// 
		  		float boll = (volume - dma) / sd;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.setValue(boll);
		  		metric.setName("volumeboll" + period);
		  		
		  		periodsVolumes.remove();
		  	}
		}

		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInVolumeDMAs(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Long> periodsVolumes = new LinkedList<Long>();
		
		for (Metric metric:metricSequence) {
			long volume = metric.getVolume();

			if (periodsVolumes.size() < (period - 1)) {
		  		periodsVolumes.add(volume);
		  		metric.setValue(null);
		  		metric.setName("volumedma" + period);
		  	}
		  	else {
		  		periodsVolumes.add(volume);
		  		long volumeSum = 0;
		  		for (Long price:periodsVolumes) {
		  			volumeSum += price;
		  		}
		  		long dma = volumeSum / period;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.setValue((float)dma);
		  		metric.setName("volumedma" + period);
		  		
		  		periodsVolumes.remove();
		  	}
		}

		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInPriceSDs(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.setValue(null);
		  		metric.setName("pricesd" + period);
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float periodsAdjClosesSum = 0;
		  		for (Float p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		float averagePrice = periodsAdjClosesSum / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.setValue(sd);
		  		metric.setName("pricesd" + period);
		
		  		periodsAdjCloses.remove();
		  	}
		}
		return metricSequence;
	}
	
	/**
	 * Standard Deviation as a percent of DMA.  I might use this for position sizing.
	 * 
	 * @param metricSequence
	 * @param period
	 * @return
	 */
	public static LinkedList<Metric> fillInMVOL(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.setValue(null);
		  		metric.setName("mvol" + period);
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float periodsAdjClosesSum = 0;
		  		for (Float p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		float averagePrice = periodsAdjClosesSum / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		float sdapodma = sd / averagePrice * 100;
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.setValue(sdapodma);
		  		metric.setName("mvol" + period);
		
		  		periodsAdjCloses.remove();
		  	}
		}
		return metricSequence;
	}
	
	public static LinkedList<Metric> fillInVolumeSDs(LinkedList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Long> periodsVolumes = new LinkedList<Long>();
		
		for (Metric metric:metricSequence) {
			long volume = metric.getVolume();
			
			if (periodsVolumes.size() < (period - 1)) {
		  		periodsVolumes.add(volume);
		  		metric.setValue(null);
		  		metric.setName("volumesd" + period);
		  	}
		  	else {
		  		periodsVolumes.add(volume);
		  		long periodsVolumesSum = 0;
		  		for (Long v:periodsVolumes) {
		  			periodsVolumesSum += v;
		  		}
		  		long averageVolume = periodsVolumesSum / period;
		  		long sumOfDifferenceFromAverageSquares = 0;
		  		for (Long v:periodsVolumes) {
		  			sumOfDifferenceFromAverageSquares += ((v - averageVolume) * (v - averageVolume));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.setValue(sd);
		  		metric.setName("volumesd" + period);
		  		
		  		periodsVolumes.remove();
		  	}
		}
		
		return metricSequence;
	}
}