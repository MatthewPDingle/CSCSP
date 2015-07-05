package gui.threads;

import gui.MapCell;
import gui.singletons.ParameterSingleton;
import gui.threads.MUTCoordinator.Cell;

import java.util.Calendar;
import java.util.HashMap;

import constants.Constants;
import dbio.QueryManager;

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
			
			Calendar latestDateInBasicr = QueryManager.getMaxDateFromBasicr();
			
			MUTCoordinator mutCoordinator = MUTCoordinator.getInstance();
			Cell cell;
			while ((cell = mutCoordinator.getNextAvailableCell()) != null && running) {
				// Run main MapCell query
				MapCell mc = QueryManager.cellQuery(cell.xMetricPosition, xCellSize, cell.yMetricPosition, yCellSize, latestDateInBasicr);
					
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