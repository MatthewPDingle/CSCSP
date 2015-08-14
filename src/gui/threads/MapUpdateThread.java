package gui.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import constants.Constants;
import gui.MapCell;
import gui.singletons.ParameterSingleton;
import gui.threads.MUTCoordinator.Cell;
import utils.CalcUtils;

public class MapUpdateThread extends Thread {

	private boolean running = false;
	private MapUpdateThreadResultSetter setter = null;
	private HashMap<String, MapCell> result = new HashMap<String, MapCell>();
	private ParameterSingleton ps = ParameterSingleton.getInstance();

	public MapUpdateThread() {
		this.running = true;
		this.setPriority(Thread.MIN_PRIORITY);
	}

	@Override
	public void run() {
		try {
			float xMetricMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getxAxisMetric());
			float xMetricMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getxAxisMetric());
			float xCellSize = (xMetricMax - xMetricMin) / (float)ps.getxRes();
			
			float yMetricMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getyAxisMetric());
			float yMetricMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getyAxisMetric());
			float yCellSize = (yMetricMax - yMetricMin) / (float)ps.getyRes();
					
			MUTCoordinator mutCoordinator = MUTCoordinator.getInstance();	
			HashMap<String, ArrayList<HashMap<String, Object>>> mapData = mutCoordinator.getCopyOfMapData();
			
			Cell cell;
			while ((cell = mutCoordinator.getNextAvailableCell()) != null && running) {
				ArrayList<HashMap<String, Object>> trades = new ArrayList<HashMap<String, Object>>();
			
				Set<String> symbolKeys = mapData.keySet(); // Get a list of symbols in the mapData
				for (String symbolKey : symbolKeys) {
					// Pull out the list of bars for this symbol
					ArrayList<HashMap<String, Object>> symbolMapData = mapData.get(symbolKey);
					for (int bi = 0; bi < symbolMapData.size(); bi++) { // Bar Index
						HashMap<String, Object> bar = symbolMapData.get(bi);
						
						HashMap<String, Object> tradeData = new HashMap<String, Object>();
						
						float m1v = (float)bar.get("m1v"); // Metric 1 (X) Value
						float m2v = (float)bar.get("m2v"); // Metric 2 (Y) Value
						
						// If this bar is in the cell being processed, open a trade
						if (m1v >= cell.xMetricPosition && m1v <= (cell.xMetricPosition + xCellSize) && m2v >= cell.yMetricPosition && m2v <= (cell.yMetricPosition + yCellSize)) {
							float tradeOpen = (float)bar.get("close");
							float alphaOpen = (float)bar.get("alphaclose");
							tradeData.put("open", tradeOpen);
							tradeData.put("alphaopen", alphaOpen);
							tradeData.put("barindex", bi);
							
							// Find the end of this trade
							// Via the regular sell metric
							if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_NUM_BARS_LATER)) {
								if (symbolMapData.size() > (bi + (int)ps.getSellValue())) {
									HashMap<String, Object> closeBar = symbolMapData.get(bi + (int)ps.getSellValue());
									float tradeClose = (float)closeBar.get("close");
									float alphaClose = (float)closeBar.get("alphaclose");
									tradeData.put("close", tradeClose);
									tradeData.put("alphaclose", alphaClose);
									tradeData.put("duration", ps.getSellValue());
									tradeData.put("exitreason", "metric");
								}
							}
							else if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_PERCENT_UP)) {
								for (int rbi = bi + 1; rbi < symbolMapData.size(); rbi++) { // Remaining Bar Index
									HashMap<String, Object> laterBar = symbolMapData.get(rbi);
									float tradeClose = (float)laterBar.get("close");
									float alphaClose = (float)laterBar.get("alphaclose");
									float perchange = (tradeClose - tradeOpen) / tradeOpen;
									if (perchange >= ps.getSellValue()) {
										tradeData.put("close", tradeClose);
										tradeData.put("alphaclose", alphaClose);
										tradeData.put("duration", (rbi - bi));
										tradeData.put("exitreason", "metric");
									}
								}
							}
							else if (ps.getSellMetric().equals(Constants.OTHER_SELL_METRIC_PERCENT_DOWN)) {
								for (int rbi = bi + 1; rbi < symbolMapData.size(); rbi++) { // Remaining Bar Index
									HashMap<String, Object> laterBar = symbolMapData.get(rbi);
									float tradeClose = (float)laterBar.get("close");
									float alphaClose = (float)laterBar.get("alphaclose");
									float perchange = (tradeClose - tradeOpen) / tradeOpen;
									if (perchange <= ps.getSellValue()) {
										tradeData.put("close", tradeClose);
										tradeData.put("alphaclose", alphaClose);
										tradeData.put("duration", (rbi - bi));
										tradeData.put("exitreason", "metric");
									}
								}
							}
							else {
								for (int rbi = bi + 1; rbi < symbolMapData.size(); rbi++) { // Remaining Bar Index
									HashMap<String, Object> laterBar = symbolMapData.get(rbi);
									float sellMetric = (float)laterBar.get("m4v");
									if (ps.getSellOperator().equals(">=")) {
										if (sellMetric >= ps.getSellValue()) {
											float tradeClose = (float)laterBar.get("close");
											float alphaClose = (float)laterBar.get("alphaclose");
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", (rbi - bi));
											tradeData.put("exitreason", "metric");
											break;
										}
									}
									else if (ps.getSellOperator().equals(">")) {
										if (sellMetric > ps.getSellValue()) {
											float tradeClose = (float)laterBar.get("close");
											float alphaClose = (float)laterBar.get("alphaclose");
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", (rbi - bi));
											tradeData.put("exitreason", "metric");
											break;
										}
									}
									else if (ps.getSellOperator().equals("=") || ps.getSellOperator().equals("==")) {
										if (sellMetric == ps.getSellValue()) {
											float tradeClose = (float)laterBar.get("close");
											float alphaClose = (float)laterBar.get("alphaclose");
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", (rbi - bi));
											tradeData.put("exitreason", "metric");
											break;
										}
									}
									else if (ps.getSellOperator().equals("<=")) {
										if (sellMetric <= ps.getSellValue()) {
											float tradeClose = (float)laterBar.get("close");
											float alphaClose = (float)laterBar.get("alphaclose");
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", (rbi - bi));
											tradeData.put("exitreason", "metric");
											break;
										}
									}
									else if (ps.getSellOperator().equals("<")) {
										if (sellMetric < ps.getSellValue()) {
											float tradeClose = (float)laterBar.get("close");
											float alphaClose = (float)laterBar.get("alphaclose");
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", (rbi - bi));
											tradeData.put("exitreason", "metric");
											break;
										}
									}
								}
							}
							// Via the stop metric
							if (ps.getStopMetric().equals(Constants.STOP_METRIC_NUM_BARS)) {
								if (symbolMapData.size() > (bi + ps.getStopValue().intValue())) {
									HashMap<String, Object> closeBar = symbolMapData.get(bi + ps.getStopValue().intValue());
									float tradeClose = (float)closeBar.get("close");
									float alphaClose = (float)closeBar.get("alphaclose");
									Integer regularDuration = null;
									if (tradeData.get("duration") != null) {
										regularDuration = (Integer)tradeData.get("duration");
									}
									int stopDuration = ps.getStopValue().intValue();
									if (regularDuration == null || stopDuration < regularDuration) {
										tradeData.put("close", tradeClose);
										tradeData.put("alphaclose", alphaClose);
										tradeData.put("duration", stopDuration);
										tradeData.put("exitreason", "stop");
									}
								}
							}
							else if (ps.getStopMetric().equals(Constants.STOP_METRIC_PERCENT_DOWN)) {
								for (int rbi = bi + 1; rbi < symbolMapData.size(); rbi++) { // Remaining Bar Index
									HashMap<String, Object> laterBar = symbolMapData.get(rbi);
									float tradeClose = (float)laterBar.get("close");
									float alphaClose = (float)laterBar.get("alphaclose");
									float perchange = (tradeClose - tradeOpen) / tradeOpen;
									Integer regularDuration = null;
									if (tradeData.get("duration") != null) {
										regularDuration = (Integer)tradeData.get("duration");
									}
									int stopDuration = rbi - bi;
									if (regularDuration == null || stopDuration < regularDuration) {
										if (perchange <= ps.getSellValue()) {
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", stopDuration);
											tradeData.put("exitreason", "stop");
										}
									}
								}
							}
							else if (ps.getStopMetric().equals(Constants.STOP_METRIC_PERCENT_UP)) {
								for (int rbi = bi + 1; rbi < symbolMapData.size(); rbi++) { // Remaining Bar Index
									HashMap<String, Object> laterBar = symbolMapData.get(rbi);
									float tradeClose = (float)laterBar.get("close");
									float alphaClose = (float)laterBar.get("alphaclose");
									float perchange = (tradeClose - tradeOpen) / tradeOpen;
									Integer regularDuration = null;
									if (tradeData.get("duration") != null) {
										regularDuration = (Integer)tradeData.get("duration");
									}
									int stopDuration = rbi - bi;
									if (regularDuration == null || stopDuration < regularDuration) {
										if (perchange >= ps.getSellValue()) {
											tradeData.put("close", tradeClose);
											tradeData.put("alphaclose", alphaClose);
											tradeData.put("duration", stopDuration);
											tradeData.put("exitreason", "stop");
										}
									}
								}
							}
							// Via the end of data
							// tradeData should have "open", "close", and "duration".  If there's no close or duration, it went to the end
							if (tradeData.get("close") == null) {
								HashMap<String, Object> lastBar = symbolMapData.get(symbolMapData.size() - 1);
								float lastBarClose = (float)lastBar.get("close");
								float alphaClose = (float)lastBar.get("alphaclose");
								int startBarIndex = (int)tradeData.get("barindex");
								int lastBarIndex = symbolMapData.size() - 1;
								tradeData.put("duration", lastBarIndex - startBarIndex);
								tradeData.put("close", lastBarClose);
								tradeData.put("alphaclose", alphaClose);
								tradeData.put("exitreason", "end");
							}
							
							trades.add(tradeData);
							
						} // End if
					} // End for bar index loop
				} // End for key loop

				// End processing on the list of trade data
				int numResults = trades.size();
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
				
				for (HashMap<String, Object> tradeData : trades) {
					float open = (float)tradeData.get("open");
					float close = (float)tradeData.get("close");
					float alphaopen = (float)tradeData.get("alphaopen");
					float alphaclose = (float)tradeData.get("alphaclose");
					int duration = (int)tradeData.get("duration");
					String exitReason = tradeData.get("exitreason").toString();
					float perchange = (close - open) / open * 100f;
					float alphaSymbolPerchange = (alphaclose - alphaopen) / alphaopen * 100f;
					float alphaPerchange = perchange - alphaSymbolPerchange;
					
					allPerchangesList.add(perchange);
					allAlphaPerchangesList.add(alphaPerchange);
					allTradeDurations.add(duration);
					if (exitReason.equals("metric")) {
						metricPerchangesList.add(perchange);
						metricTradeDurations.add(duration);
						numMetricExits++;
					}
					else if (exitReason.equals("end")) {
						latestPerchangesList.add(perchange);
						latestTradeDurations.add(duration);
						numLatestExits++;
					}
					else if (exitReason.equals("stop")) {
						stopPerchangesList.add(perchange);
						stopTradeDurations.add(duration);
						numStopExits++;
					}
				} // End while
				
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
				
				
				// Normalize the values of the x and y metrics to 0 to 100
				float xMetricRange = xMetricMax - xMetricMin;
				float xMetricScale = 100f / xMetricRange;
				float x100Scale = cell.xMetricPosition * xMetricScale;
				float xCellSize100Scale = 100f / (float)ps.getxRes();
				float yMetricRange = yMetricMax - yMetricMin;
				float yMetricScale = 100f / yMetricRange;
				float y100Scale = cell.yMetricPosition * yMetricScale;
				float yCellSize100Scale = 100f / (float)ps.getyRes();
				
				// Add a couple more entries so the map knows where to draw the cell and how big it is.
				mc.addToMetricValueHash("Map X Metric Scale", xMetricScale);
				mc.addToMetricValueHash("Map Y Metric Scale", yMetricScale);
				mc.addToMetricValueHash("Map X 100 Scale", x100Scale);
				mc.addToMetricValueHash("Map X Cell Size 100 Scale", xCellSize100Scale);
				mc.addToMetricValueHash("Map Y 100 Scale", y100Scale);
				mc.addToMetricValueHash("Map Y Cell Size 100 Scale", yCellSize100Scale);
				mc.addToMetricValueHash("Map X Metric Min", cell.xMetricPosition);
				mc.addToMetricValueHash("Map X Metric Max", cell.xMetricPosition + xCellSize);
				mc.addToMetricValueHash("Map Y Metric Min", cell.yMetricPosition);
				mc.addToMetricValueHash("Map Y Metric Max", cell.yMetricPosition + yCellSize);
				mc.addToMetricValueHash("X Array Pos", (float)cell.xArrayPos);
				mc.addToMetricValueHash("Y Array Pos", (float)cell.yArrayPos);

				// Send cell back to the GUI
				result.put("cell", mc);
				setter.setResult(result);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setMapUpdateThreadResultSetter (MapUpdateThreadResultSetter setter) {
		this.setter = setter;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
}