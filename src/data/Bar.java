package data;

import java.util.Calendar;

import constants.Constants;
import constants.Constants.BAR_SIZE;

public class Bar {

	public String symbol;
	public float open;
	public float close;
	public float high;
	public float low;
	public float vwap;
	public float volume;
	public int numTrades;
	public float change;
	public float gap;
	public Calendar periodStart;
	public Calendar periodEnd;
	public Constants.BAR_SIZE duration;
	public boolean partial;

	public Bar(String symbol, float open, float close, float high, float low,
			float vwap, float volume, int numTrades, float change, float gap,
			Calendar periodStart, Calendar periodEnd, BAR_SIZE duration,
			boolean partial) {
		super();
		this.symbol = symbol;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.vwap = vwap;
		this.volume = volume;
		this.numTrades = numTrades;
		this.change = change;
		this.gap = gap;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.duration = duration;
		this.partial = partial;
	}

	@Override
	public String toString() {
		String s = "*** BAR *** \n";
		s += "Symbol: " + symbol + "\n";
		s += "Open: " + open + "\n";
		s += "Close: " + close + "\n";
		s += "High: " + high + "\n";
		s += "Low: " + low + "\n";
		s += "VWAP: " + vwap + "\n";
		s += "Volume: " + volume + "\n";
		s += "#Trades: " + numTrades + "\n";
		s += "Change: " + change + "\n";
		s += "Gap: " + gap + "\n";
		s += "Start: " + periodStart.getTime().toString() + "\n";
		s += "End: " + periodEnd.getTime().toString() + "\n";
		s += "Duration: " + duration.toString() + "\n";
		s += "Partial: " + partial;
		return s;
	}
	
}