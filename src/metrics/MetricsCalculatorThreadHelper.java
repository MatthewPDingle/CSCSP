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
			LinkedList<Metric> metricSequence = msds.popMetricSequence();
			while (metricSequence != null) {
				Metric first = metricSequence.getFirst();
				String symbol = "";
				if (first != null) {
					symbol = first.symbol;
				}
				System.out.println("Processing " + symbol);

				
				// Going one symbol at a time.  Calculate and
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVEMA(metricSequence, 75));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVEMA(metricSequence, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVEMA(metricSequence, 25));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVEMA(metricSequence, 10));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInDV2(metricSequence));
				prepareMetricInsertStatement(MetricsCalculator.fillInDVFading4(metricSequence));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInRSI(metricSequence, 2));
				prepareMetricInsertStatement(MetricsCalculator.fillInRSI(metricSequence, 5));
				prepareMetricInsertStatement(MetricsCalculator.fillInRSI(metricSequence, 14));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInRSIAlpha(metricSequence, 2));
				prepareMetricInsertStatement(MetricsCalculator.fillInRSIAlpha(metricSequence, 5));
				prepareMetricInsertStatement(MetricsCalculator.fillInRSIAlpha(metricSequence, 14));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(metricSequence, 75));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(metricSequence, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(metricSequence, 25));
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedRSI(metricSequence, 10));	
				
				prepareMetricInsertStatement(MetricsCalculator.fillInMFI(metricSequence, 2));
				prepareMetricInsertStatement(MetricsCalculator.fillInMFI(metricSequence, 5));
				prepareMetricInsertStatement(MetricsCalculator.fillInMFI(metricSequence, 14));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(metricSequence, 75));	
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(metricSequence, 50));	
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(metricSequence, 25));	
				prepareMetricInsertStatement(MetricsCalculator.fillInWeightedDVol(metricSequence, 10));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInUpStreaks(metricSequence));
				prepareMetricInsertStatement(MetricsCalculator.fillInDownStreaks(metricSequence));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(metricSequence, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(metricSequence, 100));
				prepareMetricInsertStatement(MetricsCalculator.fillInPriceBoll(metricSequence, 200));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInGapBoll(metricSequence, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInGapBoll(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInGapBoll(metricSequence, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInIntradayBoll(metricSequence, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInIntradayBoll(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInIntradayBoll(metricSequence, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(metricSequence, 50));
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(metricSequence, 100));
				prepareMetricInsertStatement(MetricsCalculator.fillInVolumeBoll(metricSequence, 200));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsR(metricSequence, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsR(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsR(metricSequence, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInMACD(metricSequence, 12, 26, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACD(metricSequence, 20, 40, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACD(metricSequence, 40, 80, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACDDivergence(metricSequence, 12, 26, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACDDivergence(metricSequence, 20, 40, 9));
				prepareMetricInsertStatement(MetricsCalculator.fillInMACDDivergence(metricSequence, 40, 80, 9));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsRAlpha(metricSequence, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsRAlpha(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInWilliamsRAlpha(metricSequence, 50));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInPSAR(metricSequence));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInUltimateOscillator(metricSequence));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInAroonOscillator(metricSequence));
				
				prepareMetricInsertStatement(MetricsCalculator.fillInCCI(metricSequence, 10));
				prepareMetricInsertStatement(MetricsCalculator.fillInCCI(metricSequence, 20));
				prepareMetricInsertStatement(MetricsCalculator.fillInCCI(metricSequence, 40));
				
				// Required for query to gauge volatility
				prepareMetricInsertStatement(MetricsCalculator.fillInMVOL(metricSequence, 100));
				
				metricSequence = msds.popMetricSequence();
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