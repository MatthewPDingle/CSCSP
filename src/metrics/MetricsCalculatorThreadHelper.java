package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import constants.Constants.BAR_SIZE;
import data.Metric;
import utils.CalendarUtils;
import dbio.QueryManager;

public class MetricsCalculatorThreadHelper extends Thread {

	public void run() {
		try {
			MetricSequenceDataSingleton msds = MetricSequenceDataSingleton.getInstance();
			LinkedList<Metric> ms = msds.popMetricSequence();
			while (ms != null) {
				Metric first = ms.getFirst();
				String symbol = "";
				if (first != null) {
					symbol = first.symbol;
				}
				System.out.println("Processing " + symbol);

				
				// Going one symbol at a time.  Calculate and
//				MetricsCalculator.fillInWeightedDVEMA(null, ms, 75);
//				MetricsCalculator.fillInWeightedDVEMA(null, ms, 50);
//				MetricsCalculator.fillInWeightedDVEMA(null, ms, 25);
//				MetricsCalculator.fillInWeightedDVEMA(null, ms, 10);
//				prepareMetricInsertStatement(ms);
//				prepareMetricInsertStatement(ms);
//				prepareMetricInsertStatement(ms);
//				prepareMetricInsertStatement(ms);
//				
//				prepareMetricInsertStatement(MetricsCalculator.fillInDV2(ms));
//				
//				prepareMetricInsertStatement(MetricsCalculator.fillInRSI(ms, 2));
//				prepareMetricInsertStatement(MetricsCalculator.fillInRSI(ms, 5));
//				prepareMetricInsertStatement(MetricsCalculator.fillInRSI(ms, 14));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInRSIAlpha(ms, 2));
				prepareMetricInsertStatement(MetricsCalculator.fillInRSIAlpha(ms, 5));
				prepareMetricInsertStatement(MetricsCalculator.fillInRSIAlpha(ms, 14));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(ms, 75));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(ms, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(ms, 25));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(ms, 10));	
				
				prepareMetricInsertStatement(MetricsCalculator.fillInMFI(ms, 2));
				prepareMetricInsertStatement(MetricsCalculator.fillInMFI(ms, 5));
				prepareMetricInsertStatement(MetricsCalculator.fillInMFI(ms, 14));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(ms, 75));	
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(ms, 50));	
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(ms, 25));	
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(ms, 10));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInUpStreaks(ms));
				prepareMetricInsertStatement(MetricsCalculator.fillInDownStreaks(ms));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(ms, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(ms, 100));
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(ms, 200));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInGapBoll(ms, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInGapBoll(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInGapBoll(ms, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInIntradayBoll(ms, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInIntradayBoll(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInIntradayBoll(ms, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(ms, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(ms, 100));
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(ms, 200));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsR(ms, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsR(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsR(ms, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInMACD(ms, 12, 26, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACD(ms, 20, 40, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACD(ms, 40, 80, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACDDivergence(ms, 12, 26, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACDDivergence(ms, 20, 40, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACDDivergence(ms, 40, 80, 9));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsRAlpha(ms, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsRAlpha(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsRAlpha(ms, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInPSAR(ms));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInUltimateOscillator(ms));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInAroonOscillator(ms));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInCCI(ms, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInCCI(ms, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInCCI(ms, 40));
				
				// Required for query to gauge volatility
				prepareMetricInsertStatement(MetricsCalculator.fillInMVOL(ms, 100));
				
				ms = msds.popMetricSequence();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void prepareMetricInsertStatement(LinkedList<Metric> metricSequence) {
		try {
			ArrayList<String> records = new ArrayList<String>();
			String metricName = "";
			for (Metric metric:metricSequence) {
				metricName = metric.name;
				String symbol = metric.symbol;
				Float value = metric.value;
				String sValue = "NULL";
				if (value == null || value.isNaN() || value.isInfinite()) {
					sValue = "NULL";
				}
				else {
					sValue = value.toString();
				}
				
				// See if this metric/symbol/start/duration combo needs stats
				Calendar start = metric.start;
				Calendar end = metric.end;
				BAR_SIZE duration = metric.duration;
	
			  	StringBuilder sb = new StringBuilder();
			  	sb.append("('");
			  	sb.append(metricName);
			  	sb.append("', '");
			  	sb.append(symbol);
			  	sb.append("', '");
			  	sb.append(CalendarUtils.getSqlDateTimeString(start));
			  	sb.append("', '");
			  	sb.append(CalendarUtils.getSqlDateTimeString(end));
			  	sb.append("', '");
			  	sb.append(duration.toString());
			  	sb.append("', ");
			  	sb.append(sValue);
			  	sb.append(")");
			  	records.add(sb.toString());
			}
			if (records.size() > 0) {
				QueryManager.insertMetrics(records);
				records.clear();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}