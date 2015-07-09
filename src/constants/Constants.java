package constants;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {

	public static String URL = "jdbc:postgresql://localhost:5432/stocks";
	public static String USERNAME = "postgres";
	public static String PASSWORD = "graham23";
	
	public static enum BAR_SIZE {BAR_1M, BAR_2M, BAR_5M, BAR_10M, BAR_15M, BAR_30M, BAR_1H, BAR_2H, BAR_4H, BAR_6H, BAR_8H, BAR_12H, BAR_1D};
	
	public static String BAR_TABLE = "bar";			// Replaces basicr.  This table only has bar data.  If you get a datasource that is tick-based, convert it to bars and put the data here.
	public static String METRICS_TABLE = "metrics";	// Replaces all metric_ tables
	
	public static String INDEXLIST_TABLE = "indexlist"; // NYSE, Nasdaq, ETF, Index, Bitcoin
	public static String SECTORANDINDUSTRY_TABLE = "sectorandindustry";
	
	public static String REALTIMESYMBOLS_TABLE = "realtimesymbols";

	// Datasource URLs.  These occasionally break and need to be fixed or replaced.
	public static String YAHOO_NYSE_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5ENYA&c="; // "c" parameter = a page number (0+) // Broken as of 7/2/2015
	public static String NYSE_SYMBOL_URL = "http://www1.nyse.com/indexes/nyaindex.csv";
	public static String YAHOO_NASDAQ_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5EIXIC&c=";
	public static String YAHOO_DJIA_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5EDJI&c=";
	public static String YAHOO_ETF_SYMBOL_URL = "http://finance.yahoo.com/etf/lists/?mod_id=mediaquotesetf&tab=tab3&scol=volint&stype=desc&rcnt=100"; 
	public static String WIKI_ETF_SYMBOL_URL = "http://en.wikipedia.org/wiki/List_of_American_exchange-traded_funds";
	public static String YAHOO_REALTIME_QUOTE_URL = "http://download.finance.yahoo.com/d/quotes.csv?f=sk1d1c6ohgv&e=.csv&s=";
	public static String WIKI_SP500_SYMBOL_URL = "http://en.wikipedia.org/wiki/List_of_S%26P_500_companies"; // Broken as of 7/2/2015
	public static String OKFN_SP500_SYMBOL_URL = "http://data.okfn.org/data/core/s-and-p-500-companies/r/constituents.csv";
	
	public static int WINDOW_WIDTH = 560;
	public static int WINDOW_HEIGHT = 560;
	public static int WORLD_WIDTH = 100;
	public static int WORLD_HEIGHT = 100;
	
	public static HashMap<String, Float> METRIC_MIN_MAX_VALUE = new HashMap<String, Float>();
	static {
		METRIC_MIN_MAX_VALUE.put("min_av10ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_av10ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_av25ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_av25ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_av50ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_av50ema", 4f);		
		METRIC_MIN_MAX_VALUE.put("min_av75ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_av75ema", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_bv10ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_bv10ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_bv25ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_bv25ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_bv50ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_bv50ema", 4f);		
		METRIC_MIN_MAX_VALUE.put("min_bv75ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_bv75ema", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_cv10ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_cv10ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_cv25ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_cv25ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_cv50ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_cv50ema", 4f);		
		METRIC_MIN_MAX_VALUE.put("min_cv75ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_cv75ema", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_dv10ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_dv10ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_dv25ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_dv25ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_dv50ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_dv50ema", 4f);		
		METRIC_MIN_MAX_VALUE.put("min_dv75ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_dv75ema", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_ev10ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_ev10ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_ev25ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_ev25ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_ev50ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_ev50ema", 4f);		
		METRIC_MIN_MAX_VALUE.put("min_ev75ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_ev75ema", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_fv10ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_fv10ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_fv25ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_fv25ema", 4f);
		METRIC_MIN_MAX_VALUE.put("min_fv50ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_fv50ema", 4f);		
		METRIC_MIN_MAX_VALUE.put("min_fv75ema", -4f);
		METRIC_MIN_MAX_VALUE.put("max_fv75ema", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_consecutiveupdays", 0f);
		METRIC_MIN_MAX_VALUE.put("max_consecutiveupdays", 10f);
		
		METRIC_MIN_MAX_VALUE.put("min_consecutivedowndays", 0f);
		METRIC_MIN_MAX_VALUE.put("max_consecutivedowndays", 10f);
		
		METRIC_MIN_MAX_VALUE.put("min_dv2", -6f);
		METRIC_MIN_MAX_VALUE.put("max_dv2", 6f);
		
		METRIC_MIN_MAX_VALUE.put("min_dvfading4", -6f);
		METRIC_MIN_MAX_VALUE.put("max_dvfading4", 6f);
		
		METRIC_MIN_MAX_VALUE.put("min_rsi14", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi14", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi5", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi5", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi2", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi2", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_rsi14alpha", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi14alpha", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi5alpha", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi5alpha", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi2alpha", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi2alpha", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_mfi14", 0f);
		METRIC_MIN_MAX_VALUE.put("max_mfi14", 100f);
		METRIC_MIN_MAX_VALUE.put("min_mfi5", 0f);
		METRIC_MIN_MAX_VALUE.put("max_mfi5", 100f);
		METRIC_MIN_MAX_VALUE.put("min_mfi2", 0f);
		METRIC_MIN_MAX_VALUE.put("max_mfi2", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_rsi10ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi10ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi25ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi25ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi50ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi50ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi75ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi75ema", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_pricesd20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd20", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd50", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd100", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd100", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd200", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd200", 6f);
		
		METRIC_MIN_MAX_VALUE.put("min_volumesd20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd20", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd50", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd100", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd100", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd200", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd200", 6f);
		
		METRIC_MIN_MAX_VALUE.put("min_priceboll20", -4f);
		METRIC_MIN_MAX_VALUE.put("max_priceboll20", 4f);
		METRIC_MIN_MAX_VALUE.put("min_priceboll50", -4f);
		METRIC_MIN_MAX_VALUE.put("max_priceboll50", 4f);
		METRIC_MIN_MAX_VALUE.put("min_priceboll100", -4f);
		METRIC_MIN_MAX_VALUE.put("max_priceboll100", 4f);
		METRIC_MIN_MAX_VALUE.put("min_priceboll200", -4f);
		METRIC_MIN_MAX_VALUE.put("max_priceboll200", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_gapboll10", -4f);
		METRIC_MIN_MAX_VALUE.put("max_gapboll10", 4f);
		METRIC_MIN_MAX_VALUE.put("min_gapboll20", -4f);
		METRIC_MIN_MAX_VALUE.put("max_gapboll20", 4f);
		METRIC_MIN_MAX_VALUE.put("min_gapboll50", -4f);
		METRIC_MIN_MAX_VALUE.put("max_gapboll50", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_intradayboll10", -4f);
		METRIC_MIN_MAX_VALUE.put("max_intradayboll10", 4f);
		METRIC_MIN_MAX_VALUE.put("min_intradayboll20", -4f);
		METRIC_MIN_MAX_VALUE.put("max_intradayboll20", 4f);
		METRIC_MIN_MAX_VALUE.put("min_intradayboll50", -4f);
		METRIC_MIN_MAX_VALUE.put("max_intradayboll50", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_volumeboll20", -4f);
		METRIC_MIN_MAX_VALUE.put("max_volumeboll20", 4f);
		METRIC_MIN_MAX_VALUE.put("min_volumeboll50", -4f);
		METRIC_MIN_MAX_VALUE.put("max_volumeboll50", 4f);
		METRIC_MIN_MAX_VALUE.put("min_volumeboll100", -4f);
		METRIC_MIN_MAX_VALUE.put("max_volumeboll100", 4f);
		METRIC_MIN_MAX_VALUE.put("min_volumeboll200", -4f);
		METRIC_MIN_MAX_VALUE.put("max_volumeboll200", 4f);
		
		METRIC_MIN_MAX_VALUE.put("min_dvol10ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol10ema", 8f);
		METRIC_MIN_MAX_VALUE.put("min_dvol25ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol25ema", 8f);
		METRIC_MIN_MAX_VALUE.put("min_dvol50ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol50ema", 8f);
		METRIC_MIN_MAX_VALUE.put("min_dvol75ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol75ema", 8f);
		
		METRIC_MIN_MAX_VALUE.put("min_breakout20", -1f);
		METRIC_MIN_MAX_VALUE.put("max_breakout20", 1f);
		METRIC_MIN_MAX_VALUE.put("min_breakout50", -1f);
		METRIC_MIN_MAX_VALUE.put("max_breakout50", 1f);
		METRIC_MIN_MAX_VALUE.put("min_breakout100", -1f);
		METRIC_MIN_MAX_VALUE.put("max_breakout100", 1f);
		METRIC_MIN_MAX_VALUE.put("min_breakout200", -1f);
		METRIC_MIN_MAX_VALUE.put("max_breakout200", 1f);
		
		METRIC_MIN_MAX_VALUE.put("min_williamsr10", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsr10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsr20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsr20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsr50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsr50", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_williamsralpha10", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsralpha10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsralpha20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsralpha20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsralpha50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsralpha50", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_macd12_26_9", -10f);
		METRIC_MIN_MAX_VALUE.put("max_macd12_26_9", 10f);
		METRIC_MIN_MAX_VALUE.put("min_macd20_40_9", -10f);
		METRIC_MIN_MAX_VALUE.put("max_macd20_40_9", 10f);
		METRIC_MIN_MAX_VALUE.put("min_macd40_80_9", -10f);
		METRIC_MIN_MAX_VALUE.put("max_macd40_80_9", 10f);
		
		METRIC_MIN_MAX_VALUE.put("min_macddivergence12_26_9", -2.5f);
		METRIC_MIN_MAX_VALUE.put("max_macddivergence12_26_9", 2.5f);
		METRIC_MIN_MAX_VALUE.put("min_macddivergence20_40_9", -2.5f);
		METRIC_MIN_MAX_VALUE.put("max_macddivergence20_40_9", 2.5f);
		METRIC_MIN_MAX_VALUE.put("min_macddivergence40_80_9", -2.5f);
		METRIC_MIN_MAX_VALUE.put("max_macddivergence40_80_9", 2.5f);
		
		METRIC_MIN_MAX_VALUE.put("min_psar", -10f);
		METRIC_MIN_MAX_VALUE.put("max_psar", 10f);
		
		METRIC_MIN_MAX_VALUE.put("min_ultimateoscillator", 0f);
		METRIC_MIN_MAX_VALUE.put("max_ultimateoscillator", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_aroonoscillator", 0f);
		METRIC_MIN_MAX_VALUE.put("max_aroonoscillator", 100f);
		
		METRIC_MIN_MAX_VALUE.put("min_cci10", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci20", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci40", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci40", 100f);
	}
	
	public static ArrayList<String> METRICS = new ArrayList<String>();
	static {
//		METRICS.add("av10ema");
//		METRICS.add("av25ema");
//		METRICS.add("av50ema");
//		METRICS.add("av75ema");
//		
//		METRICS.add("bv10ema");
//		METRICS.add("bv25ema");
//		METRICS.add("bv50ema");
//		METRICS.add("bv75ema");
//		
//		METRICS.add("cv10ema");
//		METRICS.add("cv25ema");
//		METRICS.add("cv50ema");
//		METRICS.add("cv75ema");
		
		METRICS.add("dv10ema");
		METRICS.add("dv25ema");
		METRICS.add("dv50ema");
		METRICS.add("dv75ema");
		
//		METRICS.add("ev10ema");
//		METRICS.add("ev25ema");
//		METRICS.add("ev50ema");
//		METRICS.add("ev75ema");
//		
//		METRICS.add("fv10ema");
//		METRICS.add("fv25ema");
//		METRICS.add("fv50ema");
//		METRICS.add("fv75ema");
		
		METRICS.add("dv2");
		METRICS.add("dvfading4");
		
		METRICS.add("rsi2");
		METRICS.add("rsi5");
		METRICS.add("rsi14");
		
		METRICS.add("rsi2alpha");
		METRICS.add("rsi5alpha");
		METRICS.add("rsi14alpha");
		
		METRICS.add("rsi10ema");
		METRICS.add("rsi25ema");
		METRICS.add("rsi50ema");
		METRICS.add("rsi75ema");
		
		METRICS.add("mfi2");
		METRICS.add("mfi5");
		METRICS.add("mfi14");
		
		METRICS.add("consecutiveupdays");
		METRICS.add("consecutivedowndays");
		
		METRICS.add("priceboll20");
		METRICS.add("priceboll50");
		METRICS.add("priceboll100");
		METRICS.add("priceboll200");
		
		METRICS.add("gapboll10");
		METRICS.add("gapboll20");
		METRICS.add("gapboll50");
		
		METRICS.add("intradayboll10");
		METRICS.add("intradayboll20");
		METRICS.add("intradayboll50");
		
		METRICS.add("volumeboll20");
		METRICS.add("volumeboll50");
		METRICS.add("volumeboll100");
		METRICS.add("volumeboll200");
		
		METRICS.add("dvol10ema");
		METRICS.add("dvol25ema");
		METRICS.add("dvol50ema");
		METRICS.add("dvol75ema");
		
//		METRICS.add("breakout20");
//		METRICS.add("breakout50");
//		METRICS.add("breakout100");
//		METRICS.add("breakout200");
		
		METRICS.add("williamsr10");
		METRICS.add("williamsr20");
		METRICS.add("williamsr50");
		
		METRICS.add("williamsralpha10");
		METRICS.add("williamsralpha20");
		METRICS.add("williamsralpha50");
		
		METRICS.add("macd12_26_9");
		METRICS.add("macd20_40_9");
		METRICS.add("macd40_80_9");
		
		METRICS.add("macddivergence12_26_9");
		METRICS.add("macddivergence20_40_9");
		METRICS.add("macddivergence40_80_9");
		
		METRICS.add("psar");
		
		METRICS.add("ultimateoscillator");
		
		METRICS.add("aroonoscillator");
		
		METRICS.add("cci10");
		METRICS.add("cci20");
		METRICS.add("cci40");
		
		METRICS.add("mvol100");
	}
	
	public static String OTHER_SELL_METRIC_NUM_BARS_LATER = "# Bars Later";
	public static String OTHER_SELL_METRIC_PERCENT_UP = "% Up";
	public static String OTHER_SELL_METRIC_PERCENT_DOWN = "% Down";
	
	public static ArrayList<String> OTHER_SELL_METRICS = new ArrayList<String>();
	static {
		OTHER_SELL_METRICS.add(OTHER_SELL_METRIC_NUM_BARS_LATER);
		OTHER_SELL_METRICS.add(OTHER_SELL_METRIC_PERCENT_UP);
		OTHER_SELL_METRICS.add(OTHER_SELL_METRIC_PERCENT_DOWN);
	}
	
	public static String STOP_METRIC_NONE = "None";
	public static String STOP_METRIC_PERCENT_DOWN = "% Down";
	public static String STOP_METRIC_PERCENT_UP = "% Up";
	public static String STOP_METRIC_NUM_BARS = "# Bars";
	
	public static ArrayList<String> STOP_METRICS = new ArrayList<String>();
	static {
		STOP_METRICS.add(STOP_METRIC_NONE);
		STOP_METRICS.add(STOP_METRIC_PERCENT_DOWN);
		STOP_METRICS.add(STOP_METRIC_PERCENT_UP);
		STOP_METRICS.add(STOP_METRIC_NUM_BARS);
	}
	
	public static String MAP_COLOR_OPTION_ALL_NUM_POSITIONS = "All - # Positions";
	public static String MAP_COLOR_OPTION_ALL_PERCENT_POSITIONS = "All - % Positions";
	public static String MAP_COLOR_OPTION_ALL_MEAN_RETURN = "All - Mean Return";
	public static String MAP_COLOR_OPTION_ALL_GEOMEAN_RETURN = "All - Geo-Mean Return";
	public static String MAP_COLOR_OPTION_ALL_MEDIAN_RETURN = "All - Median Return";
	public static String MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT = "All - Mean Win %";
	public static String MAP_COLOR_OPTION_ALL_MEAN_POSITION_DURATION = "All - Mean Position Duration";
	public static String MAP_COLOR_OPTION_ALL_MAX_DRAWDOWN = "All - Max Drawdown %";
	public static String MAP_COLOR_OPTION_ALL_GEOMEAN_PER_DAY = "All - Geo-Mean / Bar Return";
	public static String MAP_COLOR_OPTION_ALL_SHARPE_RATIO = "All - Sharpe Ratio";
	public static String MAP_COLOR_OPTION_ALL_SORTINO_RATIO = "All - Sortino Ratio";
	
	public static String MAP_COLOR_OPTION_ALL_ALPHA_MEAN_RETURN = "All - Alpha Mean Return";
	public static String MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_RETURN = "All - Alpha Geo-Mean Return";
	public static String MAP_COLOR_OPTION_ALL_ALPHA_MEDIAN_RETURN = "All - Alpha Median Return";
	public static String MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT = "All - Alpha Mean Win %";
	public static String MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_DAY = "All - Alpha Geo-Mean / Bar";
	
	public static String MAP_COLOR_OPTION_METRIC_NUM_POSITIONS = "Metric - # Positions";
	public static String MAP_COLOR_OPTION_METRIC_PERCENT_POSITIONS = "Metric - % Positions";
	public static String MAP_COLOR_OPTION_METRIC_MEAN_RETURN = "Metric - Mean Return";
	public static String MAP_COLOR_OPTION_METRIC_GEOMEAN_RETURN = "Metric - Geo-Mean Return";
	public static String MAP_COLOR_OPTION_METRIC_MEDIAN_RETURN = "Metric - Median Return";
	public static String MAP_COLOR_OPTION_METRIC_MEAN_WIN_PERCENT = "Metric - Mean Win %";
	public static String MAP_COLOR_OPTION_METRIC_MEAN_POSITION_DURATION = "Metric - Mean Position Duration";
	public static String MAP_COLOR_OPTION_METRIC_GEOMEAN_PER_DAY = "Metric - Geo-Mean / Bar Return";
	
	public static String MAP_COLOR_OPTION_STOP_NUM_POSITIONS = "Stop - # Positions";
	public static String MAP_COLOR_OPTION_STOP_PERCENT_POSITIONS = "Stop - % Positions";
	public static String MAP_COLOR_OPTION_STOP_MEAN_RETURN = "Stop - Mean Return";
	public static String MAP_COLOR_OPTION_STOP_GEOMEAN_RETURN = "Stop - Geo-Mean Return";
	public static String MAP_COLOR_OPTION_STOP_MEDIAN_RETURN = "Stop - Median Return";
	public static String MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT = "Stop - Mean Win %";
	public static String MAP_COLOR_OPTION_STOP_MEAN_POSITION_DURATION = "Stop - Mean Position Duration";
	public static String MAP_COLOR_OPTION_STOP_GEOMEAN_PER_DAY = "Stop - Geo-Mean / Bar Return";
	
	public static String MAP_COLOR_OPTION_END_NUM_POSITIONS = "End - # Positions";
	public static String MAP_COLOR_OPTION_END_PERCENT_POSITIONS = "End - % Positions";
	public static String MAP_COLOR_OPTION_END_MEAN_RETURN = "End - Mean Return";
	public static String MAP_COLOR_OPTION_END_GEOMEAN_RETURN = "End - Geo-Mean Return";
	public static String MAP_COLOR_OPTION_END_MEDIAN_RETURN = "End - Median Return";
	public static String MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT = "End - Mean Win %";
	public static String MAP_COLOR_OPTION_END_MEAN_POSITION_DURATION = "End - Mean Position Duration";
	public static String MAP_COLOR_OPTION_END_GEOMEAN_PER_DAY = "End - Geo-Mean / Bar Return";
	
	public static ArrayList<String> MAP_COLOR_OPTIONS = new ArrayList<String>();
	static {
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_PERCENT_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_MEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_GEOMEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_MEDIAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_MEAN_POSITION_DURATION);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_MAX_DRAWDOWN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_GEOMEAN_PER_DAY);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_SHARPE_RATIO);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_SORTINO_RATIO);
		
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_ALPHA_MEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_ALPHA_MEDIAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_DAY);
		
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_NUM_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_PERCENT_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_MEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_GEOMEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_MEDIAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_MEAN_WIN_PERCENT);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_MEAN_POSITION_DURATION);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_METRIC_GEOMEAN_PER_DAY);
		
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_NUM_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_PERCENT_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_MEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_GEOMEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_MEDIAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_MEAN_POSITION_DURATION);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_STOP_GEOMEAN_PER_DAY);
		
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_NUM_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_PERCENT_POSITIONS);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_MEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_GEOMEAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_MEDIAN_RETURN);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_MEAN_POSITION_DURATION);
		MAP_COLOR_OPTIONS.add(MAP_COLOR_OPTION_END_GEOMEAN_PER_DAY);
	}
}