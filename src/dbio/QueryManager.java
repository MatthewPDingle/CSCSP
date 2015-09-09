package dbio;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.Metric;
import data.MetricKey;
import data.Model;
import gui.MapCell;
import gui.MapSymbol;
import gui.singletons.ParameterSingleton;
import utils.CalcUtils;
import utils.CalendarUtils;
import utils.ConnectionSingleton;

public class QueryManager {

	/**
	 * The stop part seems to be broken as of CSCSP .38 on 7/8/2015.
	 * This method is deprecated as of CSCSP .38 in favor of the getDataForCells method below.
	 * 
	 * @param x
	 * @param xCellSize
	 * @param y
	 * @param yCellSize
	 * @param latestDateInBasicr
	 * @return
	 */
	@Deprecated
	public static MapCell cellQuery(float x, float xCellSize, float y, float yCellSize, Calendar latestDateInBasicr) {
		try {
			ParameterSingleton ps = ParameterSingleton.getInstance();
			
			String q = "SELECT ";
			if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_NUM_BARS_LATER)) {
				// "metricsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol ORDER BY start LIMIT " + ((int) ps.getSellValue() + 1) + ") q ORDER BY start DESC LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS metricsp500perchange, ";
				// "perchange" when the sell metric is # Trading Days Later
				q += "((SELECT q.close FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol ORDER BY start LIMIT " + ((int) ps.getSellValue() + 1) + " ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100 AS perchange, ";
				// "positionlength". Is 0 if the metric exit never hits and it runs to the latest start.
				q += "(SELECT COUNT(*) FROM bar b3 WHERE b3.symbol = b.symbol AND b3.start > b.start AND b3.start <= (SELECT q.start FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol ORDER BY start LIMIT " + ((int) ps.getSellValue() + 1) + " ) q ORDER BY start DESC LIMIT 1)) AS positionlength, ";
			} else if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_PERCENT_UP)) {
				// "metricsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT start FROM bar WHERE start >= b.start AND symbol = b.symbol AND (close-b.close) / b.close * 100 >= " + ps.getSellValue() + " ORDER BY start) q ORDER BY start LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS metricsp500perchange, ";
				// "perchange" when the sell metric is % Up - Will be latest start if it never hits.
				q += "COALESCE((SELECT (q.close - b.close) / b.close * 100 FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol AND (close - b.close) / b.close * 100 >= " + ps.getSellValue() + " ORDER BY start) q ORDER BY start LIMIT 1), (((SELECT q.close FROM (SELECT start, close FROM bar WHERE start > b.start AND symbol = b.symbol ORDER BY start DESC LIMIT 1 ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100)) AS perchange, ";
				// "positionlength". Is 0 if the metric exit never hits and it runs to the latest start.
				q += "(SELECT COUNT(*) FROM bar b3 WHERE b3.symbol = b.symbol AND b3.start > b.start AND b3.start <= (SELECT q.start FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol AND (close - b.close) / b.close * 100 >= " + ps.getSellValue() + " ORDER BY start) q ORDER BY start LIMIT 1)) AS positionlength, ";
			} else if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_PERCENT_DOWN)) {
				// "metricsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT start FROM bar WHERE start >= b.start AND symbol = b.symbol AND (close-b.close) / b.close * 100 <= -"+ ps.getSellValue() + " ORDER BY start) q ORDER BY start LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS metricsp500perchange, ";
				// "perchange" when the sell metric is % Down - Will be latest start if it never hits.
				q += "COALESCE((SELECT (q.close - b.close) / b.close * 100 FROM 	(SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol AND (close - b.close) / b.close * 100 <= -" + ps.getSellValue() + " ORDER BY start) q ORDER BY start LIMIT 1), (((SELECT q.close FROM (SELECT start, close FROM bar WHERE start > b.start AND symbol = b.symbol ORDER BY start DESC LIMIT 1 ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100)) AS perchange, ";
				// "positionlength". Is 0 if the metric exit never hits and it runs to the latest start.
				q += "(SELECT COUNT(*) FROM bar b3 WHERE b3.symbol = b.symbol AND b3.start > b.start AND b3.start <= (SELECT q.start FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol AND (close - b.close) / b.close * 100 <= -" + ps.getSellValue() + " ORDER BY start) q ORDER BY start LIMIT 1)) AS positionlength, ";
			} else {
				// "metricsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT r.start, r.close, m.value FROM bar r INNER JOIN (SELECT symbol, start, value FROM metrics WHERE name = '" + ps.getSellMetric() + "') m ON r.symbol = m.symbol AND r.start = m.start WHERE r.start > b.start AND r.symbol = b.symbol AND m.value " + ps.getSellOperator() + " " + ps.getSellValue() + " ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS metricsp500perchange, ";			 
				// "perchange" for any other exit. Will be based on the latest start if the metric exit never hits.
				q += "COALESCE((((SELECT q.close FROM (SELECT r.start, r.close, m.value FROM bar r INNER JOIN (SELECT symbol, start, value FROM metrics WHERE name = '" + ps.getSellMetric() + "') m ON r.symbol = m.symbol AND r.start = m.start WHERE r.start > b.start AND r.symbol = b.symbol AND m.value " + ps.getSellOperator() + " " + ps.getSellValue() + " ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100), (((SELECT q.close FROM (SELECT start, close FROM bar WHERE start > b.start AND symbol = b.symbol ORDER BY start DESC LIMIT 1 ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100)) AS perchange, ";
				// "positionlength". Is 0 if it the metric exit never hits and it runs to the latest start.
				q += "(SELECT COUNT(*) FROM bar b3 WHERE b3.symbol = b.symbol AND b3.start > b.start AND b3.start <= (SELECT r.start FROM bar r INNER JOIN (SELECT symbol, start, value FROM metrics WHERE name = '" + ps.getSellMetric() + "') m ON r.symbol = m.symbol AND r.start = m.start WHERE r.start > b.start AND r.symbol = b.symbol AND m.value " + ps.getSellOperator() + " " + ps.getSellValue() + " ORDER BY r.start LIMIT 1))  AS positionlength, "; 
			}

			// "latestdatepositionlength"
			q += "(SELECT COUNT(*) FROM bar b4 WHERE b4.symbol = b.symbol AND b4.start > b.start AND b4.start <= (SELECT start FROM bar WHERE start > b.start AND symbol = b.symbol ORDER BY start DESC LIMIT 1 )) AS latestdatepositionlength, ";
			// "endsp500perchange"
			q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT start FROM bar WHERE start > b.start AND symbol = b.symbol ORDER BY start DESC LIMIT 1) q ORDER BY start DESC LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS endsp500perchange, ";
			
			// Stop Metric (% Down, # Days, or None)
			if (ps.getStopMetric().equals(Constants.OTHER_SELL_METRIC_PERCENT_DOWN)) {
				// "stopexitperchange"
				q += "(((SELECT q.close FROM (SELECT r.start, r.close FROM bar r WHERE r.start > b.start AND r.symbol = b.symbol AND (b.close - r.close) / b.close * 100 >= " + ps.getStopValue() + " " + "ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100) AS stopexitperchange, ";
				// "stopexitpositionlength". Is 0 if the stop isn't hit.
				q += "(SELECT COUNT(*) FROM bar b5 WHERE b5.symbol = b.symbol AND b5.start > b.start AND b5.start <= (SELECT q.start FROM (SELECT r.start, r.close FROM bar r WHERE r.start > b.start AND r.symbol = b.symbol AND (b.close - r.close) / b.close * 100 >= " + ps.getStopValue() + " " + "ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1)) AS stopexitpositionlength, ";
				// "stopsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT r.start, r.close FROM bar r WHERE r.start > b.start AND r.symbol = b.symbol AND (b.close - r.close) / b.close * 100 >= " + ps.getStopValue() + " ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS stopsp500perchange ";
			} else if (ps.getStopMetric().equals(Constants.OTHER_SELL_METRIC_PERCENT_UP)) {
				// "stopexitperchange"
				q += "(((SELECT q.close FROM (SELECT r.start, r.close FROM bar r WHERE r.start > b.start AND r.symbol = b.symbol AND (r.close - b.close) / b.close * 100 >= " + ps.getStopValue() + " " + "ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100) AS stopexitperchange, ";
				// "stopexitpositionlength". Is 0 if the stop isn't hit.
				q += "(SELECT COUNT(*) FROM bar b5 WHERE b5.symbol = b.symbol AND b5.start > b.start AND b5.start <= (SELECT q.start FROM (SELECT r.start, r.close FROM bar r WHERE r.start > b.start AND r.symbol = b.symbol AND (r.close - b.close) / b.close * 100 >= " + ps.getStopValue() + " " + "ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1)) AS stopexitpositionlength, ";
				// "stopsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT r.start, r.close FROM bar r WHERE r.start > b.start AND r.symbol = b.symbol AND (r.close - b.close) / b.close * 100 >= " + ps.getStopValue() + " ORDER BY r.start LIMIT 1 ) q ORDER BY start DESC LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS stopsp500perchange ";
			} else if (ps.getStopMetric().equals("# Days")) {
				// "stopexitperchange"
				q += "((SELECT q.close FROM (SELECT start, close FROM bar " + "WHERE start >= b.start AND symbol = b.symbol ORDER BY start LIMIT " + (ps.getStopValue() + 1) + ") q ORDER BY start DESC LIMIT 1) - b.close) / b.close * 100 AS stopexitperchange, ";
				// "stopexitpositionlength". Is 0 if the stop isn't hit
				q += "CASE WHEN (SELECT COUNT(*) FROM bar b5 WHERE b5.symbol = b.symbol AND b5.start > b.start AND b5.start <= (SELECT q.start FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol ORDER BY start LIMIT " + (ps.getStopValue() + 1) + ") q ORDER BY start DESC LIMIT 1)) = " + (ps.getStopValue()) + " THEN (SELECT COUNT(*) FROM bar b5 WHERE b5.symbol = b.symbol AND b5.start > b.start AND b5.start <= (SELECT q.start FROM (SELECT start, close FROM bar WHERE start >= b.start AND symbol = b.symbol ORDER BY start LIMIT " + (ps.getStopValue() + 1) + " ) q ORDER BY start DESC LIMIT 1)) ELSE 0 END AS stopexitpositionlength, ";
				// "stopsp500perchange"
				q += "(SELECT (b2.close - b1.close) / b1.close * 100 FROM bar b1 INNER JOIN bar b2 ON b1.symbol = b2.symbol AND b2.start = (SELECT q.start FROM (SELECT start FROM bar WHERE start >= b.start AND symbol = 'SPY' ORDER BY start LIMIT " + (ps.getStopValue() + 1) + ") q ORDER BY start DESC LIMIT 1) WHERE b1.symbol = 'SPY' AND b1.start = b.start) AS stopsp500perchange ";
			} else { // No Stop Loss Metric
				q += "NULL AS stopexitperchange, 0 AS stopexitpositionlength, NULL AS stopsp500perchange ";
			}
			// END OF SELECT SECTION.
			q += "FROM bar b "
					+ "INNER JOIN (SELECT symbol, start, value FROM metrics WHERE name = '" + ps.getyAxisMetric() + "') y "
					+ "ON b.symbol = y.symbol AND b.start = y.start "
					+ "INNER JOIN (SELECT symbol, start, value FROM metrics WHERE name = '" + ps.getxAxisMetric() + "') x "
					+ "ON b.symbol = x.symbol AND b.start = x.start "
					+ "INNER JOIN (SELECT symbol, start, value FROM metrics WHERE name = 'priceboll20') psd "
					+ "ON b.symbol = psd.symbol AND b.start = psd.start "
					+ "WHERE b.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE ";
			// Index List
			String i = "";
			boolean one = false;
			if (ps.isNyse()) {
				i = "index = 'NYSE' ";
				one = true;
			}
			if (ps.isNasdaq()) {
				if (one) {
					i += "OR index = 'Nasdaq' ";
				} else {
					i = "index = 'Nasdaq' ";
				}
			}
			if (ps.isDjia()) {
				if (one) {
					i += "OR index = 'DJIA' ";
				} else {
					i = "index = 'DJIA' ";
				}
			}
			if (ps.isSp500()) {
				if (one) {
					i += "OR index = 'SP500' ";
				} else {
					i = "index = 'SP500' ";
				}
			}
			if (ps.isEtf()) {
				if (one) {
					i += "OR index = 'ETF' ";
				} else {
					i = "index = 'ETF' ";
				}
			}
			if (ps.isBitcoin()) {
				if (one) {
					i += "OR index = 'Bitcoin' ";
				} else {
					i = "index = 'Bitcoin' ";
				}
			}
			q += i + ") " + "AND b.start >= '"
					+ CalendarUtils.getSqlDateString(ps.getFromCal()) + "' AND b.start < '"
					+ CalendarUtils.getSqlDateString(ps.getToCal()) + "' "
					+ "AND b.start < '"
					+ CalendarUtils.getSqlDateString(latestDateInBasicr) + "' "
					+ "AND x.value >= " + x + " AND x.value <= "
					+ (x + xCellSize) + " " + "AND y.value >= " + y
					+ " AND y.value <= " + (y + yCellSize) + " "
					+ "AND b.volume >= " + ps.getMinLiquidity() + " / b.close "
					+ "AND ABS(psd.value) <= " + ps.getMaxVolatility() + " "
					+ "AND b.close >= " + ps.getMinPrice() + " ";
			// Sector & Industry
			if (!ps.getSector().equals("All")) {
				q += "AND b.symbol IN (SELECT DISTINCT symbol FROM sectorandindustry WHERE sector = '" + ps.getSector() + "') ";
			}
			if (!ps.getIndustry().equals("All")) {
				q += "AND b.symbol IN (SELECT DISTINCT symbol FROM sectorandindustry WHERE industry = '" + ps.getIndustry() + "') ";
			}

//			System.out.println(q);

			// Run Query
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);

			int numResults = 0;
			int numMetricExits = 0;
			int numStopExits = 0;
			int numLatestExits = 0;
			ArrayList<Float> allPerchangesList = new ArrayList<Float>();
			ArrayList<Float> allAlphaPerchangesList = new ArrayList<Float>();
			ArrayList<Float> metricPerchangesList = new ArrayList<Float>();
			ArrayList<Float> stopPerchangesList = new ArrayList<Float>();
			ArrayList<Float> latestPerchangesList = new ArrayList<Float>();
			ArrayList<Integer> allTradeDurations = new ArrayList<Integer>();
			ArrayList<Integer> metricTradeDurations = new ArrayList<Integer>();
			ArrayList<Integer> stopTradeDurations = new ArrayList<Integer>();
			ArrayList<Integer> latestTradeDurations = new ArrayList<Integer>();

			// Iterate through the cell query results
			while (rs.next()) {
				numResults++;

				float perchange = rs.getFloat("perchange");
				int positionLength = rs.getInt("positionLength");
				int latestDatePositionLength = rs.getInt("latestdatepositionlength");
				float stopExitPerchange = rs.getFloat("stopexitperchange");
				int stopExitPositionLength = rs.getInt("stopexitpositionlength");
				float metricSP500Perchange = rs.getFloat("metricsp500perchange");
				float endSP500Perchange = rs.getFloat("endsp500perchange");
				float stopSP500Perchange = rs.getFloat("stopsp500perchange");

				// Mathy Time	
				boolean latestExit = false;
				if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_NUM_BARS_LATER)) {
					if (latestDatePositionLength == positionLength 
							&& (latestDatePositionLength <= stopExitPositionLength || stopExitPositionLength == 0)
							&& latestDatePositionLength < ps.getSellValue()) {
						latestExit = true;
					}
				}
				else {
					if (positionLength == 0 && stopExitPositionLength == 0) {
						latestExit = true;
					}
				}
				
				if (latestExit) {
					// Latest Date Exit
					numLatestExits++;
					allAlphaPerchangesList.add(perchange - endSP500Perchange);
					allPerchangesList.add(perchange);
					latestPerchangesList.add(perchange);
					allTradeDurations.add(latestDatePositionLength);
					latestTradeDurations.add(latestDatePositionLength);
				} else {
					if (positionLength == 0 && stopExitPositionLength > 0) {
						// Stop Exit
						numStopExits++;
						allAlphaPerchangesList.add(stopExitPerchange - stopSP500Perchange);
						allPerchangesList.add(stopExitPerchange);
						stopPerchangesList.add(stopExitPerchange);
						allTradeDurations.add(stopExitPositionLength);
						stopTradeDurations.add(stopExitPositionLength);
					} else if (positionLength > 0 && stopExitPositionLength == 0) {
						// Metric Exit
						numMetricExits++;
						allAlphaPerchangesList.add(perchange - metricSP500Perchange);
						allPerchangesList.add(perchange);
						metricPerchangesList.add(perchange);
						allTradeDurations.add(positionLength);
						metricTradeDurations.add(positionLength);
					} else {
						if (positionLength <= stopExitPositionLength) {
							// Metric Exit
							numMetricExits++;
							allAlphaPerchangesList.add(perchange - metricSP500Perchange);
							allPerchangesList.add(perchange);
							metricPerchangesList.add(perchange);
							allTradeDurations.add(positionLength);
							metricTradeDurations.add(positionLength);
						} else {
							// Stop Exit
							numStopExits++;
							allAlphaPerchangesList.add(stopExitPerchange - stopSP500Perchange);
							allPerchangesList.add(stopExitPerchange);
							stopPerchangesList.add(stopExitPerchange);
							allTradeDurations.add(stopExitPositionLength);
							stopTradeDurations.add(stopExitPositionLength);
						}
					}
				}
			}
			
			rs.close();
			s.close();
			c.close();

			Float allGeomeanPerDay = 0f;
			if (CalcUtils.getMean(allTradeDurations) > 0)
				allGeomeanPerDay = CalcUtils.getGeoMean(allPerchangesList) / CalcUtils.getMean(allTradeDurations);
			
			Float allAlphaGeomeanPerDay = 0f;
			if (CalcUtils.getMean(allTradeDurations) > 0)
				allAlphaGeomeanPerDay = CalcUtils.getGeoMean(allAlphaPerchangesList) / CalcUtils.getMean(allTradeDurations);

			Float metricGeomeanPerDay = 0f;
			if (CalcUtils.getMean(metricTradeDurations) > 0)
				metricGeomeanPerDay = CalcUtils.getGeoMean(metricPerchangesList) / CalcUtils.getMean(metricTradeDurations);

			Float stopGeomeanPerDay = 0f;
			if (CalcUtils.getMean(stopTradeDurations) > 0)
				stopGeomeanPerDay = CalcUtils.getGeoMean(stopPerchangesList) / CalcUtils.getMean(stopTradeDurations);

			Float endGeomeanPerDay = 0f;
			if (CalcUtils.getMean(latestTradeDurations) > 0)
				endGeomeanPerDay = CalcUtils.getGeoMean(latestPerchangesList) / CalcUtils.getMean(latestTradeDurations);

			// Sharpe
			Float sharpe = 0f;
			if (CalcUtils.getStandardDeviation(allAlphaPerchangesList) > 0)
				sharpe = CalcUtils.getGeoMean(allAlphaPerchangesList) / CalcUtils.getStandardDeviation(allAlphaPerchangesList);
			// Sharpe can be irrationally high if the SD in the calc above is close to zero, so make it a max of 5.
			if (sharpe > 5)
				sharpe = 5f;

			// Sortino
			Float sortino = 0f;
			ArrayList<Float> allNegativeAlphaPerchangesList = CalcUtils.removePositives(allAlphaPerchangesList);
			if (CalcUtils.getStandardDeviation(allNegativeAlphaPerchangesList) > 0)
				sortino = CalcUtils.getGeoMean(allNegativeAlphaPerchangesList) / CalcUtils.getStandardDeviation(allNegativeAlphaPerchangesList);

			// Package results into a MapCell object
			MapCell mc = new MapCell();
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS, 			(float) numResults);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_PERCENT_POSITIONS, 		100f);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_MEAN_RETURN, 			CalcUtils.getMean(allPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_RETURN, 			CalcUtils.getGeoMean(allPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_MEDIAN_RETURN,			CalcUtils.getMedian(allPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT,		CalcUtils.getWinPercent(allPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_MEAN_POSITION_DURATION,	CalcUtils.getMean(allTradeDurations));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_MAX_DRAWDOWN,			CalcUtils.getMaxDrawdownPercent(allPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR,			allGeomeanPerDay);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO, 			sharpe);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_SORTINO_RATIO, 			sortino);

			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_RETURN, 	CalcUtils.getGeoMean(allAlphaPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_RETURN,		CalcUtils.getMean(allAlphaPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEDIAN_RETURN, 	CalcUtils.getMedian(allAlphaPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT,  CalcUtils.getWinPercent(allAlphaPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR,   allAlphaGeomeanPerDay);
			
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_NUM_POSITIONS,		(float) numMetricExits);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_PERCENT_POSITIONS,	numMetricExits / (float) numResults * 100f);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_MEAN_RETURN,			CalcUtils.getMean(metricPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_GEOMEAN_RETURN,		CalcUtils.getGeoMean(metricPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_MEDIAN_RETURN,		CalcUtils.getMedian(metricPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_MEAN_WIN_PERCENT,		CalcUtils.getWinPercent(metricPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_MEAN_POSITION_DURATION,CalcUtils.getMean(metricTradeDurations));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_METRIC_GEOMEAN_PER_BAR,		metricGeomeanPerDay);

			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_NUM_POSITIONS,			(float) numStopExits);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_PERCENT_POSITIONS,		numStopExits / (float) numResults * 100f);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_MEAN_RETURN,			CalcUtils.getMean(stopPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_GEOMEAN_RETURN,			CalcUtils.getGeoMean(stopPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_MEDIAN_RETURN,			CalcUtils.getMedian(stopPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT,		CalcUtils.getWinPercent(stopPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_MEAN_POSITION_DURATION,	CalcUtils.getMean(stopTradeDurations));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_STOP_GEOMEAN_PER_BAR,		stopGeomeanPerDay);

			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_NUM_POSITIONS,			(float) numLatestExits);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_PERCENT_POSITIONS,		numLatestExits / (float) numResults * 100f);
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_MEAN_RETURN,				CalcUtils.getMean(latestPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_GEOMEAN_RETURN,			CalcUtils.getGeoMean(latestPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_MEDIAN_RETURN,			CalcUtils.getMedian(latestPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT,		CalcUtils.getWinPercent(latestPerchangesList));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_MEAN_POSITION_DURATION,	CalcUtils.getMean(latestTradeDurations));
			mc.addToMetricValueHash(Constants.MAP_COLOR_OPTION_END_GEOMEAN_PER_BAR,			endGeomeanPerDay);

			return mc;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static HashMap<String, ArrayList<HashMap<String, Object>>> getMapDataForCells(Calendar latestDateInBar) {
		HashMap<String, ArrayList<HashMap<String, Object>>> mapData = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		try {
			ParameterSingleton ps = ParameterSingleton.getInstance();
			
			// This query gets all the data needed for every map cell
			String q = 	"SELECT b.*, m1.value AS m1v, m2.value AS m2v, m4.value AS m4v, (SELECT close FROM bar WHERE symbol = 'SPY' AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphaclose " +
						"FROM bar b " +
						"LEFT OUTER JOIN metrics m1 ON b.symbol = m1.symbol and b.start = m1.start AND b.duration = m1.duration " + // Buy X
						"LEFT OUTER JOIN metrics m2 ON b.symbol = m2.symbol and b.start = m2.start AND b.duration = m2.duration " + // Buy Y
						"LEFT OUTER JOIN metrics m3 ON b.symbol = m3.symbol and b.start = m3.start AND b.duration = m3.duration " + // Volatility
						"LEFT OUTER JOIN metrics m4 ON b.symbol = m4.symbol and b.start = m4.start AND b.duration = m4.duration " + // Sell
						"WHERE b.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE ";
			// Index List
			String i = "";
			boolean one = false;
			if (ps.isNyse()) {
				i = "index = 'NYSE' ";
				one = true;
			}
			if (ps.isNasdaq()) {
				if (one) {
					i += "OR index = 'Nasdaq' ";
				} else {
					i = "index = 'Nasdaq' ";
				}
			}
			if (ps.isDjia()) {
				if (one) {
					i += "OR index = 'DJIA' ";
				} else {
					i = "index = 'DJIA' ";
				}
			}
			if (ps.isSp500()) {
				if (one) {
					i += "OR index = 'SP500' ";
				} else {
					i = "index = 'SP500' ";
				}
			}
			if (ps.isEtf()) {
				if (one) {
					i += "OR index = 'ETF' ";
				} else {
					i = "index = 'ETF' ";
				}
			}
			if (ps.isBitcoin()) {
				if (one) {
					i += "OR index = 'Bitcoin' ";
				} else {
					i = "index = 'Bitcoin' ";
				}
			}
			q += i + ") ";
			
			ArrayList<BarKey> barKeys = ps.getBarKeys();
			String symbolsClause = "";
			if (barKeys != null) {
				symbolsClause = "AND (";
				for (BarKey bk : barKeys) {
					symbolsClause += "(b.symbol = '" + bk.symbol + "' AND b.duration = '" + bk.duration.toString() + "') OR ";
				}
				symbolsClause = symbolsClause.substring(0, symbolsClause.length() - 4) + ") ";
			}
			
						q += symbolsClause +
						"AND m1.name = '" + ps.getxAxisMetric() + "' " +
						"AND m2.name = '" + ps.getyAxisMetric() + "' " +
						"AND m3.name = 'pricebolls20' " +
						"AND m4.name = '" + ps.getSellMetric() + "' " +
						"AND ABS(m3.value) <= " + ps.getMaxVolatility() + " " +
						"AND b.close >= " + ps.getMinPrice() + " " +
						"AND b.volume * b.close >= " + ps.getMinLiquidity() + " " +
						"AND b.start >= '" + CalendarUtils.getSqlDateString(ps.getFromCal()) + "' " +
						"AND b.start < '" + CalendarUtils.getSqlDateString(ps.getToCal()) + "' " +
						"AND b.start < '" + CalendarUtils.getSqlDateString(latestDateInBar) + "' " +
						"ORDER BY b.symbol, b.start";
			
			System.out.println(q);
			
			// Run Query
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			
			// Iterate through the cell query results
			String workingSymbol = null;
			ArrayList<HashMap<String, Object>> symbolResults = new ArrayList<HashMap<String, Object>>();
			while (rs.next()) {
				// On the first pass, set the workingSymbol to the first symbol in the resultset
				String symbol = rs.getString("symbol");
				if (workingSymbol == null) {
					workingSymbol = symbol;
				}
				
				// Package everything in this record into a barHash
				HashMap<String, Object> barHash = new HashMap<String, Object>();
				barHash.put("symbol", rs.getString("symbol"));
				barHash.put("open", rs.getFloat("open"));
				barHash.put("close", rs.getFloat("close"));
				barHash.put("high", rs.getFloat("high"));
				barHash.put("low", rs.getFloat("low"));
				barHash.put("vwap", rs.getFloat("vwap"));
				barHash.put("volume", rs.getFloat("volume"));
				barHash.put("numtrades", rs.getInt("numtrades"));
				barHash.put("change", rs.getFloat("change"));
				barHash.put("gap", rs.getFloat("gap"));
				barHash.put("start", rs.getTimestamp("start"));
				barHash.put("end", rs.getTimestamp("end"));
				barHash.put("duration", rs.getString("duration"));
				barHash.put("partial", rs.getBoolean("partial"));
				barHash.put("m1v", rs.getFloat("m1v")); // Metric 1 Buy X Value
				barHash.put("m2v", rs.getFloat("m2v")); // Metric 2 Buy Y Value
				barHash.put("m4v", rs.getFloat("m4v")); // Metric 4 Sell Value
				barHash.put("alphaclose", rs.getFloat("alphaclose")); // Alpha baseline - SPY by default.
				
				// If we're still dealing with the same symbol, add it to the symbolResults
				if (symbol.equals(workingSymbol)) {
					symbolResults.add(barHash);
				}
				else { // If it's a new symbol, add the last symbolResults to the mapData, clear out symbolResults, and set the new workingSymbol
					mapData.put(symbol, symbolResults);
					symbolResults = new ArrayList<HashMap<String, Object>>();
					symbolResults.add(barHash);
					workingSymbol = symbol;
				}
			}
			if (symbolResults.size() > 0) {
				mapData.put(workingSymbol, symbolResults);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return mapData;
	}

	public static Calendar getMaxDateFromBasicr() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(date) AS d FROM basicr";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);

			Date d = null;
			while (rs.next()) {
				d = rs.getDate("d");
			}

			rs.close();
			s.close();
			c.close();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			return cal;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Calendar getMaxDateFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(start) AS d FROM bar";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);

			Date d = null;
			while (rs.next()) {
				d = rs.getDate("d");
			}

			rs.close();
			s.close();
			c.close();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			return cal;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<Float> getSP500PerchangeList(Calendar from, Calendar to) {
		ArrayList<Float> perchangeList = new ArrayList<Float>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT b.date, ((SELECT q.adjclose FROM (SELECT date, adjclose FROM basicr "
					+ "WHERE date >= b.date AND symbol = b.symbol ORDER BY date LIMIT 2 ) q "
					+ "ORDER BY date DESC LIMIT 1) - b.adjclose) / b.adjclose * 100 AS tomorrowperchange "
					+ "FROM basicr b WHERE symbol = 'SPY' AND date >= '"
					+ CalendarUtils.getSqlDateString(from)
					+ "' "
					+ "AND date < '"
					+ CalendarUtils.getSqlDateString(to)
					+ "' ORDER BY date";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				perchangeList.add(rs.getFloat("tomorrowperchange"));
			}

			rs.close();
			s.close();
			c.close();
			
			return perchangeList;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return perchangeList;
		}
	}

	public static void updateIndexList(ArrayList<String> symbols, String index) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO indexlist(symbol, \"index\") VALUES ";
			StringBuilder sb = new StringBuilder();
			for (String symbol : symbols) {
				sb.append("('" + symbol + "', '" + index + "'), ");
			}
			String valuesPart = sb.toString();
			valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
			q = q + valuesPart;

			Statement s = c.createStatement();
			s.executeUpdate(q);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getUniqueListOfSymbols() {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String selectQuery = "SELECT DISTINCT symbol FROM "	+ Constants.INDEXLIST_TABLE;
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(selectQuery);
			while (rs.next()) {
				symbols.add(rs.getString("symbol"));
			}
			
			rs.close();
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<BarKey> getUniqueBarKeys() {
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT symbol, duration FROM bar GROUP BY symbol, duration";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				barKeys.add(new BarKey(symbol, duration));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return barKeys;
	}

	/**
	 * Gets data from 2 trading days ago.  
	 * If today is Friday, then get Thursday.
	 * If today is Saturday, then get Thursday.
	 * 
	 * @param minLiquidity
	 * @param maxVolatility
	 * @return
	 */
	public static ArrayList<String> getListOfSymbolsForRealtimeUpdates(int minLiquidity, float maxVolatility, float minPrice,
			boolean nyse, boolean nasdaq, boolean djia, boolean sp500, boolean etf, boolean index) {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			// Index List
			String whereIndexClause = "WHERE r.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE ";
			String i = "";
			boolean one = false;
			if (nyse) {
				i = "index = 'NYSE' ";
				one = true;
			}
			if (nasdaq) {
				if (one) {
					i += "OR index = 'Nasdaq' ";
				} else {
					i = "index = 'Nasdaq' ";
				}
				one = true;
			}
			if (djia) {
				if (one) {
					i += "OR index = 'DJIA' ";
				} else {
					i = "index = 'DJIA' ";
				}
				one = true;
			}
			if (sp500) {
				if (one) {
					i += "OR index = 'SP500' ";
				} else {
					i = "index = 'SP500' ";
				}
				one = true;
			}
			if (etf) {
				if (one) {
					i += "OR index = 'ETF' ";
				} else {
					i = "index = 'ETF' ";
				}
				one = true;
			}
			if (index) {
				if (one) {
					i += "OR index = 'Index' ";
				} else {
					i = "index = 'Index' ";
				}
			}
			whereIndexClause += i + ") ";
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"SELECT DISTINCT i.symbol " +
									"FROM indexlist i " +
									"INNER JOIN basicr r " +
									"ON r.symbol = i.symbol AND r.date = (SELECT MAX(date) FROM tradingdays WHERE date < (SELECT MAX(date) FROM tradingdays WHERE date < now()::date)) " +
									"INNER JOIN metric_priceboll20 sd " +
									"ON r.symbol = sd.symbol AND r.date = sd.date " +
									whereIndexClause + 
									"AND r.volume >= ? / r.adjclose " +
									"AND ABS(sd.value) <= ? " +
									"AND r.adjclose >= ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setInt(1, minLiquidity);
			ps.setFloat(2, maxVolatility);
			ps.setFloat(3, minPrice);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				symbols.add(rs.getString("symbol"));
			}
			symbols.add("SPY");
			
			rs.close();
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<MapSymbol> getMapSymbols() {
		ArrayList<MapSymbol> mapSymbols = new ArrayList<MapSymbol>();
		try {
			ParameterSingleton ps = ParameterSingleton.getInstance();
			
			// Index List
			String whereIndexClause = "WHERE b.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE ";
			String i = "";
			boolean one = false;
			if (ps.isNyse()) {
				i = "index = 'NYSE' ";
				one = true;
			}
			if (ps.isNasdaq()) {
				if (one) {
					i += "OR index = 'Nasdaq' ";
				} else {
					i = "index = 'Nasdaq' ";
				}
				one = true;
			}
			if (ps.isDjia()) {
				if (one) {
					i += "OR index = 'DJIA' ";
				} else {
					i = "index = 'DJIA' ";
				}
				one = true;
			}
			if (ps.isSp500()) {
				if (one) {
					i += "OR index = 'SP500' ";
				} else {
					i = "index = 'SP500' ";
				}
				one = true;
			}
			if (ps.isEtf()) {
				if (one) {
					i += "OR index = 'ETF' ";
				} else {
					i = "index = 'ETF' ";
				}
				one = true;
			}
			if (ps.isBitcoin()) {
				if (one) {
					i += "OR index = 'Bitcoin' ";
				} else {
					i = "index = 'Bitcoin' ";
				}
			}
			whereIndexClause += i + ") ";
			
			ArrayList<BarKey> barKeys = ps.getBarKeys();
			String symbolsClause = "";
			if (barKeys != null) {
				symbolsClause = "AND (";
				for (BarKey bk : barKeys) {
					symbolsClause += "(b.symbol = '" + bk.symbol + "' AND b.duration = '" + bk.duration.toString() + "') OR ";
				}
				symbolsClause = symbolsClause.substring(0, symbolsClause.length() - 4) + ") ";
			}

			// This clause used to be in here, and I'll need to add it back in.
			String openTradeClause = "OR b.symbol IN (SELECT symbol FROM trades WHERE status = 'open')";
			
			String q = 	"SELECT b.symbol, b.duration, b.close AS price " + //, m1.value AS m1, m2.value AS m2 " +
						"FROM bar b " +
						//"LEFT OUTER JOIN metrics m1 ON b.symbol = m1.symbol and b.start = m1.start AND b.duration = m1.duration " +
						//"LEFT OUTER JOIN metrics m2 ON b.symbol = m2.symbol and b.start = m2.start AND b.duration = m2.duration " +
						//"LEFT OUTER JOIN metrics m3 ON b.symbol = m3.symbol and b.start = m3.start AND b.duration = m3.duration " +
						"INNER JOIN (SELECT symbol, duration, MAX(start) AS start FROM bar GROUP BY symbol, duration) t ON t.symbol = b.symbol AND t.start = b.start  AND t.duration = b.duration " +
						whereIndexClause + 
						symbolsClause;
						//"AND m1.name = '" + ps.getxAxisMetric() + "' " +
						//"AND m2.name = '" + ps.getyAxisMetric() + "' " +
						//"AND m3.name = 'priceboll20' " +
						//"AND ABS(m3.value) <= " + ps.getMaxVolatility() + " " +
						//"AND b.volume * b.close >= " + ps.getMinLiquidity() + " " +
						//"AND b.close >= " + ps.getMinPrice();
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			PreparedStatement pst = c.prepareStatement(q);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				float price = rs.getFloat("price");
//				Float m1 = rs.getFloat("m1");
//				Float m2 = rs.getFloat("m2");
				MapSymbol ms = new MapSymbol(symbol, duration, price, null, null);
				mapSymbols.add(ms);
			}
			rs.close();
			pst.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return mapSymbols;
	}

	public static HashMap<String, Object> getLatestQuote(String symbol) {
		HashMap<String, Object> hash = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"SELECT * FROM basicr WHERE symbol = ? AND date = " +
						"(SELECT MAX(date) FROM basicr WHERE symbol = ?)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, symbol);
			ResultSet rs = ps.executeQuery();
					
			while (rs.next()) {
				String qsymbol = rs.getString("symbol");
				Float adjopen = rs.getFloat("adjopen");
				Float adjclose = rs.getFloat("adjclose");
				Float adjhigh = rs.getFloat("adjhigh");
				Float adjlow = rs.getFloat("adjlow");
				Date date = rs.getDate("date");
				Long volume = rs.getLong("volume");
				Float change = rs.getFloat("change");
				Float gap = rs.getFloat("gap");
				hash.put("symbol", qsymbol);
				hash.put("adjopen", adjopen);
				hash.put("adjclose", adjclose);
				hash.put("adjhigh", adjhigh);
				hash.put("adjlow", adjlow);
				hash.put("date", date);
				hash.put("volume", volume);
				hash.put("change", change);
				hash.put("gap", gap);
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}
	
	public static void saveFakeQuotes(ArrayList<HashMap<String, Object>> hashList) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO basicr (symbol, date, volume, adjopen, adjclose, adjhigh, adjlow, change, gap, partial) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?, ?, ?, true)";
			PreparedStatement ps = c.prepareStatement(q);
	
			for (HashMap<String, Object> hash:hashList) {
				String symbol = hash.get("symbol").toString();
				float adjopen = (float)hash.get("adjopen");
				float adjclose = (float)hash.get("adjclose");
				float adjhigh = (float)hash.get("adjhigh");
				float adjlow = (float)hash.get("adjlow");
				long volume = (long)hash.get("volume");
				float change = (float)hash.get("change");
				float gap = (float)hash.get("gap");
				Date date = (Date)hash.get("date");
				java.sql.Date nextTradingDate = getNextTradingDay(date);
				
				// Move the close up to +-.5%
				Random r = new Random();
				float pChange = r.nextFloat() / 200f;
				if (r.nextFloat() >= .5f) {
					pChange = -pChange;
				}
				adjclose = adjclose * (1f + pChange);
				if (adjclose > adjhigh) {
					adjhigh = adjclose;
				}
				if (adjclose < adjlow) {
					adjlow = adjclose;
				}
				
				// Move the volume between 61.8% up and 38.2% down (geo-mean's out to 0)
				float volFactor = r.nextFloat() - .382f;
				volume = volume + (long)(volume * volFactor);
				
				ps.setString(1, symbol);
				ps.setDate(2, nextTradingDate);
				ps.setLong(3, volume);
				ps.setFloat(4, adjopen);
				ps.setFloat(5, adjclose);
				ps.setFloat(6, adjhigh);
				ps.setFloat(7, adjlow);
				ps.setFloat(8, change);
				ps.setFloat(9, gap);
				ps.addBatch();
			}
			ps.executeBatch();
			c.commit();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static java.sql.Date getNextTradingDay(java.sql.Date d) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MIN(date) FROM tradingdays WHERE date > ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setDate(1, d);
			
			java.sql.Date rd = null;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				rd = rs.getDate(1);
				break;
			}
			rs.close();
			ps.close();
			c.close();
			
			return rd;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads metric sequences (oldest to newest) starting from the bar of the last metric available, or as early as 2010-01-01 
	 * if the bar data goes back that far.
	 * 
	 * @param barKeys
	 * @return
	 */
	public static HashMap<MetricKey, ArrayList<Metric>> loadMetricSequenceHash(ArrayList<BarKey> barKeys, ArrayList<String> neededMetrics) {
		HashMap<MetricKey, ArrayList<Metric>> metricSequenceHash = new HashMap<MetricKey, ArrayList<Metric>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			for (BarKey bk : barKeys) {
				for (String metricName : neededMetrics) {
					
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
					ArrayList<Metric> ms = metricSequenceHash.get(mk);
					if (ms == null) {
						ms = new ArrayList<Metric>();
					}
					
					// Get the base date
					String q0 = "SELECT COALESCE((SELECT MAX(start) FROM metrics WHERE symbol = ? AND duration = ? AND name = ?), '2010-01-01 00:00:00')";
					PreparedStatement s0 = c.prepareStatement(q0);
					s0.setString(1, bk.symbol);
					s0.setString(2, bk.duration.toString());
					s0.setString(3, metricName);
					ResultSet rs0 = s0.executeQuery();
					Calendar startCal = Calendar.getInstance();
					while (rs0.next()) {
						Timestamp tsStart = rs0.getTimestamp(1);
						startCal.setTimeInMillis(tsStart.getTime());
						break;
					}
					startCal = CalendarUtils.addBars(startCal, bk.duration, -100);
					rs0.close();
					s0.close();
					
					String alphaComparison = "SPY"; // TODO: probably change this.  Seems weird to compare bitcoin or forex to SPY.
					String q = "SELECT b.*, " +
							"(SELECT close FROM bar WHERE symbol = ? AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphaclose, " +
							"(SELECT change FROM bar WHERE symbol = ? AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphachange " +
							"FROM bar b " +
							"WHERE (b.start >= ? OR b.partial = true) " +
							"AND b.symbol = ? " +
							"AND b.duration = ? " +
							"ORDER BY b.start";
					
					PreparedStatement s = c.prepareStatement(q);
					s.setString(1, alphaComparison);
					s.setString(2, alphaComparison);
					s.setTimestamp(3, new Timestamp(startCal.getTimeInMillis()));
					s.setString(4, bk.symbol);
					s.setString(5, bk.duration.toString());
					ResultSet rs = s.executeQuery();
					
					int counter = 0;
					
					while (rs.next()) {
						Timestamp tsStart = rs.getTimestamp("start");
						Calendar start = Calendar.getInstance();
						start.setTimeInMillis(tsStart.getTime());
						Timestamp tsEnd = rs.getTimestamp("end");
						Calendar end = Calendar.getInstance();
						end.setTimeInMillis(tsEnd.getTime());
						end.set(Calendar.SECOND, 0);
						long volume = rs.getLong("volume");
						float adjOpen = rs.getFloat("open");
						float adjClose = rs.getFloat("close");
						float adjHigh = rs.getFloat("high");
						float adjLow = rs.getFloat("low");
						float alphaClose = rs.getFloat("alphaclose");
						float alphaChange = rs.getFloat("alphachange");
						float gap = rs.getFloat("gap");
						float change = rs.getFloat("change");

						// Create a Metric
						Metric m = new Metric(metricName, bk.symbol, start, end, bk.duration, volume, adjOpen, adjClose, adjHigh, adjLow, gap, change, alphaClose, alphaChange);
						ms.add(m);
						counter++;
					}
					metricSequenceHash.put(mk, ms);
//					System.out.println("Adding " + counter + " metrics to MetricSequence for " + mk.toString());
					
					rs.close();
					s.close();
				}
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metricSequenceHash;
	}

	public static ArrayList<LinkedList<Metric>> loadMetricSequencesForRealtimeUpdates(ArrayList<BarKey> barKeys) {
		ArrayList<LinkedList<Metric>> metricSequences = new ArrayList<LinkedList<Metric>>();
		try {
			// Put the list of symbols together into a string for the query
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (BarKey bk : barKeys) {
				sb.append("'").append(bk.symbol).append("'").append(",");
			}
			String symbolsString = sb.toString();
			if (barKeys.size() > 0) {
				symbolsString = symbolsString.substring(0, symbolsString.length() - 1) + ")";
			}
			else {
				symbolsString = "()";
			}
			
			// Query
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q1 = "SELECT DISTINCT symbol, " +
						"(SELECT MIN(start) FROM (SELECT symbol, start FROM bar WHERE symbol IN " + symbolsString + " ORDER BY start DESC LIMIT 300) t) AS baseDate " +
						"FROM bar WHERE symbol IN " + symbolsString;
			Statement s1 = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs1 = s1.executeQuery(q1);

			while (rs1.next()) {
				String symbol = rs1.getString("symbol");
				String baseDate = rs1.getString("baseDate"); // 300 bars ago

				// Fill a "metricSequence" with the price data for the last X
				// days + however many days I need stats for
				String alphaComparison = "SPY"; // TODO: probably change this
				String q2 = "SELECT r.*, " +
						"(SELECT close FROM bar WHERE symbol = '" + alphaComparison + "' AND start <= r.start ORDER BY start DESC LIMIT 1) AS alphaclose, " +
						"(SELECT change FROM bar WHERE symbol = '" + alphaComparison + "' AND start <= r.start ORDER BY start DESC LIMIT 1) AS alphachange " +
						"FROM bar r " +
						"WHERE r.symbol = '" + symbol + "' " +
						"AND r.start >= '" + baseDate + "' " +
						"ORDER BY start ASC";
				
				LinkedList<Metric> metricSequence = new LinkedList<Metric>();
				
				Statement s2 = c.createStatement();
				ResultSet rs2 = s2.executeQuery(q2);
				while (rs2.next()) {
					Timestamp tsStart = rs2.getTimestamp("start");
					Calendar start = Calendar.getInstance();
					start.setTimeInMillis(tsStart.getTime());
					Timestamp tsEnd = rs2.getTimestamp("end");
					Calendar end = Calendar.getInstance();
					end.setTimeInMillis(tsEnd.getTime());
					end.set(Calendar.SECOND, 0);
					String duration = rs2.getString("duration");
					long volume = rs2.getLong("volume");
					float adjOpen = rs2.getFloat("open");
					float adjClose = rs2.getFloat("close");
					float adjHigh = rs2.getFloat("high");
					float adjLow = rs2.getFloat("low");
					float alphaClose = rs2.getFloat("alphaclose");
					float alphaChange = rs2.getFloat("alphachange");
					float gap = rs2.getFloat("gap");
					float change = rs2.getFloat("change");

					Metric day = new Metric(symbol, start, end, BAR_SIZE.valueOf(duration), volume, adjOpen, adjClose, adjHigh, adjLow, gap, change, alphaClose, alphaChange);
					metricSequence.add(day);
				}
				metricSequences.add(metricSequence);
				rs2.close();
				s2.close();
			}
			rs1.close();
			s1.close();
			c.close();
			
			return metricSequences;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return metricSequences;
		}
	}

	public static void truncateIndexList() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "TRUNCATE TABLE " + Constants.INDEXLIST_TABLE;
			Statement s = c.createStatement();
			s.executeUpdate(q);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteStocksFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "DELETE FROM bar " +
						"WHERE symbol IN (SELECT symbol FROM indexlist WHERE index = 'NYSE' OR index = 'Nasdaq' OR index = 'DJIA' OR index = 'SP500' OR index = 'ETF' OR index = 'Stock Index') ";
			Statement s = c.createStatement();
			s.executeUpdate(q);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void dropMetricTables() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			for (String metric : Constants.METRICS) {
				String q = "DROP TABLE metric_" + metric;
				s.executeUpdate(q);
			}
			s.close();
			c.close();
		}
		catch (Exception e) {}
	}
	
	public static void dropMetricTable() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			String q = "DROP TABLE metrics";
			s.executeUpdate(q);
			s.close();
			c.close();
		}
		catch (Exception e) {}
	}

	public static void createMetricTables() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			for (String metric : Constants.METRICS) {
				String q = "CREATE TABLE metric_"
						+ metric
						+ " (symbol character varying(10) NOT NULL, date date NOT NULL, \"value\" real, "
						+ "CONSTRAINT metric_" + metric
						+ "_pk PRIMARY KEY (symbol, date))";
				s.execute(q);
			}
			s.close();
			c.close();
		}
		catch (Exception e) {
		}
	}
	
	public static void createMetricTable() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
	
			String q = "CREATE TABLE metrics " +
						"( " +
						"	name character varying(32) NOT NULL, " +
						"	symbol character varying(16) NOT NULL, " +
						"   start timestamp without time zone NOT NULL, " +
						"   \"end\" timestamp without time zone NOT NULL, " +
						"   duration character varying(16) NOT NULL, " +
						"   value real, " +
						"   CONSTRAINT metrics_pk PRIMARY KEY (name, symbol, start, \"end\") " +
						") " +
						"WITH (OIDS=FALSE);";
			s.execute(q);
			
			s.close();
			c.close();
		}
		catch (Exception e) {
		}
	}

	public static void createMetricIndex(String metric) {
		try {
			System.out.println("Creating indexes for " + metric);
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();			
			String q =  "CREATE INDEX metric_" + metric 
					+ "_date_symbol_index ON metric_" + metric
					+ " USING btree (date, symbol); "
					+ "CREATE INDEX metric_" + metric 
					+ "_symbol_value_index ON metric_" + metric
					+ " USING btree (symbol, value); "
					+ "CREATE INDEX metric_" + metric
					+ "_value_index ON metric_" + metric
					+ " USING btree (value); ";
			s.execute(q);
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void createMetricTableIndexes() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();			
			String q =  "CREATE INDEX metrics_duration_index ON metrics USING btree (duration COLLATE pg_catalog.\"default\"); "
					+ "CREATE INDEX metrics_end_index ON metrics USING btree (\"end\"); "
					+ "CREATE INDEX metrics_name_index ON metrics USING btree (name COLLATE pg_catalog.\"default\"); "
					+ "CREATE INDEX metrics_start_index ON metrics USING btree (start); "
					+ "CREATE INDEX metrics_symbol_index ON metrics USING btree (symbol COLLATE pg_catalog.\"default\"); "
					+ "CREATE INDEX metrics_value_index ON metrics USING btree (value);";
			s.execute(q);
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertMetrics(ArrayList<String> records) {
		try {
			String insertQuery = "INSERT INTO metrics(name, symbol, start, \"end\", duration, value) VALUES ";

			StringBuilder sb = new StringBuilder();
			for (String record : records) {
				System.out.println(record);
				sb.append(record + ", ");
			}
			String valuesPart = sb.toString();
			valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
			insertQuery = insertQuery + valuesPart;

			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			s.executeUpdate(insertQuery);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertIntoMetrics(Metric metric) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// First see if this bar exists in the DB
			String q = "SELECT * FROM metrics WHERE name = ? AND symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, metric.name);
			s.setString(2, metric.symbol);
			s.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
			s.setString(4, metric.duration.toString());
			
			ResultSet rs = s.executeQuery();
			boolean exists = false;
			while (rs.next()) {
				exists = true;
				break;
			}
			s.close();
			
			// If it doesn't exist, insert it
			if (!exists) {
				String q2 = "INSERT INTO metrics(name, symbol, start, \"end\", duration, value) " + 
							"VALUES (?, ?, ?, ?, ?, ?)";
				PreparedStatement s2 = c.prepareStatement(q2);
				s2.setString(1, metric.name);
				s2.setString(2, metric.symbol);
				s2.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
				s2.setTimestamp(4, new java.sql.Timestamp(metric.end.getTime().getTime()));
				s2.setString(5, metric.duration.toString());
				if (metric.value == null) {
					s2.setNull(6, java.sql.Types.FLOAT);
				}
				else {
					s2.setFloat(6, metric.value);
				}
				
				s2.executeUpdate();
				s2.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param metricName
	 * @param duration - optional
	 * @param type - "min" or "max"
	 * @param percentile - 0-100
	 * @return
	 */
	public static float getMetricValueAtPercentile(String metricName, Constants.BAR_SIZE duration, String type, int percentile) {
		float result = 0f;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Create query parameters & clauses
			String durationClause = "";
			if (duration != null) {
				durationClause = "AND duration = '" + duration.toString() + "' ";
			}
			
			String sort1 = "ASC";
			String sort2 = "DESC";
			if (type.equals("max")) {
				sort1 = "DESC";
				sort2 = "ASC";
			}
			
			float divisor = 100f / (float)percentile;
			
			String q = "SELECT * FROM (SELECT value FROM metrics WHERE name = ? " + durationClause + " ORDER BY value " + sort1 + " LIMIT (SELECT COUNT(*) / ? FROM metrics WHERE name = ? " + durationClause + " )) t ORDER BY value " + sort2 + " LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, metricName);
			s.setFloat(2, divisor);
			s.setString(3, metricName);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				result = rs.getFloat(1);
				break;
			}
			rs.close();
			s.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void insertOrUpdateIntoMetrics(ArrayList<Metric> metrics) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q2 = "INSERT INTO metrics(name, symbol, start, \"end\", duration, value) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement s2 = c.prepareStatement(q2);

			String q3 = "UPDATE metrics SET value = ? WHERE name = ? AND symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s3 = c.prepareStatement(q3);

			int numInserts = 0;
			int numUpdates = 0;
			
			// Cache the bars we already have for this metric sequence
			ArrayList<String> starts = new ArrayList<String>();
			if (metrics != null && metrics.size() > 0) {
				String q0 = "SELECT start FROM metrics WHERE name = ? AND symbol = ? AND duration = ?";
				PreparedStatement s0 = c.prepareStatement(q0);
				s0.setString(1, metrics.get(0).name);
				s0.setString(2, metrics.get(0).symbol);
				s0.setString(3, metrics.get(0).duration.toString());
				
				ResultSet rs0 = s0.executeQuery();
				while (rs0.next()) {
					Timestamp ts = rs0.getTimestamp("start");
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(ts.getTime());
					starts.add(cal.getTime().toString());
				}
				rs0.close();
				s0.close();
			}
			
			
			for (Metric metric : metrics) {
				if (metric.value != null) {
					// First see if this bar exists in the DB.  This was too slow so I put the caching code up above
//					String q = "SELECT * FROM metrics WHERE name = ? AND symbol = ? AND start = ? AND duration = ?";
//					PreparedStatement s = c.prepareStatement(q);
//					s.setString(1, metric.name);
//					s.setString(2, metric.symbol);
//					s.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
//					s.setString(4, metric.duration.toString());
//					
//					ResultSet rs = s.executeQuery();
//					boolean exists = false;
//					while (rs.next()) {
//						exists = true;
//						break;
//					}
//					rs.close();
//					s.close();
					
					boolean exists = false;
					if (starts.contains(metric.start.getTime().toString())) {
						exists = true;
					}
					
					// If it doesn't exist, insert it
					if (!exists) {
						s2.setString(1, metric.name);
						s2.setString(2, metric.symbol);
						s2.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
						s2.setTimestamp(4, new java.sql.Timestamp(metric.end.getTime().getTime()));
						s2.setString(5, metric.duration.toString());
						if (metric.value == null) {
							s2.setNull(6, java.sql.Types.FLOAT);
						}
						else {
							s2.setFloat(6, metric.value);
						}
						s2.addBatch();
						numInserts++;
					}
					else { // Otherwise it does exist, so update it
						if (metric.value == null) {
							s3.setNull(1, java.sql.Types.FLOAT);
						}
						else {
							s3.setFloat(1, metric.value);
						}
						s3.setString(2, metric.name);
						s3.setString(3, metric.symbol);
						s3.setTimestamp(4, new java.sql.Timestamp(metric.start.getTime().getTime()));
						s3.setString(5, metric.duration.toString());
						s3.addBatch();
						numUpdates++;
					}
				}
			}
			
			if (numInserts > 0) {
				s2.executeBatch();
//				System.out.println("# Inserts: " + numInserts);
			}
			if (numUpdates > 0) {
//				System.out.println("# Updates: " + numUpdates);
				s3.executeBatch();
			}
			s2.close();
			s3.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getSectorList() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("All");
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT DISTINCT sector FROM sectorandindustry";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				list.add(rs.getString("sector"));
			}
			rs.close();
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(list);
		return list;
	}

	public static ArrayList<String> getIndustryList() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("All");
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT DISTINCT industry FROM sectorandindustry";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				list.add(rs.getString("industry"));
			}
			rs.close();
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(list);
		return list;
	}
	
	public static ArrayList<String> getDistinctSymbolDurations(ArrayList<String> indexes) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			if (indexes == null || indexes.size() == 0) {
				return list;
			}
			
			String indexClause = "WHERE i.index IN (";
			for (String index : indexes) {
				indexClause += "'" + index + "',";
			}
			indexClause = indexClause.substring(0, indexClause.length() - 1) + ")";
		
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT b.symbol, b.duration, COUNT(b.*) AS barcount " +
						"FROM bar b " +
						"INNER JOIN indexlist i ON b.symbol = i.symbol " +
						indexClause +
						"GROUP BY b.symbol, b.duration " + 
						"ORDER BY b.symbol, b.duration";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				list.add(rs.getString("duration") + " - " + rs.getString("symbol") + " (" + rs.getInt("barcount") + ")");
			}
			rs.close();
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(list);
		return list;
	}

	public static void saveSearchResults(float bullScore, float bearScore) {
		try {
			ParameterSingleton ps = ParameterSingleton.getInstance();
			
			Connection c = ConnectionSingleton.getInstance().getConnection();

			String insertQuery = "INSERT INTO searchresults( "
					+ "bullscore, bearscore, rundate, buy1, buy2, sell, sellop, sellvalue, stop, stopvalue, "
					+ "fromdate, todate, xres, yres, liquidity, volatility, price, sector, "
					+ "industry, nyse, nasdaq, djia, sp500, etf, bitcoin) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement pst = c.prepareStatement(insertQuery);

			pst.setFloat(1, bullScore);
			pst.setFloat(2, bearScore);
			pst.setTimestamp(3, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
			pst.setString(4, ps.getxAxisMetric());
			pst.setString(5, ps.getyAxisMetric());
			pst.setString(6, ps.getSellMetric());
			pst.setString(7, ps.getSellOperator());
			pst.setFloat(8, ps.getSellValue());
			pst.setString(9, ps.getStopMetric());
			if (ps.getStopValue() == null)
				pst.setNull(10, java.sql.Types.FLOAT);
			else	
				pst.setFloat(10, ps.getStopValue());
			pst.setDate(11, new java.sql.Date(ps.getFromCal().getTime().getTime()));
			pst.setDate(12, new java.sql.Date(ps.getToCal().getTime().getTime()));
			pst.setInt(13, ps.getxRes());
			pst.setInt(14, ps.getyRes());
			pst.setFloat(15, ps.getMinLiquidity());
			pst.setFloat(16, ps.getMaxVolatility());
			pst.setFloat(17, ps.getMinPrice());
			pst.setString(18, ps.getSector());
			pst.setString(19, ps.getIndustry());
			pst.setBoolean(20, ps.isNyse());
			pst.setBoolean(21, ps.isNasdaq());
			pst.setBoolean(22, ps.isDjia());
			pst.setBoolean(23, ps.isSp500());
			pst.setBoolean(24, ps.isEtf());
			pst.setBoolean(25, ps.isBitcoin());

			pst.executeUpdate();
			pst.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateBullFitness(String type, String metric, Float fitness) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE searchfitness SET bullfitness = ? WHERE type = ? AND metric = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, fitness);
			ps.setString(2, type);
			ps.setString(3, metric);
			ps.executeUpdate();
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateBearFitness(String type, String metric, Float fitness) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE searchfitness SET bearfitness = ? WHERE type = ? AND metric = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, fitness);
			ps.setString(2, type);
			ps.setString(3, metric);
			ps.executeUpdate();
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static float loadBullFitness(String type, String metric) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT bullfitness FROM searchfitness WHERE type = ? AND metric = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, type);
			ps.setString(2, metric);
			ResultSet rs = ps.executeQuery();
			float fitness = 100f;
			if (type.equals("stop")) {
				fitness = 500f;
			}
			while (rs.next()) {
				fitness = rs.getFloat(1);
			}
			rs.close();
			ps.close();
			c.close();
			
			return fitness;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return 100f;
		}
	}
	
	public static float loadBearFitness(String type, String metric) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT bearfitness FROM searchfitness WHERE type = ? AND metric = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, type);
			ps.setString(2, metric);
			ResultSet rs = ps.executeQuery();
			float fitness = 100f;
			while (rs.next()) {
				fitness = rs.getFloat(1);
			}
			rs.close();
			ps.close();
			c.close();
			return fitness;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return 100f;
		}
	}

	public static boolean seeIfSearchComboHasBeenUsed(String buy1, String buy2,
			String sell, String sellOp, Float sellValue, String stop, Float stopValue, Date from, Date to) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT COUNT(*) c FROM searchresults WHERE buy1 = ? AND buy2 = ? AND "
					+ "sell = ?  AND sellop = ? AND sellValue = ? AND stop = ? AND stopValue = ? AND "
					+ "fromdate = ? AND todate = ?";

			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, buy1);
			ps.setString(2, buy2);
			ps.setString(3, sell);
			ps.setString(4, sellOp);
			ps.setFloat(5, sellValue);
			ps.setString(6, stop);
			ps.setFloat(7, stopValue);
			ps.setDate(8, from);
			ps.setDate(9, to);

			ResultSet rs = ps.executeQuery();
			int numResults = 0;
			while (rs.next()) {
				numResults = rs.getInt("c");
				break;
			}
			rs.close();
			ps.close();
			c.close();
			
			if (numResults > 0)
				return true;
			else
				return false;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void deletePartialsFromBasic() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "DELETE FROM basicr WHERE partial = true";
			PreparedStatement ps = c.prepareStatement(q);
			ps.executeUpdate();
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMostRecentBar(String symbol, String duration) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM bar WHERE (symbol, duration, start) IN ( " +
						"SELECT symbol, duration, MAX(start) FROM bar " +
						"WHERE symbol = ? AND duration = ? " +
						"GROUP BY symbol, duration)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, duration);
			ps.executeUpdate();
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMostRecentMetrics(String symbol, String duration) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM metrics WHERE (symbol, duration, start) IN ( " +
						"SELECT symbol, duration, MAX(start) AS start FROM metrics " +
						"WHERE symbol = ? AND duration = ? " +
						"GROUP BY symbol, duration)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, duration);
			ps.executeUpdate();
			ps.close();
			
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMostRecentMetrics(String symbol, String duration, ArrayList<String> metrics) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM metrics WHERE (symbol, duration, start) IN ( " +
						"SELECT symbol, duration, MAX(start) AS start FROM metrics " +
						"WHERE symbol = ? AND duration = ? " +
						"GROUP BY symbol, duration) ";
			String metricsTerm = "";
			if (metrics != null && metrics.size() > 0) {
				metricsTerm = "AND name IN (";
				
				for (String metric : metrics) {
					metricsTerm += "'" + metric + "',";
				}
				
				metricsTerm = metricsTerm.substring(0, metricsTerm.length() - 1) + ")";
			}
			q += metricsTerm;
			
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, duration);
			ps.executeUpdate();
			ps.close();
			
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteMostRecentTradingDayFromBasic(ArrayList<String> symbolList) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			int counter = 0;
			for (String symbol:symbolList) {
				sb.append("'").append(symbol).append("', ");
				counter++;
			}
			String symbolPart = sb.toString();
			if (counter > 0) {
				symbolPart = symbolPart.substring(0, symbolPart.length() - 2);
				symbolPart += ")";
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "DELETE FROM basicr WHERE date = (SELECT MAX(date) FROM tradingdays WHERE date <= now())";
			if (counter > 0) {
				q += " AND symbol IN " + symbolPart;
			}

			PreparedStatement ps = c.prepareStatement(q);
			ps.executeUpdate();
			ps.close();
			c.close();
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteHolidaysForStocksFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM bar " +
						"WHERE symbol IN (SELECT symbol FROM indexlist WHERE index = 'NYSE' OR index = 'Nasdaq' OR index = 'DJIA' OR index = 'SP500' OR index = 'ETF' OR index = 'Stock Index') " +
						"AND start IN (SELECT * FROM (SELECT * FROM generate_series('2012-01-01'::date, now()::date, '1 day') AS alldates) t WHERE alldates NOT IN (SELECT * FROM tradingdays))";
		
			PreparedStatement ps = c.prepareStatement(q);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public static void deleteMostRecentTradingDayFromMetricTables(ArrayList<BarKey> barKeys, ArrayList<String> usedMetrics) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			int counter = 0;
			for (BarKey bk : barKeys) {
				sb.append("'").append(bk.symbol).append("', ");
				counter++;
			}
			String symbolPart = sb.toString();
			if (counter > 0) {
				symbolPart = symbolPart.substring(0, symbolPart.length() - 2);
				symbolPart += ")";
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			for (String metric:usedMetrics) {
				try {
					String q = "DELETE FROM metric_" + metric + " WHERE date = (SELECT MAX(date) FROM tradingdays WHERE date <= now())";
					if (counter > 0) {
						q += " AND symbol IN " + symbolPart;
					}
					
					Statement s = c.createStatement();
					s.executeUpdate(q);
					s.close();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertBasicPartials(ArrayList<String> records) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO basicr (symbol, date, volume, adjopen, adjclose, adjhigh, adjlow, change, gap, partial) VALUES ";
			StringBuilder sb = new StringBuilder();
			for (String record : records) {
				sb.append(record);
				sb.append(", ");
			}
			String valuesPart = sb.toString();
			valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
			q = q + valuesPart;

			Statement s = c.createStatement();
			s.executeUpdate(q);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Calendar getLastestTradingDay() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(date) d FROM tradingdays WHERE date <= now()";
			PreparedStatement ps = c.prepareStatement(q);

			ResultSet rs = ps.executeQuery();
			java.sql.Date date = null;
			Calendar cal = Calendar.getInstance();
			while (rs.next()) {
				date = rs.getDate("d");
				break;
			}
			rs.close();
			ps.close();
			c.close();
			
			cal.setTime(date);
			return cal;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Calendar getMaxStartFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(start) AS d FROM bar";
			PreparedStatement ps = c.prepareStatement(q);

			ResultSet rs = ps.executeQuery();
			java.sql.Timestamp ts = null;
			Calendar cal = Calendar.getInstance();
			while (rs.next()) {
				cal.setTimeInMillis(rs.getTimestamp(1).getTime());
				break;
			}

			rs.close();
			ps.close();
			c.close();
			
			return cal;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void insertRealtimeMetrics(Calendar maxStartFromBar, ArrayList<Metric> metricSequence) {
		try {
			insertOrUpdateIntoMetrics(metricSequence);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveMapCell(int tradeID, MapCell mc) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO tradeorigins(tradeid, a_numpositions, a_percentpositions, a_meanreturn, a_geomeanreturn, " +
		            "a_medianreturn, a_meanwinpercent, a_meanpositionduration, a_maxdrawdown, " +
		            "a_geomeanperdayreturn, a_sharperatio, a_sortinoratio, a_alphameanreturn, " + 
		            "a_alphageomeanreturn, a_alphamedianreturn, a_alphameanwinpercent, " + 
		            "a_alphageomeanperdayreturn, m_numpositions, m_percentpositions, " + 
		            "m_meanreturn, m_geomeanreturn, m_medianreturn, m_meanwinpercent, " + 
		            "m_meanpositionduration, m_geomeanperdayreturn, s_numpositions, " + 
		            "s_percentpositions, s_meanreturn, s_geomeanreturn, s_medianreturn, " + 
		            "s_meanwinpercent, s_meanpositionduration, s_geomeanperdayreturn, " + 
		            "e_numpositions, e_percentpositions, e_meanreturn, e_geomeanreturn, " + 
		            "e_medianreturn, e_meanwinpercent, e_meanpositionduration, e_geomeanperdayreturn) " +
		            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q);
			
			HashMap<String, Float> mvh = mc.getMetricValueHash();
			s.setInt(1, tradeID);
			s.setInt(2, mvh.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS).intValue());
			s.setFloat(3, mvh.get(Constants.MAP_COLOR_OPTION_ALL_PERCENT_POSITIONS));
			s.setFloat(4, mvh.get(Constants.MAP_COLOR_OPTION_ALL_MEAN_RETURN));
			s.setFloat(5, mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_RETURN));
			s.setFloat(6, mvh.get(Constants.MAP_COLOR_OPTION_ALL_MEDIAN_RETURN));
			s.setFloat(7, mvh.get(Constants.MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT));
			s.setFloat(8, mvh.get(Constants.MAP_COLOR_OPTION_ALL_MEAN_POSITION_DURATION));
			s.setFloat(9, mvh.get(Constants.MAP_COLOR_OPTION_ALL_MAX_DRAWDOWN));
			s.setFloat(10, mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR));
			s.setFloat(11, mvh.get(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO));
			s.setFloat(12, mvh.get(Constants.MAP_COLOR_OPTION_ALL_SORTINO_RATIO));
			
			s.setFloat(13, mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_RETURN));
			s.setFloat(14, mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_RETURN));
			s.setFloat(15, mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEDIAN_RETURN));
			s.setFloat(16, mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT));
			s.setFloat(17, mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR));
			
			s.setInt(18, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_NUM_POSITIONS).intValue());
			s.setFloat(19, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_PERCENT_POSITIONS));
			s.setFloat(20, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_MEAN_RETURN));
			s.setFloat(21, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_GEOMEAN_RETURN));
			s.setFloat(22, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_MEDIAN_RETURN));
			s.setFloat(23, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_MEAN_WIN_PERCENT));
			s.setFloat(24, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_MEAN_POSITION_DURATION));
			s.setFloat(25, mvh.get(Constants.MAP_COLOR_OPTION_METRIC_GEOMEAN_PER_BAR));
			
			s.setInt(26, mvh.get(Constants.MAP_COLOR_OPTION_STOP_NUM_POSITIONS).intValue());
			s.setFloat(27, mvh.get(Constants.MAP_COLOR_OPTION_STOP_PERCENT_POSITIONS));
			s.setFloat(28, mvh.get(Constants.MAP_COLOR_OPTION_STOP_MEAN_RETURN));
			s.setFloat(29, mvh.get(Constants.MAP_COLOR_OPTION_STOP_GEOMEAN_RETURN));
			s.setFloat(30, mvh.get(Constants.MAP_COLOR_OPTION_STOP_MEDIAN_RETURN));
			s.setFloat(31, mvh.get(Constants.MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT));
			s.setFloat(32, mvh.get(Constants.MAP_COLOR_OPTION_STOP_MEAN_POSITION_DURATION));
			s.setFloat(33, mvh.get(Constants.MAP_COLOR_OPTION_STOP_GEOMEAN_PER_BAR));
			
			s.setInt(34, mvh.get(Constants.MAP_COLOR_OPTION_END_NUM_POSITIONS).intValue());
			s.setFloat(35, mvh.get(Constants.MAP_COLOR_OPTION_END_PERCENT_POSITIONS));
			s.setFloat(36, mvh.get(Constants.MAP_COLOR_OPTION_END_MEAN_RETURN));
			s.setFloat(37, mvh.get(Constants.MAP_COLOR_OPTION_END_GEOMEAN_RETURN));
			s.setFloat(38, mvh.get(Constants.MAP_COLOR_OPTION_END_MEDIAN_RETURN));
			s.setFloat(39, mvh.get(Constants.MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT));
			s.setFloat(40, mvh.get(Constants.MAP_COLOR_OPTION_END_MEAN_POSITION_DURATION));
			s.setFloat(41, mvh.get(Constants.MAP_COLOR_OPTION_END_GEOMEAN_PER_BAR));

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertIntoTick(ArrayList<String> records) {
		try {
			if (records != null && records.size() > 0) {
				Connection c = ConnectionSingleton.getInstance().getConnection();
				String q = "INSERT INTO tick(symbol, price, volume, \"timestamp\") VALUES ";
				StringBuilder sb = new StringBuilder();
				for (String record : records) {
					sb.append("(" + record + "), ");
				}
				String valuesPart = sb.toString();
				valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
				q = q + valuesPart;
	
				Statement s = c.createStatement();
				s.executeUpdate(q);
				s.close();
				c.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Calendar getTickEarliestTick(String symbol) {
		Calendar earliestTick = Calendar.getInstance();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT timestamp FROM tick WHERE symbol = ? ORDER BY \"timestamp\" LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				earliestTick.setTimeInMillis(rs.getTimestamp(1).getTime());
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return earliestTick;
	}
	
	public static Calendar getTickLatestTick(String symbol) {
		Calendar latestTick = Calendar.getInstance();
		latestTick.set(2000, 0, 1); // Just make it old in case there isn't any tick data
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT timestamp FROM tick WHERE symbol = ? ORDER BY \"timestamp\" DESC LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				latestTick.setTimeInMillis(rs.getTimestamp(1).getTime());
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return latestTick;
	}
	
	public static void insertIntoBar(String symbol, float open, float close, float high, float low, float vwap, float volume, int numTrades, float change, float gap, Calendar start, Calendar end, BAR_SIZE barSize, boolean partial) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO bar(symbol, open, close, high, low, vwap, volume, numtrades, change, gap, start, \"end\", duration, partial) " + 
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			s.setFloat(2, open);
			s.setFloat(3, close);
			s.setFloat(4, high);
			s.setFloat(5, low);
			s.setFloat(6, vwap);
			s.setFloat(7, volume);
			s.setInt(8, numTrades);
			s.setFloat(9, change);
			s.setFloat(10, gap);
			s.setTimestamp(11, new java.sql.Timestamp(start.getTime().getTime()));
			s.setTimestamp(12, new java.sql.Timestamp(end.getTime().getTime()));
			s.setString(13, barSize.toString());
			s.setBoolean(14, partial);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			System.err.println("Fear not - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts if the bar does not exist. Updates if it's marked as partial or if the numTrades column doesn't have data (i.e. the record didn't have tick data when it was made)
	 * 
	 * @param bar
	 */
	public static void insertOrUpdateIntoBar(Bar bar) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
		
			// First see if this bar exists in the DB
			String q = "SELECT partial, numtrades FROM bar WHERE symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, bar.symbol);
			s.setTimestamp(2, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
			s.setString(3, bar.duration.toString());
			
			ResultSet rs = s.executeQuery();
			boolean exists = false;
			boolean partial = false;
			Object numTrades = null;
			while (rs.next()) {
				exists = true;
				partial = rs.getBoolean("partial");
				numTrades = rs.getObject("numtrades");
				break;
			}
			s.close();
			
			// If there are no trades for this existing bar, say its partial so it can be updated with bar data that contains this (if coming from tick data)
			if (numTrades == null) {
				partial = true;
			}
		
			// If it doesn't exist, insert it
			if (!exists) {
				String q2 = "INSERT INTO bar(symbol, open, close, high, low, vwap, volume, numtrades, change, gap, start, \"end\", duration, partial) " + 
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement s2 = c.prepareStatement(q2);
				s2.setString(1, bar.symbol);
				s2.setFloat(2, bar.open);
				s2.setFloat(3, bar.close);
				s2.setFloat(4, bar.high);
				s2.setFloat(5, bar.low);
				s2.setFloat(6, bar.vwap);
				s2.setFloat(7, bar.volume);
				if (bar.numTrades == null) {
					s2.setNull(8, Types.INTEGER);
				}
				else {
					s2.setInt(8, bar.numTrades);
				}
				if (bar.change == null) {
					s2.setNull(9, Types.FLOAT);
				}
				else {
					s2.setFloat(9, bar.change);
				}
				if (bar.gap == null) {
					s2.setNull(10, Types.FLOAT);
				}
				else {
					s2.setFloat(10, bar.gap);
				}
				s2.setTimestamp(11, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
				s2.setTimestamp(12, new java.sql.Timestamp(bar.periodEnd.getTime().getTime()));
				s2.setString(13, bar.duration.toString());
				s2.setBoolean(14, bar.partial);
				
				s2.executeUpdate();
				s2.close();
			}
			// It exists and it's partial, so we need to update it.
			else if (partial) {
				String q3 = "UPDATE bar SET symbol = ?, open = ?, close = ?, high = ?, low = ?, vwap = ?, volume = ?, numtrades = ?, change = ?, gap = ?, start = ?, \"end\" = ?, duration = ?, partial = ? " +
							"WHERE symbol = ? AND start = ? AND duration = ?";
				PreparedStatement s3 = c.prepareStatement(q3);
				s3.setString(1, bar.symbol);
				s3.setFloat(2, bar.open);
				s3.setFloat(3, bar.close);
				s3.setFloat(4, bar.high);
				s3.setFloat(5, bar.low);
				s3.setFloat(6, bar.vwap);
				s3.setFloat(7, bar.volume);
				if (bar.numTrades == null) {
					s3.setNull(8, Types.INTEGER);
				}
				else {
					s3.setInt(8, bar.numTrades);
				}
				if (bar.change == null) {
					s3.setNull(9, Types.FLOAT);
				}
				else {
					s3.setFloat(9, bar.change);
				}
				if (bar.gap == null) {
					s3.setNull(10, Types.FLOAT);
				}
				else {
					s3.setFloat(10, bar.gap);
				}
				s3.setTimestamp(11, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
				s3.setTimestamp(12, new java.sql.Timestamp(bar.periodEnd.getTime().getTime()));
				s3.setString(13, bar.duration.toString());
				s3.setBoolean(14, bar.partial);
				s3.setString(15, bar.symbol);
				s3.setTimestamp(16, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
				s3.setString(17, bar.duration.toString());
				
				s3.executeUpdate();
				s3.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			System.err.println("Fear not - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
			e.printStackTrace();
		}
	}
	
	public static void insertOrUpdateIntoMetricCalcEssentials(MetricKey mk, HashMap<String, Object> mce) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// First check to see if this MCE exists in the metriccalcessentials table
			String q = "SELECT * FROM metriccalcessentials WHERE name = ? AND symbol = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, mk.name);
			s.setString(2, mk.symbol);
			s.setString(3, mk.duration.toString());
			
			ResultSet rs = s.executeQuery();
			boolean exists = false;
			while (rs.next()) {
				exists = true;
				break;
			}
			s.close();
			
			// If it doesn't exist, insert it
			if (!exists) {
				Iterator i = mce.entrySet().iterator();
				while (i.hasNext()) {
					// Get the VarName and VarValue out of the MCE hash
					Map.Entry pair = (Map.Entry)i.next();
					String varName = pair.getKey().toString();
					Array varValue = null;
					Object o = pair.getValue();
					if (!(o instanceof Calendar)) {
						if (o instanceof Float || o instanceof Integer || o instanceof Double) {
							varValue = c.createArrayOf("float", new Float[] {Float.parseFloat(o.toString())});
						}
						else if (o instanceof List) {
							List l = (List)o;
							varValue = c.createArrayOf("float", l.toArray());
						}
						
						String q1 = "INSERT INTO metriccalcessentials(name, symbol, duration, start, varname, varvalue) VALUES (?, ?, ?, ?, ?, ?)";
						PreparedStatement s1 = c.prepareStatement(q1);
						s1.setString(1, mk.name);
						s1.setString(2, mk.symbol);
						s1.setString(3, mk.duration.toString());
						Calendar start = (Calendar)mce.get("start");
						s1.setTimestamp(4, new java.sql.Timestamp(start.getTime().getTime()));
						s1.setString(5, varName);
						s1.setArray(6, varValue);
						
						s1.executeUpdate();
						s1.close();
					}
				}
			}
			else { // It does exist, update it
				Iterator i = mce.entrySet().iterator();
				while (i.hasNext()) {
					// Get the VarName and VarValue out of the MCE hash
					Map.Entry pair = (Map.Entry)i.next();
					String varName = pair.getKey().toString();
					Array varValue = null;
					Object o = pair.getValue();
					if (!(o instanceof Calendar)) {
						if (o instanceof Float || o instanceof Integer || o instanceof Double) {
							varValue = c.createArrayOf("float", new Float[] {Float.parseFloat(o.toString())});
						}
						else if (o instanceof List) {
							List l = (List)o;
							varValue = c.createArrayOf("float", l.toArray());
						}
						
						String q1 = "UPDATE metriccalcessentials SET start = ?, varname = ?, varvalue = ? WHERE name = ? AND symbol = ? AND duration = ? AND varname = ?";
						PreparedStatement s1 = c.prepareStatement(q1);
						Calendar start = (Calendar)mce.get("start");
						s1.setTimestamp(1, new java.sql.Timestamp(start.getTime().getTime()));
						s1.setString(2, varName);
						s1.setArray(3, varValue);
						s1.setString(4, mk.name);
						s1.setString(5, mk.symbol);
						s1.setString(6, mk.duration.toString());
						s1.setString(7, varName);
						
						s1.executeUpdate();
						s1.close();
					}
				}
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<HashMap<String, Object>> getTrainingSet(BarKey bk, Calendar start, Calendar end, ArrayList<String> metricNames) {
		ArrayList<HashMap<String, Object>> trainingSet = new ArrayList<HashMap<String, Object>>();
		try {
			// Create metric clauses
			String metricColumnClause = "";
			for (int a = 0; a < metricNames.size(); a++) {
				metricColumnClause += ", m" + a + ".value AS m" + a + " ";
			}
			
			String metricJoinClause = "";
			for (int a = 0; a < metricNames.size(); a++) {
				String metricName = metricNames.get(a);
				metricJoinClause += "LEFT OUTER JOIN metrics m" + a + " ON b.symbol = m" + a + ".symbol AND b.duration = m" + a + ".duration AND b.start = m" + a + ".start AND m" + a + ".name = '" + metricName + "' ";
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();

			String q = 	"SELECT b.*, date_part('hour', b.start) AS hour " + metricColumnClause + 
						"FROM bar b " + metricJoinClause +
						"WHERE b.symbol = ? AND b.duration = ? AND b.start >= ? AND b.\"end\" <= ? ORDER BY b.start DESC";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, bk.symbol);
			s.setString(2, bk.duration.toString());
			s.setTimestamp(3, new Timestamp(start.getTimeInMillis()));
			s.setTimestamp(4, new Timestamp(end.getTimeInMillis()));
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> record = new HashMap<String, Object>();
				
				float close = rs.getFloat("close");
				float high = rs.getFloat("high");
				float low = rs.getFloat("low");
				int hour = rs.getInt("hour");
				record.put("close", close);
				record.put("high", high);
				record.put("low", low);
				record.put("hour", hour);
				for (int a = 0; a < metricNames.size(); a++) {
					String metricName = metricNames.get(a);
					float metricValue = rs.getFloat("m" + a);
					record.put(metricName, metricValue);
				}
				
				trainingSet.add(record);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return trainingSet;
	}
	
	public static ArrayList<HashMap<String, Object>> getBarAndMetricInfo() {
		ArrayList<HashMap<String, Object>> barAndMetricInfo = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = 	"SELECT b.*, m.metricmin, m.metricmax, m.metricage FROM ( " +
						"SELECT symbol, duration, MIN(start) AS barmin, MAX(start) AS barmax, AGE(now(), MAX(start)) AS barage, COUNT(*) AS barcount " + 
						"FROM bar GROUP BY symbol, duration ORDER BY symbol, duration) b " +
						"LEFT OUTER JOIN (SELECT symbol, duration, MIN(start) AS metricmin, MAX(start) AS metricmax, AGE(now(), MAX(start)) AS metricage FROM metrics GROUP BY symbol, duration) m " +
						"ON b.symbol = m.symbol AND b.duration = m.duration";
			PreparedStatement ps = c.prepareStatement(q);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> record = new HashMap<String, Object>();
				
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				Timestamp barminTS = rs.getTimestamp("barmin");
				Calendar barmin = Calendar.getInstance();
				barmin.setTimeInMillis(barminTS.getTime());
				Timestamp barmaxTS = rs.getTimestamp("barmax");
				Calendar barmax = Calendar.getInstance();
				barmax.setTimeInMillis(barmaxTS.getTime());
				String barage = rs.getString("barage");
				int barcount = rs.getInt("barcount");
				
				Timestamp metricminTS = rs.getTimestamp("metricmin");
				Calendar metricmin = Calendar.getInstance();
				if (metricminTS != null) {
					metricmin.setTimeInMillis(metricminTS.getTime());
				}
				else {
					metricmin = null;
				}
				
				Timestamp metricmaxTS = rs.getTimestamp("metricmax");
				Calendar metricmax = Calendar.getInstance();
				if (metricmaxTS != null) {
					metricmax.setTimeInMillis(metricmaxTS.getTime());
				}
				else {
					metricmax = null;
				}
				
				String metricage = rs.getString("metricage");
				
				record.put("symbol", symbol);
				record.put("duration", duration);
				record.put("barmin", sdf.format(barmin.getTime()));
				record.put("barmax", sdf.format(barmax.getTime()));
				record.put("barage", barage);
				record.put("barcount", barcount);
				if (metricmin != null) {
					record.put("metricmin", sdf.format(metricmin.getTime()));
				}
				else {
					record.put("metricmin", null);
				}
				if (metricmax != null) {
					record.put("metricmax", sdf.format(metricmax.getTime()));
				}
				else {
					record.put("metricmax", null);
				}
				record.put("metricage", metricage);
				barAndMetricInfo.add(record);
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return barAndMetricInfo;
	}
	
	public static ArrayList<Model> getModels(String whereClause) {
		ArrayList<Model> models = new ArrayList<Model>();
		try {
			if (whereClause == null) {
				whereClause = "";
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "SELECT * FROM models " + whereClause;
			PreparedStatement ps = c.prepareStatement(q);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String type = rs.getString("type");
				String modelFile = rs.getString("modelfile");
				String algo = rs.getString("algo");
				String params = rs.getString("params");
				String symbol = rs.getString("symbol");
				BAR_SIZE duration = BAR_SIZE.valueOf(rs.getString("duration"));
				boolean interbarData = rs.getBoolean("interbardata");
				Array metricArray = rs.getArray("metrics");
				String[] metrics = (String[])metricArray.getArray();
				ArrayList<String> metricList = new ArrayList<String>(Arrays.asList(metrics));
				Timestamp trainStartTS = rs.getTimestamp("trainstart");
				Calendar trainStart = Calendar.getInstance();
				trainStart.setTimeInMillis(trainStartTS.getTime());
				Timestamp trainEndTS = rs.getTimestamp("trainend");
				Calendar trainEnd = Calendar.getInstance();
				trainEnd.setTimeInMillis(trainEndTS.getTime());
				Timestamp testStartTS = rs.getTimestamp("teststart");
				Calendar testStart = Calendar.getInstance();
				testStart.setTimeInMillis(testStartTS.getTime());
				Timestamp testEndTS = rs.getTimestamp("testend");
				Calendar testEnd = Calendar.getInstance();
				testEnd.setTimeInMillis(testEndTS.getTime());
				String sellMetric = rs.getString("sellmetric");
				float sellMetricValue = rs.getFloat("sellmetricvalue");
				String stopMetric = rs.getString("stopmetric");
				float stopMetricValue = rs.getFloat("stopmetricvalue");
				int numBars = rs.getInt("numbars");
				int trainDatasetSize = rs.getInt("traindatasetsize");
				int trainTrueNegatives = rs.getInt("traintruenegatives");
				int trainFalseNegatives = rs.getInt("trainfalsenegatives");
				int trainFalsePositives = rs.getInt("trainfalsepositives");
				int trainTruePositives = rs.getInt("traintruepositives");
				double trainTruePositiveRate = rs.getDouble("traintruepositiverate");
				double trainFalsePositiveRate = rs.getDouble("trainfalsepositiverate");
				double trainCorrectRate = rs.getDouble("traincorrectrate");
				double trainKappa = rs.getDouble("trainKappa");
				double trainMeanAbsoluteError = rs.getDouble("trainmeanabsoluteerror");
				double trainRootMeanSquaredError = rs.getDouble("trainrootmeansquarederror");
				double trainRelativeAbsoluteError = rs.getDouble("trainrelativeabsoluteerror");
				double trainRootRelativeSquaredError = rs.getDouble("trainrootrelativesquarederror");
				double trainROCArea = rs.getDouble("trainrocarea");
				int testDatasetSize = rs.getInt("testdatasetsize");
				int testTrueNegatives = rs.getInt("testtruenegatives");
				int testFalseNegatives = rs.getInt("testfalsenegatives");
				int testFalsePositives = rs.getInt("testfalsepositives");
				int testTruePositives = rs.getInt("testtruepositives");
				double testTruePositiveRate = rs.getDouble("testtruepositiverate");
				double testFalsePositiveRate = rs.getDouble("testfalsepositiverate");
				double testCorrectRate = rs.getDouble("testcorrectrate");
				double testKappa = rs.getDouble("testKappa");
				double testMeanAbsoluteError = rs.getDouble("testmeanabsoluteerror");
				double testRootMeanSquaredError = rs.getDouble("testrootmeansquarederror");
				double testRelativeAbsoluteError = rs.getDouble("testrelativeabsoluteerror");
				double testRootRelativeSquaredError = rs.getDouble("testrootrelativesquarederror");
				double testROCArea = rs.getDouble("testrocarea");
				
				Model model = new Model(type, modelFile, algo, params, new BarKey(symbol, duration), interbarData, metricList,
						trainStart, trainEnd, testStart, testEnd, sellMetric,
						sellMetricValue, stopMetric, stopMetricValue, numBars, trainDatasetSize,
						trainTrueNegatives, trainFalseNegatives, trainFalsePositives, trainTruePositives,
						trainTruePositiveRate, trainFalsePositiveRate, trainCorrectRate, trainKappa,
						trainMeanAbsoluteError, trainRootMeanSquaredError, trainRelativeAbsoluteError,
						trainRootRelativeSquaredError, trainROCArea, testDatasetSize, testTrueNegatives,
						testFalseNegatives, testFalsePositives, testTruePositives, testTruePositiveRate,
						testFalsePositiveRate, testCorrectRate, testKappa, testMeanAbsoluteError,
						testRootMeanSquaredError, testRelativeAbsoluteError, testRootRelativeSquaredError,
						testROCArea);
				model.id = id;
				
				models.add(model);
			}
			
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return models;
	}
	
	public static int insertModel(Model m) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "INSERT INTO models( " +
			            "type, modelfile, algo, params, symbol, duration, interbardata, metrics, trainstart,  " +
			            "trainend, teststart, testend, sellmetric, sellmetricvalue, stopmetric,  " +
			            "stopmetricvalue, numbars, traindatasetsize, traintruenegatives,  " +
			            "trainfalsenegatives, trainfalsepositives, traintruepositives,  " +
			            "traintruepositiverate, trainfalsepositiverate, traincorrectrate,  " +
			            "trainkappa, trainmeanabsoluteerror, trainrootmeansquarederror,  " +
			            "trainrelativeabsoluteerror, trainrootrelativesquarederror, trainrocarea,  " +
			            "testdatasetsize, testtruenegatives, testfalsenegatives, testfalsepositives,  " +
			            "testtruepositives, testtruepositiverate, testfalsepositiverate,  " +
			            "testcorrectrate, testkappa, testmeanabsoluteerror, testrootmeansquarederror,  " +
			            "testrelativeabsoluteerror, testrootrelativesquarederror, testrocarea) " +
			            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, m.type);
			if (m.modelFile == null) {
				ps.setNull(2, Types.VARCHAR);
			}
			else {
				ps.setString(2, m.modelFile);
			}
			ps.setString(3, m.algo);
			if (m.params == null) {
				ps.setNull(4, Types.VARCHAR);
			}
			else {
				ps.setString(4, m.params);
			}
			ps.setString(5, m.bk.symbol);
			ps.setString(6, m.bk.duration.toString());
			ps.setBoolean(7, m.interBarData);
			ps.setArray(8, c.createArrayOf("text", m.metrics.toArray()));
			ps.setTimestamp(9, new Timestamp(m.trainStart.getTime().getTime()));
			ps.setTimestamp(10, new Timestamp(m.trainEnd.getTime().getTime()));
			ps.setTimestamp(11, new Timestamp(m.testStart.getTime().getTime()));
			ps.setTimestamp(12, new Timestamp(m.testEnd.getTime().getTime()));
			ps.setString(13, m.sellMetric);
			ps.setFloat(14, m.sellMetricValue);
			ps.setString(15, m.stopMetric);
			ps.setFloat(16, m.stopMetricValue);
			ps.setInt(17, m.numBars);
			ps.setInt(18, m.trainDatasetSize);
			ps.setInt(19, m.trainTrueNegatives);
			ps.setInt(20, m.trainFalseNegatives);
			ps.setInt(21, m.trainFalsePositives);
			ps.setInt(22, m.trainTruePositives);
			ps.setDouble(23, m.trainTruePositiveRate);
			ps.setDouble(24, m.trainFalsePositiveRate);
			ps.setDouble(25, m.trainCorrectRate);
			ps.setDouble(26, m.trainKappa);
			ps.setDouble(27, m.trainMeanAbsoluteError);
			ps.setDouble(28, m.trainRootMeanSquaredError);
			ps.setDouble(29, m.trainRelativeAbsoluteError);
			ps.setDouble(30, m.trainRootRelativeSquaredError);
			ps.setDouble(31, m.trainROCArea);
			ps.setInt(32, m.testDatasetSize);
			ps.setInt(33, m.testTrueNegatives);
			ps.setInt(34, m.testFalseNegatives);
			ps.setInt(35, m.testFalsePositives);
			ps.setInt(36, m.testTruePositives);
			ps.setDouble(37, m.testTruePositiveRate);
			ps.setDouble(38, m.testFalsePositiveRate);
			ps.setDouble(39, m.testCorrectRate);
			ps.setDouble(40, m.testKappa);
			ps.setDouble(41, m.testMeanAbsoluteError);
			ps.setDouble(42, m.testRootMeanSquaredError);
			ps.setDouble(43, m.testRelativeAbsoluteError);
			ps.setDouble(44, m.testRootRelativeSquaredError);
			ps.setDouble(45, m.testROCArea);
			
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int id = -1;
			if (rs.next()) {
				id = rs.getInt(1);
				m.id = id;
				m.modelFile = id + ".model";
			}
			rs.close();
			ps.close();
			c.close();
			
			return id;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static int getNextModelID() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT last_value FROM models_id_seq";
			PreparedStatement ps = c.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			int id = -1;
			if (rs.next()) {
				id =  rs.getInt(1) + 1;
			}
			rs.close();
			ps.close();
			c.close();
			return id;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static HashMap<String, Object> getMetricCalcEssentials(MetricKey mk) {
		HashMap<String, Object> mce = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();

			String q = "SELECT * FROM metriccalcessentials WHERE name = ? AND symbol = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, mk.name);
			s.setString(2, mk.symbol);
			s.setString(3, mk.duration.toString());
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				String varName = rs.getString("varname");
				Array varValue = rs.getArray("varvalue");
				Timestamp startTS = rs.getTimestamp("start");
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(startTS.getTime());
				
				Float[] values = (Float[])varValue.getArray();
				if (values.length == 1) {
					mce.put(varName, values[0]);
				}
				else {
					mce.put(varName, values);
				}
				mce.put("start", start);
			}
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (mce.size() == 0) {
			return null;
		}
		else {
			return mce;
		}
	}
	
	public static ArrayList<HashMap<String, Object>> getTickData(String symbol, Calendar periodStart, BAR_SIZE barSize) {
		ArrayList<HashMap<String, Object>> listOfRecords = new ArrayList<HashMap<String, Object>>();
		try {
			Calendar periodEnd = Calendar.getInstance();
			periodEnd.setTime(periodStart.getTime());
			switch (barSize) {
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_2M:
					periodEnd.add(Calendar.MINUTE, 2);
					break;
				case BAR_5M:
					periodEnd.add(Calendar.MINUTE, 5);
					break;
				case BAR_10M:
					periodEnd.add(Calendar.MINUTE, 10);
					break;
				case BAR_15M:
					periodEnd.add(Calendar.MINUTE, 15);
					break;
				case BAR_30M:
					periodEnd.add(Calendar.MINUTE, 30);
					break;
				case BAR_1H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case BAR_2H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 2);
					break;
				case BAR_4H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 4);
					break;
				case BAR_6H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 6);
					break;
				case BAR_8H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 8);
					break;
				case BAR_12H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 12);
					break;
				case BAR_1D:
					periodEnd.add(Calendar.DATE, 1);
					break;
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM tick " + 
						"WHERE symbol = ? AND \"timestamp\" >= '" + CalendarUtils.getSqlDateTimeString(periodStart) + "' AND \"timestamp\" < '" + CalendarUtils.getSqlDateTimeString(periodEnd) + "' ORDER BY \"timestamp\" ";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> record = new HashMap<String, Object>();
				record.put("price", rs.getFloat("price"));
				record.put("volume", rs.getFloat("volume"));
				record.put("start", periodStart);
				record.put("end", periodEnd);
				listOfRecords.add(record);
			}
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return listOfRecords;
	}
	
	public static float getTradingAccountCash() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT cash FROM tradingaccount";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			float cash = 0f;
			while (rs.next()) {
				cash = rs.getFloat("cash");
			}
			rs.close();
			s.close();
			c.close();
			return cash;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static float getSymbolRelativeVolatility(String symbol) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT p1.value / p2.value AS relvol " +
						"FROM metric_mvol100 p1 " +
						"INNER JOIN metric_mvol100 p2 " +
						"ON p1.date = p2.date AND p2.symbol = 'SPY' " +
						"WHERE p1.symbol = ? " +
						"ORDER BY p1.date DESC LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			ResultSet rs = s.executeQuery();
			float relvol = 1f; 
			while (rs.next()) {
				relvol = rs.getFloat("relvol");
			}
			rs.close();
			s.close();
			c.close();
			
			// If the stock hasn't been listed for 100 days, use 1/3 standard position size
			if (relvol <= 0) { 
				relvol = 3;
			}
			
			return relvol;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 1f;
		}
	}
	
	public static float getTradingAccountValue() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT (SELECT SUM(b.adjclose * t.shares) " +
					"FROM trades t " +
					"INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol) " +
					"WHERE t.type = 'long' AND status = 'open') " +
					"+ (SELECT SUM(b.adjclose * t.shares) " +
					"FROM trades t " +
					"INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol) " +
					"WHERE t.type = 'short' AND status = 'open') " +
					"+ (SELECT cash FROM tradingaccount) AS value";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			float value = 0f;
			while (rs.next()) {
				value = rs.getFloat("value");
			}
			rs.close();
			s.close();
			c.close();
			return value;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static void saveAccountHistoryValue(float accountValue) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO tradingaccounthistory(date, \"value\") VALUES (now(), ?)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, accountValue);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static float updateTradingAccountCash(float cash) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE tradingaccount SET cash = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, cash);
			ps.executeUpdate();
			ps.close();
			c.close();
			return cash;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static float getCurrentValueOfLongOpenTrades() {
		float value = 0f;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT SUM(b.adjclose * t.shares) AS value " +
					"FROM trades t " +
					"INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol) " +
					"WHERE t.type = 'long' AND status = 'open'";
			PreparedStatement ps = c.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getFloat("value");
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static float getCurrentValueOfShortOpenTrades() {
		float value = 0f;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT SUM(b.adjclose * t.shares) AS value " +
					"FROM trades t " +
					"INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol) " +
					"WHERE t.type = 'short' AND status = 'open'";
			PreparedStatement ps = c.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getFloat("value");
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static ArrayList<String> getTradingPositionSymbols() {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT DISTINCT symbol FROM trades WHERE status = 'open'";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				symbols.add(rs.getString("symbol"));
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<HashMap<String, Object>> getOpenPositions() {
		ArrayList<HashMap<String, Object>> openPositions = new ArrayList<HashMap<String, Object>>(); 
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT type, symbol, duration, shares, entryprice, commission, sell, sellop, sellvalue, stop, stopvalue FROM trades WHERE status = 'open'";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				HashMap<String, Object> openPosition = new HashMap<String, Object>();
				openPosition.put("type", rs.getString("type"));
				openPosition.put("symbol", rs.getString("symbol"));
				openPosition.put("duration", rs.getString("duration"));
				openPosition.put("shares", rs.getInt("shares"));
				openPosition.put("sell", rs.getString("sell"));
				openPosition.put("sellop", rs.getString("sellop"));
				openPosition.put("sellvalue", rs.getFloat("sellvalue"));
				openPosition.put("stop", rs.getString("stop"));
				openPosition.put("stopvalue", rs.getFloat("stopvalue"));
				openPosition.put("entryprice", rs.getFloat("entryprice"));
				openPosition.put("commission", rs.getFloat("commission"));
				openPositions.add(openPosition);
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return openPositions;
	}
	
	public static HashMap<String, Object> doICloseThisPosition(String symbol, String sellMetric, String sellop, String stopMetric, float stopValue) {
		HashMap<String, Object> answers = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT b.adjclose, t.entryprice, (b.adjclose - t.entryprice) / t.entryprice * 100 AS perchange, " +
						"m.value AS sellmetricvalue, t.sellvalue AS sellmetrictrigger, " +
						"CASE WHEN m.value " + sellop + " + t.sellvalue THEN true ELSE false END AS sellcriteriahit, " +
						"countbusinessdays(date(t.entry), b.date) AS length " +
						"FROM basicr b " +
						"INNER JOIN trades t " +
						"ON t.symbol = b.symbol AND t.status = 'open' " +
						"INNER JOIN metric_" + sellMetric + " m " +
						"ON m.symbol = b.symbol AND m.date = b.date " +
						"WHERE b.symbol = ? " +
						"AND b.date = date(now())";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			ResultSet rs = s.executeQuery();
			boolean sellAnswer = false;
			boolean stopAnswer = false;
			float perchange = 0f;
			int length = 0;
			float adjclose = 0f;
			while (rs.next()) {
				sellAnswer = rs.getBoolean("sellcriteriahit");
				perchange = rs.getFloat("perchange");
				length = rs.getInt("length");
				adjclose = rs.getFloat("adjclose");
			}
			
			if (stopMetric.equals("# Days")) {
				if (length >= stopValue) {
					stopAnswer = true;
				}
			}
			else if (stopMetric.equals("% Up")) {
				if (perchange >= stopValue) {
					stopAnswer = true;
				}
			}
			else if (stopMetric.equals("% Down")) {
				if (perchange <= -stopValue) {
					stopAnswer = true;
				}
			}
			
			if (length == 0) {
				sellAnswer = false;
				stopAnswer = false;
			}
			
			answers.put("sell", sellAnswer);
			answers.put("stop", stopAnswer);
			answers.put("adjclose", new Float(adjclose));
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return answers;
	}
	
	public static float getQuote(String symbol) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT adjclose FROM basicr WHERE symbol = ? AND date = now()";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			ResultSet rs = s.executeQuery();
			float adjclose = 0f;
			while (rs.next()) {
				adjclose = rs.getFloat("adjclose");
			}
			rs.close();
			s.close();
			c.close();
			return adjclose;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static void closePosition(String symbol, String exitReason, float exitPrice, float totalCommission, float netProfit, float grossProfit) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades " +
						"SET status = 'closed', exit = now(), exitprice = ?, exitreason = ?, commission = ?, netprofit = ?, grossprofit = ? " +
						"WHERE symbol = ? AND status = 'open'";
			PreparedStatement s = c.prepareStatement(q);
			s.setFloat(1, exitPrice);
			s.setString(2, exitReason);
			s.setFloat(3, totalCommission);
			s.setFloat(4, netProfit);
			s.setFloat(5, grossProfit);
			s.setString(6, symbol);
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int makeTrade(String type, String symbol, float entry, int numShares, float commission) {
		try {
			ParameterSingleton ps = ParameterSingleton.getInstance();
			String stop = ps.getStopMetric();
			Float stopValue = ps.getStopValue();
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO trades(status, entry, exit, \"type\", symbol, shares, entryprice, exitprice, exitreason, commission, netprofit, grossprofit, buy1, buy2, sell, sellop, sellvalue, stop, stopvalue) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			s.setString(1, "open");
			s.setTimestamp(2, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
			s.setDate(3, null);
			s.setString(4, type);
			s.setString(5, symbol);
			s.setInt(6, numShares);
			s.setFloat(7, entry);
			s.setNull(8, java.sql.Types.FLOAT);
			s.setString(9, null);
			s.setFloat(10, commission);
			s.setNull(11, java.sql.Types.FLOAT);
			s.setNull(12, java.sql.Types.FLOAT);
			s.setString(13, ps.getxAxisMetric());
			s.setString(14, ps.getyAxisMetric());
			s.setString(15, ps.getSellMetric());
			s.setString(16, ps.getSellOperator());
			s.setFloat(17, ps.getSellValue());
			if (stop != null && !stop.equals(""))
				s.setString(18, stop);
			else
				s.setString(18, null);
			if (stopValue != null)
				s.setFloat(19, stopValue);
			else
				s.setNull(19, java.sql.Types.FLOAT);
			
			s.executeUpdate();
			ResultSet rs = s.getGeneratedKeys();
			int id = 0;
			while (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			s.close();
			c.close();
			return id;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}