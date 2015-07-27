package bitcoin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import utils.CalendarUtils;
import constants.Constants.BAR_SIZE;
import dbio.QueryManager;

public class BitcoinHistoricalPriceProcessor {

	public static void main(String[] args) {
		
//		String filename = "data/bitfinexUSD.csv.gz";
		String filename = "data/okcoinCNY.csv.gz";
		
		processArchiveFile(filename);
		
//		for (BAR_SIZE barSize : BAR_SIZE.values()) {
//			createBars("bitfinexUSD", barSize);
//		}
		
//		createBars("bitfinexUSD", BAR_SIZE.BAR_1M);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_2M);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_5M);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_10M);
//		createBars("okcoinCNY", BAR_SIZE.BAR_15M);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_30M);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_1H);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_2H);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_4H);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_6H);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_8H);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_12H);
//		createBars("bitfinexUSD", BAR_SIZE.BAR_1D);
	}

	/**
	 * Reads the file archive and inserts the data into the bitcointick table
	 * 
	 * @param filename
	 */
	private static void processArchiveFile(String filename) {
		try {
			String symbol = filename.replaceAll("data/", "").replaceAll(".csv.gz", "");
			InputStream fileStream = new FileInputStream(filename);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream);
			BufferedReader br = new BufferedReader(decoder);
			
			ArrayList<String> recordBuffer = new ArrayList<String>();
			
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println("---");
				String[] pieces = line.split(",");
				long msTime = Long.parseLong(pieces[0]) * 1000; // Dumps give time in seconds, have to convert to ms.
				float price = Float.parseFloat(pieces[1]);
				float volume = Float.parseFloat(pieces[2]);
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(msTime);
				System.out.println(c.getTime().toString());
				System.out.println(price);
				System.out.println(volume);
				
				String record = "'" + symbol + "', " + price + ", " + volume + ", '" + CalendarUtils.getSqlDateTimeString(c) + "'";
				recordBuffer.add(record);
				
				if (recordBuffer.size() >= 100) {
					QueryManager.insertIntoBitcoinTick(recordBuffer);
					recordBuffer.clear();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Queries data from bitcointick, packages the tick data into bars, and inserts it into bar
	 * 
	 * @param bitcoinSymbol
	 * @param barSize
	 */
	private static void createBars(String bitcoinSymbol, BAR_SIZE barSize) {
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
				QueryManager.insertIntoBar(bitcoinSymbol, open, close, high, low, vwap, volumeSum, numTrades, change, gap, periodStart, periodEnd, barSize, false);
				
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}