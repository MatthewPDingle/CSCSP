package data.downloaders.bitcoincharts;

import java.util.HashMap;

import data.TickConstants;

public class BitcoinChartsConstants {

	public static String URL = "http://api.bitcoincharts.com/v1/csv/";
	
	public static String FILE_TICK_HISTORY_BITFINEX_BTC_USD = "bitfinexUSD.csv.gz";
	public static String FILE_TICK_HISTORY_BITSTAMP_BTC_USD = "bitstampUSD.csv.gz";
	public static String FILE_TICK_HISTORY_BTCE_BTC_USD = "btceUSD.csv.gz";
	public static String FILE_TICK_HISTORY_BTCN_BTC_CNY = "btcnCNY.csv.gz";
	public static String FILE_TICK_HISTORY_KRAKEN_BTC_USD = "krakenUSD.csv.gz";
	public static String FILE_TICK_HISTORY_KRAKEN_BTC_EUR = "krakenEUR.csv.gz";
	public static String FILE_TICK_HISTORY_OKCOIN_BTC_CNY = "okcoinCNY.csv.gz";
	
	public static HashMap<String, String> FILENAME_TICKNAME_HASH = new HashMap<String, String>();
	
	static {
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_BITFINEX_BTC_USD, TickConstants.TICK_NAME_BITFINEX_BTC_USD);
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_BITSTAMP_BTC_USD, TickConstants.TICK_NAME_BITSTAMP_BTC_USD);
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_BTCE_BTC_USD, TickConstants.TICK_NAME_BTCE_BTC_USD);
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_BTCN_BTC_CNY, TickConstants.TICK_NAME_BTCN_BTC_CNY);
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_KRAKEN_BTC_USD, TickConstants.TICK_NAME_KRAKEN_BTC_USD);
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_KRAKEN_BTC_EUR, TickConstants.TICK_NAME_KRAKEN_BTC_EUR);
		FILENAME_TICKNAME_HASH.put(FILE_TICK_HISTORY_OKCOIN_BTC_CNY, TickConstants.TICK_NAME_OKCOIN_BTC_CNY);
	}
}