package data;

import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants;

public class Converter {

	/**
	 * Converts a list of ticks (oldest to newest) to a bar.
	 * 
	 * @param ticks
	 * @param barStart
	 * @param barEnd
	 * @param barSize
	 * @param previousClose
	 * @return
	 */
	public static Bar ticksToBar(ArrayList<Tick> ticks, Calendar barStart, Calendar barEnd, Constants.BAR_SIZE barSize, float previousClose, boolean partial) {
		Bar bar = null;
		String symbol = "";
		try {
			int numTrades = ticks.size();
			float volumeSum = 0;
			float priceVolumeSum = 0;
			int tradeNumber = 1;
			float open = 0;
			float close = 0;
			float high = 0;
			float low = 10000000;
			
			for (Tick tick : ticks) {
				symbol = tick.symbol;
				if (tradeNumber == 1) {
					open = tick.price;
				}
				if (tradeNumber == numTrades) {
					close = tick.price;
				}
				if (tick.price > high) {
					high = tick.price;
				}
				if (tick.price < low) {
					low = tick.price;
				}
				
				float priceVolume = tick.price * tick.volume;
				volumeSum += tick.volume;
				priceVolumeSum += priceVolume;
				tradeNumber++;
			}
			
			if (previousClose == 0) {
				previousClose = open;
			}
			
			float vwap = priceVolumeSum / volumeSum; // Volume Weighted Average Price
			float change = close - previousClose;
			float gap = open - previousClose;
			
			if (numTrades == 0) {
				open = previousClose;
				close = previousClose;
				high = previousClose;
				low = previousClose;
				vwap = previousClose;
				change = 0;
				gap = 0;
			}
			
			bar = new Bar(symbol, open, close, high, low, vwap, volumeSum, numTrades, change, gap, barStart, barEnd, barSize, partial);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bar;
	}
}