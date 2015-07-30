package metrics;

import java.util.Calendar;

import constants.Constants.BAR_SIZE;

public class Metric {
	
	// Essentials
	private String name;
	private String symbol;
	private Calendar start;
	private Calendar end;
	private String duration;
	private Float value;
	
	// Auxiliary
	private double volume = 0;
	private float adjOpen = 0f;
	private float adjClose = 0f;
	private float adjHigh = 0f;
	private float adjLow = 0f;
	private float gap = 0f;
	private float change = 0f;
	private float spyAdjClose = 0f;
	private float spyChange = 0f;

	public Metric(String symbol, Calendar start, Calendar end, String duration, double volume, float adjOpen, float adjClose,
			float adjHigh, float adjLow, float gap, float change, float spyAdjClose, float spyChange) {
		super();
		this.symbol = symbol;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.volume = volume;
		this.adjOpen = adjOpen;
		this.adjClose = adjClose;
		this.adjHigh = adjHigh;
		this.adjLow = adjLow;
		this.gap = gap;
		this.change = change;
		this.spyAdjClose = spyAdjClose;
		this.spyChange = spyChange;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Calendar getStart() {
		return start;
	}

	public void setStart(Calendar start) {
		this.start = start;
	}
	
	public Calendar getEnd() {
		return end;
	}

	public void setEnd(Calendar end) {
		this.end = end;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public float getAdjOpen() {
		return adjOpen;
	}

	public void setAdjOpen(float adjOpen) {
		this.adjOpen = adjOpen;
	}

	public float getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(float adjClose) {
		this.adjClose = adjClose;
	}

	public float getAdjHigh() {
		return adjHigh;
	}

	public void setAdjHigh(float adjHigh) {
		this.adjHigh = adjHigh;
	}

	public float getAdjLow() {
		return adjLow;
	}

	public void setAdjLow(float adjLow) {
		this.adjLow = adjLow;
	}

	public float getSpyAdjClose() {
		return spyAdjClose;
	}

	public void setSpyAdjClose(float spyAdjClose) {
		this.spyAdjClose = spyAdjClose;
	}

	public float getGap() {
		return gap;
	}

	public void setGap(float gap) {
		this.gap = gap;
	}

	public float getChange() {
		return change;
	}

	public void setChange(float change) {
		this.change = change;
	}

	public float getSpyChange() {
		return spyChange;
	}

	public void setSpyChange(float spyChange) {
		this.spyChange = spyChange;
	}
}