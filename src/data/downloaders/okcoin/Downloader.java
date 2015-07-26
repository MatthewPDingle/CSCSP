package data.downloaders.okcoin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import utils.CalendarUtils;
import utils.StringUtils;

import com.google.gson.Gson;

import constants.Constants;
import data.Bar;
import data.Converter;
import data.Tick;
import dbio.QueryManager;


public class Downloader {

	public static void main(String[] args) {

		ArrayList<Bar> bars = getMostRecentBarsFromTickHistory(Constants.BAR_SIZE.BAR_5M);
		for (Bar bar : bars) {
			QueryManager.insertOrUpdateIntoBar(bar);
			System.out.println(bar);
		}
	}

	/**
	 * Returns the most recent bars in order of oldest to newest
	 * The newest bar may be "partial" if the full bar duration has not passed yet.
	 * 
	 * @param barSize
	 * @return
	 */
	
	public static ArrayList<Bar> getMostRecentBarsFromTickHistory(Constants.BAR_SIZE barSize) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			String json = getTickHistoryJSON(OKCoinConstants.SYMBOL_BTCUSD, "6000");
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
				Tick tick = new Tick("okcoinBTCUSD", price, volume, timestamp);
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return bars;
	}
	
	public static void barTest() {
		String json = getBarHistoryJSON(OKCoinConstants.SYMBOL_BTCUSD, OKCoinConstants.BAR_DURATION_15M, "100");
		System.out.println(json);
		List<List> list = new Gson().fromJson(json, List.class);
		for (List bar : list) {
			String timeMS = bar.get(0).toString();
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
			float open = Float.parseFloat(bar.get(1).toString());
			float high = Float.parseFloat(bar.get(2).toString());
			float low = Float.parseFloat(bar.get(3).toString());
			float close = Float.parseFloat(bar.get(4).toString());
			float volume = Float.parseFloat(bar.get(5).toString());
			System.out.println(timestamp.getTime().toString());
			System.out.println(open);
			System.out.println(high);
			System.out.println(low);
			System.out.println(close);
			System.out.println(volume);
		}
	}
	
	public static String getTickHistoryJSON(String symbol, String since) {
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
			result = okCoin.requestHttpGet(OKCoinConstants.URL, "/trades.do", param);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getBarHistoryJSON(String symbol, String type, String numBarsBack) {
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
			result = okCoin.requestHttpGet(OKCoinConstants.URL, "/kline.do", param);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
