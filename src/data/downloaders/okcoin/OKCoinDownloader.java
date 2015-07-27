package data.downloaders.okcoin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import utils.CalendarUtils;
import utils.StringUtils;

import com.google.gson.Gson;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.Converter;
import data.Tick;
import dbio.QueryManager;


public class OKCoinDownloader {

	public static void main(String[] args) {

		ArrayList<Bar> bars = getMostRecentBarsFromBarHistory(OKCoinConstants.SYMBOL_BTCUSD, BAR_SIZE.BAR_15M, 48);
		for (Bar bar : bars) {
			QueryManager.insertOrUpdateIntoBar(bar);
		}
		
		ArrayList<Bar> bars2 = getMostRecentBarsFromTickHistory(OKCoinConstants.SYMBOL_BTCUSD, Constants.BAR_SIZE.BAR_15M);
		for (Bar bar : bars2) {
			QueryManager.insertOrUpdateIntoBar(bar);
		}
	}

	/**
	 * Returns the most recent bars in order of oldest to newest
	 * The newest bar may be "partial" if the full bar duration has not passed yet.
	 * 
	 * @param barSize
	 * @return
	 */
	
	public static ArrayList<Bar> getMostRecentBarsFromTickHistory(String okCoinSymbol, Constants.BAR_SIZE barSize) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			String tickSymbol = "okcoin";
			if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCUSD)) {
				tickSymbol = "okcoinBTCUSD";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCCNY)) {
				tickSymbol = "okcoinBTCCNY";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCUSD)) {
				tickSymbol = "okcoinLTCUSD";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCCNY)) {
				tickSymbol = "okcoinLTCCNY";
			}
			
			String json = getTickHistoryJSON(okCoinSymbol, "6000");
			List<Map> list = new Gson().fromJson(json, List.class);
			
			// From oldest to newest
			ArrayList<Tick> ticks = new ArrayList<Tick>();
			for (Map map : list) {
				float volume = Float.parseFloat(map.get("amount").toString());
				String timeMS = map.get("date").toString();
				timeMS = timeMS.replace(".", "");
				if (timeMS.contains("E")) {
					timeMS = timeMS.substring(0, timeMS.indexOf("E"));
				}
				while (timeMS.length() < 10) {
					timeMS = timeMS + "0";
				}
				long ms = Long.parseLong(timeMS) * 1000;
				Calendar timestamp = Calendar.getInstance();
				timestamp.setTimeInMillis(ms);
				float price = Float.parseFloat(map.get("price").toString());
				Tick tick = new Tick(tickSymbol, price, volume, timestamp);
				ticks.add(tick);
			}
			
			if (ticks != null) {
				Tick firstTick = ticks.get(0);
				Calendar barStart = CalendarUtils.getBarStart(firstTick.timestamp, barSize);
				Calendar barEnd = CalendarUtils.getBarEnd(firstTick.timestamp, barSize);
				
				ArrayList<Tick> barTicks = new ArrayList<Tick>();
				float previousClose = 0f;
				if (ticks.size() > 0) {
					previousClose = ticks.get(0).price;
				}
				boolean firstBar = true;
				// Oldest to newest
				for (Tick tick : ticks) {
					if ((tick.timestamp.after(barStart) || CalendarUtils.areSame(tick.timestamp, barStart) == true) && tick.timestamp.before(barEnd)) {
						// We're still in the same bar so add this tick to the collection.
						barTicks.add(tick);
					}
					else {
						// Turn the collection of bar ticks into a proper bar.  Don't do the first bar because it will probably be partial and will also have no previousClose info.
						if (!firstBar) {
							Bar bar = Converter.ticksToBar(barTicks, barStart, barEnd, barSize, previousClose, firstBar);
							bars.add(bar);
							if (barTicks.size() > 0) {
								previousClose = barTicks.get(barTicks.size() - 1).price;
							}
						}
						firstBar = false;
						
						// It's a new bar so reset the bar start & end times.
						barStart = CalendarUtils.getBarStart(tick.timestamp, barSize);
						barEnd = CalendarUtils.getBarEnd(tick.timestamp, barSize);
						
						// Clear the collection of bar ticks so it can be used again for the next bar.
						barTicks.clear();
						barTicks.add(tick);
					}
				}
				// Create a final bar.  This one is partial
				Bar bar = Converter.ticksToBar(barTicks, barStart, barEnd, barSize, previousClose, true);
				bars.add(bar);
			}
			
			LogManager.getLogger("data.downloader").info("Got " + bars.size() + " bars from OKCoin tick history");
		}
		catch (Exception e) {
			e.printStackTrace();
			LogManager.getLogger("data.downloader").error(e.getStackTrace().toString());
		}
		
		return bars;
	}
	
	/**
	 * Note: 2M, 10M, 8H bars are not supported on OKCoin's API
	 * 
	 * @param barSize
	 * @return
	 */
	public static ArrayList<Bar> getMostRecentBarsFromBarHistory(String okCoinSymbol, Constants.BAR_SIZE barSize, int barCount) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			String barSymbol = "okcoin";
			if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCUSD)) {
				barSymbol = "okcoinBTCUSD";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCCNY)) {
				barSymbol = "okcoinBTCCNY";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCUSD)) {
				barSymbol = "okcoinLTCUSD";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCCNY)) {
				barSymbol = "okcoinLTCCNY";
			}
			
			String okBarDuration = OKCoinConstants.BAR_DURATION_15M; 
			int barMinutes = 0;
			switch (barSize) {
				case BAR_1M:
					okBarDuration = OKCoinConstants.BAR_DURATION_1M;
					barMinutes = 1;
					break;
				case BAR_5M:
					okBarDuration = OKCoinConstants.BAR_DURATION_5M;
					barMinutes = 5;
					break;
				case BAR_15M:
					okBarDuration = OKCoinConstants.BAR_DURATION_15M;
					barMinutes = 15;
					break;
				case BAR_30M:
					okBarDuration = OKCoinConstants.BAR_DURATION_30M;
					barMinutes = 30;
					break;
				case BAR_1H:
					okBarDuration = OKCoinConstants.BAR_DURATION_1H;
					barMinutes = 60;
					break;
				case BAR_2H:
					okBarDuration = OKCoinConstants.BAR_DURATION_2H;
					barMinutes = 120;
					break;
				case BAR_4H:
					okBarDuration = OKCoinConstants.BAR_DURATION_4H;
					barMinutes = 240;
					break;
				case BAR_6H:
					okBarDuration = OKCoinConstants.BAR_DURATION_6H;
					barMinutes = 360;
					break;
				case BAR_12H:
					okBarDuration = OKCoinConstants.BAR_DURATION_12H;
					barMinutes = 720;
					break;
				case BAR_1D:
					okBarDuration = OKCoinConstants.BAR_DURATION_1D;
					barMinutes = 1440;
					break;
				default:
					break;
			}
			String json = getBarHistoryJSON(okCoinSymbol, okBarDuration, new Integer(barCount + 1).toString());
			List<List> list = new Gson().fromJson(json, List.class);
			
			Float previousClose = null;
			
			// From oldest to newest
			for (List jsonBar : list) {
				String timeMS = jsonBar.get(0).toString();
				timeMS = timeMS.replace(".", "");
				if (timeMS.contains("E")) {
					timeMS = timeMS.substring(0, timeMS.indexOf("E"));
				}
				while (timeMS.length() < 10) {
					timeMS = timeMS + "0";
				}
				long ms = Long.parseLong(timeMS) * 1000;
				Calendar periodStart = Calendar.getInstance();
				periodStart.setTimeInMillis(ms);
				Calendar periodEnd = Calendar.getInstance();
				periodEnd.setTime(periodStart.getTime());
				periodEnd.add(Calendar.MINUTE, barMinutes);
				float open = Float.parseFloat(jsonBar.get(1).toString());
				float high = Float.parseFloat(jsonBar.get(2).toString());
				float low = Float.parseFloat(jsonBar.get(3).toString());
				float close = Float.parseFloat(jsonBar.get(4).toString());
				float volume = Float.parseFloat(jsonBar.get(5).toString());
				float vwapEstimate = (open + close + high + low) / 4f;
				Float change = null;
				Float gap = null;
				if (previousClose != null) {
					change = close - previousClose; 
					gap = open - previousClose;
				}
			
				Bar bar = new Bar(barSymbol, open, close, high, low, vwapEstimate, volume, null, change, gap, periodStart, periodEnd, barSize, false);
				bars.add(bar);
				
				previousClose = close;
			}
			
			// Set the most recent one to partial and toss the oldest one (we got one more bar than we needed)
			if (bars.size() > 0) {
				bars.get(bars.size() - 1).partial = true;
				bars.remove(0);
			}
			
			LogManager.getLogger("data.downloader").info("Got " + bars.size() + " bars from OKCoin bar history");
		}
		catch (Exception e) {
			e.printStackTrace();
			LogManager.getLogger("data.downloader").error(e.getStackTrace().toString());
		}
		
		return bars;
	}
	
	private static String getTickHistoryJSON(String symbol, String since) {
		String result = "";
		try {
			OKCoinAPI okCoin = OKCoinAPI.getInstance();
			String param = "";
			if (!StringUtils.isEmpty(symbol)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "symbol=" + symbol;
			}
			if (!StringUtils.isEmpty(since)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "since=" + since;
			}
			String url = OKCoinConstants.URL_USA;
			if (symbol != null && symbol.endsWith("cny")) {
				url = OKCoinConstants.URL_CHINA;
			}
			result = okCoin.requestHttpGet(url, "/trades.do", param);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static String getBarHistoryJSON(String symbol, String type, String numBarsBack) {
		String result = "";
		try {
			OKCoinAPI okCoin = OKCoinAPI.getInstance();
			String param = "";
			if (!StringUtils.isEmpty(symbol)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "symbol=" + symbol;
			}
			if (!StringUtils.isEmpty(type)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "type=" + type;
			}
			if (!StringUtils.isEmpty(numBarsBack)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "size=" + numBarsBack;
			}
			String url = OKCoinConstants.URL_USA;
			if (symbol != null && symbol.endsWith("cny")) {
				url = OKCoinConstants.URL_CHINA;
			}
			result = okCoin.requestHttpGet(url, "/kline.do", param);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
