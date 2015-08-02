package gui.threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import dbio.QueryManager;

public class MUTCoordinator {

	private static MUTCoordinator mut = null;
	private ArrayList<Cell> cells = null;
	private HashMap<String, ArrayList<HashMap<String, Object>>> mapData = null; // Symbol, List of bars containing data
	
	protected MUTCoordinator() {
		cells = new ArrayList<Cell>();
	}
	
	public static MUTCoordinator getInstance() {
		if (mut == null) {
			mut = new MUTCoordinator();
		}
		return mut;
	}
	
	public void addCell(float xMetricPosition, float yMetricPosition, int xArrayPos, int yArrayPos) {
		cells.add(new Cell(xMetricPosition, yMetricPosition, xArrayPos, yArrayPos));
	}
	
	public void loadRawDataIntoMemory() {
		Calendar latestDateInBar = QueryManager.getMaxDateFromBar();
		mapData = QueryManager.getMapDataForCells(latestDateInBar);
	}

	public HashMap<String, ArrayList<HashMap<String, Object>>> getCopyOfMapData() {
//		ArrayList<HashMap<String, Object>> copy = new ArrayList<HashMap<String, Object>>();
//		copy.addAll(mapData);
		return mapData;
	}
	
	public synchronized Cell getNextAvailableCell() {
		if (cells.size() > 0)
			return (Cell)cells.remove(0);
		else 
			return null;
	}
	
	public void clearCells() {
		cells = new ArrayList<Cell>();
	}
	
	// Inner class
	public class Cell {
		public float xMetricPosition;
		public float yMetricPosition;
		public int xArrayPos;
		public int yArrayPos;
		
		public Cell(float xMetricPosition, float yMetricPosition, int xArrayPos, int yArrayPos) {
			this.xMetricPosition = xMetricPosition;
			this.yMetricPosition = yMetricPosition;
			this.xArrayPos = xArrayPos;
			this.yArrayPos = yArrayPos;
		}
	}
}