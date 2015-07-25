package data.downloaders.okcoin;

import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;


public class Downloader {

	public static void main(String[] args) {

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
			String open = bar.get(1).toString();
			String high = bar.get(2).toString();
			String low = bar.get(3).toString();
			String close = bar.get(4).toString();
			String volume = bar.get(5).toString();
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
			OKCoin okCoin = OKCoin.getInstance();
			String param = "";
			if (!StringUtil.isEmpty(symbol)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "symbol=" + symbol;
			}
			if (!StringUtil.isEmpty(since)) {
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
			OKCoin okCoin = OKCoin.getInstance();
			String param = "";
			if (!StringUtil.isEmpty(symbol)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "symbol=" + symbol;
			}
			if (!StringUtil.isEmpty(type)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "type=" + type;
			}
			if (!StringUtil.isEmpty(numBarsBack)) {
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
