package gui.threads;

import java.util.ArrayList;

public class MUTCoordinator {

	private static MUTCoordinator mut = null;
	private ArrayList<Cell> cells = null;
	
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