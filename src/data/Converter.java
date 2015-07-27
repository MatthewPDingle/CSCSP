package data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;

import utils.CalendarUtils;
import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.downloaders.bitcoincharts.BitcoinChartsConstants;
import dbio.QueryManager;

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
	
	/**
	 * Queries data from bitcointick, packages the tick data into bars, and inserts it into bar
	 * 
	 * @param bitcoinSymbol
	 * @param barSize
	 */
	public static void processTickDataIntoBars(String bitcoinSymbol, BAR_SIZE barSize) {
		int numBarsProcessed = 0;
		try {
			// First figure out where to start by finding the earlist tick and then rounding back to the beginning of what that bar period would be
			Calendar earlistTick = QueryManager.getBitcoinTickEarliestTick(bitcoinSymbol);
			Calendar latestTick = QueryManager.getBitcoinTickLatestTick(bitcoinSymbol);
			
			Calendar periodStart = earlistTick;
			periodStart.set(Calendar.SECOND, 0);
			periodStart.set(Calendar.MILLISECOND, 0);
			Calendar periodEnd = Calendar.getInstance();
			periodEnd.setTime(periodStart.getTime());
			int unroundedMinute = 0;
			int unroundedHour = 0;
			int remainder = 0;
			switch (barSize) {
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_2M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 2;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 2);
					break;
				case BAR_5M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 5;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 5);
					break;
				case BAR_10M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 10;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 10);
					break;
				case BAR_15M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 15;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 15);
					break;
				case BAR_30M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 30;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 30);
					break;
				case BAR_1H:
					periodStart.set(Calendar.MINUTE, 0);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case BAR_2H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 2;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 2);
					break;
				case BAR_4H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 4;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 4);
					break;
				case BAR_6H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 6;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 6);
					break;
				case BAR_8H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 8;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 8);
					break;
				case BAR_12H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 12;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 12);
					break;
				case BAR_1D:
					periodStart.set(Calendar.MINUTE, 0);
					periodStart.set(Calendar.HOUR_OF_DAY, 0);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 24);
					break;
			}
			
			float previousclose = 0;
			while (periodStart.getTimeInMillis() < latestTick.getTimeInMillis()) {
				System.out.println("Processing bar between " + periodStart.getTime().toString() + " and " + periodEnd.getTime().toString());
				
				// Get the tick data from the database
				ArrayList<HashMap<String, Object>> recordsInBar = QueryManager.getBitcoinTickData(bitcoinSymbol, periodStart, barSize);
				
				int numTrades = recordsInBar.size();
				
				float volumeSum = 0;
				float priceVolumeSum = 0;
				int tradeNumber = 1;
				float open = 0;
				float close = 0;
				float high = 0;
				float low = 10000000;
				
				// Turn the tick data into a bar
				for (HashMap<String, Object> record : recordsInBar) {
					float price = (float)record.get("price");
					float volume = (float)record.get("volume");

					if (tradeNumber == 1) {
						open = price;
					}
					if (tradeNumber == numTrades) {
						close = price;
					}
					if (price > high) {
						high = price;
					}
					if (price < low) {
						low = price;
					}
					
					float priceVolume = price * volume;
					volumeSum += volume;
					priceVolumeSum += priceVolume;
					tradeNumber++;
				}
				
				if (previousclose == 0) {
					previousclose = open;
				}
				
				float vwap = priceVolumeSum / volumeSum; // Volume Weighted Average Price
				float change = close - previousclose;
				float gap = open - previousclose;

				if (numTrades == 0) {
					open = previousclose;
					close = previousclose;
					high = previousclose;
					low = previousclose;
					vwap = previousclose;
					change = 0;
					gap = 0;
				}
				
				System.out.println("Inserting a bar with " + numTrades + " trades at a vwap of " + vwap);
				
				previousclose = close;
				
				// Insert the bar data into the bitcoinbar table
				Bar bar = new Bar(bitcoinSymbol, open, close, high, low, new Float(vwap), volumeSum, new Integer(numTrades), new Float(change), new Float(gap), periodStart, periodEnd, barSize, false);
				QueryManager.insertOrUpdateIntoBar(bar);
				numBarsProcessed++;
				
				// Move forward the bar window by the bar size and repeat
				periodStart.setTime(periodEnd.getTime());
				switch (barSize) {
					case BAR_1M:
						periodEnd.add(Calendar.MINUTE, 1);
						break;
					case BAR_2M:
						periodEnd.add(Calendar.MINUTE, 2);
						break;
					case BAR_5M:
						periodEnd.add(Calendar.MINUTE, 5);
						break;
					case BAR_10M:
						periodEnd.add(Calendar.MINUTE, 10);
						break;
					case BAR_15M:
						periodEnd.add(Calendar.MINUTE, 15);
						break;
					case BAR_30M:
						periodEnd.add(Calendar.MINUTE, 30);
						break;
					case BAR_1H:
						periodEnd.add(Calendar.HOUR_OF_DAY, 1);
						break;
					case BAR_2H:
						periodEnd.add(Calendar.HOUR_OF_DAY, 2);
						break;
					case BAR_4H:
						periodEnd.add(Calendar.HOUR_OF_DAY, 4);
						break;
					case BAR_6H:
						periodEnd.add(Calendar.HOUR_OF_DAY, 6);
						break;
					case BAR_8H:
						periodEnd.add(Calendar.HOUR_OF_DAY, 8);
						break;
					case BAR_12H:
						periodEnd.add(Calendar.HOUR_OF_DAY, 12);
						break;
					case BAR_1D:
						periodEnd.add(Calendar.HOUR_OF_DAY, 24);
						break;
				}
			}
			LogManager.getLogger("data").info("Converter.processTickDataIntoBars processed " + numBarsProcessed + " " + barSize + " bars into the bar table.  Period spanned from " + periodStart.getTime().toString() + " to " + periodEnd.getTime().toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			LogManager.getLogger("data").error("Converter.processTickDataIntoBars failed processing bars into the bar table.  " + numBarsProcessed + " " + barSize + " bars were imported before failure.");
			LogManager.getLogger("data").error(e.getStackTrace().toString());
		}
	}
	
	/**
	 * Reads the file archive and inserts the data into the bitcointick table.
	 * It first checks the bitcointick table for previous data and gets the time of the latest tick data for the symbol.
	 * Only data in the file after that time gets imported into bitcointick.  T
	 * 
	 * @param filename
	 */
	public static void processArchiveFileIntoTicks(String filename) {
		int numTicks = 0;
		int numOldIgnoredTicks = 0;
		try {
			String tickSymbol = "unknownBTC";
			if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_BITFINEX_BTC_USD)) {
				tickSymbol = "bitfinexBTCUSD";
			}
			else if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_BITSTAMP_BTC_USD)) {
				tickSymbol = "bitstampBTCUSD";
			}
			else if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_BTCE_BTC_USD)) {
				tickSymbol = "btceBTCUSD";
			}
			else if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_BTCN_BTC_CNY)) {
				tickSymbol = "btcnBTCCNY";
			}
			else if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_KRAKEN_BTC_EUR)) {
				tickSymbol = "krakenBTCEUR";
			}
			else if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_KRAKEN_BTC_USD)) {
				tickSymbol = "krakenBTCUSD";
			}
			else if (filename.equals(BitcoinChartsConstants.FILE_TICK_HISTORY_OKCOIN_BTC_CNY)) {
				tickSymbol = "okcoinBTCCNY";
			}
			
			Calendar latestTick = QueryManager.getBitcoinTickLatestTick(tickSymbol);
			
			InputStream fileStream = new FileInputStream("data/" + filename);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream);
			BufferedReader br = new BufferedReader(decoder);
			
			ArrayList<String> recordBuffer = new ArrayList<String>();
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] pieces = line.split(",");
				long msTime = Long.parseLong(pieces[0]) * 1000; // Dumps give time in seconds, have to convert to ms.
				float price = Float.parseFloat(pieces[1]);
				float volume = Float.parseFloat(pieces[2]);
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(msTime);
				
				// Only insert ticks newer than what we already have data for in the database
				if (c.after(latestTick)) {
					numTicks++;
					String record = "'" + tickSymbol + "', " + price + ", " + volume + ", '" + CalendarUtils.getSqlDateTimeString(c) + "'";
					recordBuffer.add(record);
					if (recordBuffer.size() >= 100) {
						QueryManager.insertIntoBitcoinTick(recordBuffer);
						recordBuffer.clear();
					}
				}
				else {
					numOldIgnoredTicks++;
				}
			}
			
			// Insert remaining buffered ticks
			QueryManager.insertIntoBitcoinTick(recordBuffer);
			recordBuffer.clear();
			
			LogManager.getLogger("data").info("Converter.processArchiveFileIntoTicks processed " + numTicks + " ticks into the tick table.  " + numOldIgnoredTicks + " ticks were ignored because data was already in the DB.  Data from " + filename);
		}
		catch (Exception e) {
			e.printStackTrace();
			LogManager.getLogger("data").error("Converter.processArchiveFileIntoTicks failed processing ticks.  " + numTicks + " were imported before failure.  " + numOldIgnoredTicks + " were ignored because data was already in the DB.  Data from " + filename);
			LogManager.getLogger("data").error(e.getStackTrace().toString());
		}
	}
}