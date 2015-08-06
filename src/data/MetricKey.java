package data;

import constants.Constants.BAR_SIZE;

/**
 * Not a Primary Key for the Metrics table.  Just a unique name, symbol, duration combo
 * 
 */
public class MetricKey {

	public String name;
	public String symbol;
	public BAR_SIZE duration;
	
	public MetricKey(String name, String symbol, BAR_SIZE duration) {
		super();
		this.name = name;
		this.symbol = symbol;
		this.duration = duration;
	}

	@Override
	public boolean equals(Object o) {
		if (o.equals(null)) {
			return false;
		}
		if (o instanceof MetricKey) {
			MetricKey m = (MetricKey)o;
			if (this.name.equals(m.name) && this.symbol.equals(m.symbol) && this.duration == m.duration) {
				return true;
			}
		}
		return false;
	}
}