package gui;

import gui.singletons.MapCellSingleton;

import java.util.Calendar;

public class MapSymbol implements Comparable {

	private String symbol = "";
	private String duration = "";
	private float price;
	private float xMetricValue;
	private float yMetricValue;
	private float cellBullScore;
	private float cellBearScore;
	private float groupBullishScore;
	private float groupBearishScore;
	private boolean partOfBestBullGroup = false;
	private boolean partOfBestBearGroup = false;
	private Calendar lastUpdated = Calendar.getInstance();
	
	public MapSymbol(String symbol, String duration, float price, float xMetricValue, float yMetricValue) {
		super();
		this.symbol = symbol;
		this.duration = duration;
		this.price = price;
		this.xMetricValue = xMetricValue;
		this.yMetricValue = yMetricValue;
		lastUpdated = Calendar.getInstance();
	}

	@Override
	public int compareTo(Object anotherMapSymbol) {
		MapSymbol ms = (MapSymbol)anotherMapSymbol;

		if (ms.getCellBullScore() == this.getCellBullScore())
			return 0;
		else if (ms.getCellBullScore() > this.getCellBullScore())
			return -1;
		else
			return 1;
	}
	
	public MapCell getParentMapCell() {
		return MapCellSingleton.getInstance().getSmoothedMapCellAt(xMetricValue, yMetricValue);
	}

	public String getSymbol() {
		return symbol;
	}

	public float getXMetricValue() {
		return xMetricValue;
	}

	public float getYMetricValue() {
		return yMetricValue;
	}

	public float getPrice() {
		return price;
	}

	public float getCellBullScore() {
		return cellBullScore;
	}

	public void setCellBullScore(float cellBullScore) {
		this.cellBullScore = cellBullScore;
	}

	public float getCellBearScore() {
		return cellBearScore;
	}

	public void setCellBearScore(float cellBearScore) {
		this.cellBearScore = cellBearScore;
	}

	public float getGroupBullishScore() {
		return groupBullishScore;
	}

	public void setGroupBullishScore(float groupScore) {
		this.groupBullishScore = groupScore;
	}

	public boolean isPartOfBestBullGroup() {
		return partOfBestBullGroup;
	}

	public void setPartOfBestBullGroup(boolean partOfBestBullGroup) {
		this.partOfBestBullGroup = partOfBestBullGroup;
	}

	public boolean isPartOfBestBearGroup() {
		return partOfBestBearGroup;
	}

	public void setPartOfBestBearGroup(boolean partOfBestBearGroup) {
		this.partOfBestBearGroup = partOfBestBearGroup;
	}

	public float getGroupBearishScore() {
		return groupBearishScore;
	}

	public void setGroupBearishScore(float groupBearishScore) {
		this.groupBearishScore = groupBearishScore;
	}

	public Calendar getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Calendar lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
}