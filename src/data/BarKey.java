package data;

import constants.Constants.BAR_SIZE;

/**
 * Not a Primary Key for the Bar table.  Just a unique symbol, duration combo
 * 
 */
public class BarKey {

	public String symbol;
	public BAR_SIZE duration;

	public BarKey(String symbol, BAR_SIZE duration) {
		super();
		this.symbol = symbol;
		this.duration = duration;
	}
	
	public BarKey(String symbol, String duration) {
		super();
		this.symbol = symbol;
		this.duration = BAR_SIZE.valueOf(duration);
	}

	@Override
	public boolean equals(Object o) {
		if (o.equals(null)) {
			return false;
		}
		if (o instanceof BarKey) {
			BarKey b = (BarKey)o;
			if (this.symbol.equals(b.symbol) && this.duration == b.duration) {
				return true;
			}
		}
		return false;
	}
}