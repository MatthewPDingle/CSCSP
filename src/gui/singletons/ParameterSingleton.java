package gui.singletons;

import java.util.ArrayList;
import java.util.Calendar;

public class ParameterSingleton {

	private static ParameterSingleton instance = null;

	private String xAxisMetric = "";
	private String yAxisMetric = "";
	private String sellMetric = "";
	private String sellOperator = ">=";
	private float sellValue = 0f;
	private String stopMetric = "";
	private Float stopValue = 0f;
	private Calendar fromCal = Calendar.getInstance();
	private Calendar toCal = Calendar.getInstance();
	private int xRes = 20;
	private int yRes = 20;
	private String mapColor = "";
	private boolean showCellTooltips = true;
	private boolean showStockTooltips = true;
	private boolean smoothMap = true;
	private int minNumResults = 200;
	private boolean showGroups = true;
	private String find = "";
	private int minLiquidity = 2000000;
	private float maxVolatility = 5f;
	private float minPrice = 3f;
	private String sector = "";
	private String industry = "";
	private boolean nyse = true;
	private boolean nasdaq = true;
	private boolean djia = true;
	private boolean sp500 = true;
	private boolean etf = false;
	private boolean bitcoin = false;
	private ArrayList<String> symbols = new ArrayList<String>(); // Contains both duration and symbol in "Duration - Symbol" format
	
	private boolean runFinished = false;
	
	protected ParameterSingleton() {
	}
	
	public static ParameterSingleton getInstance() {
		if (instance == null) {
			instance = new ParameterSingleton();
		}
		return instance;
	}	

	public String getxAxisMetric() {
		return xAxisMetric;
	}

	public void setxAxisMetric(String xAxisMetric) {
		this.xAxisMetric = xAxisMetric;
	}

	public String getyAxisMetric() {
		return yAxisMetric;
	}

	public void setyAxisMetric(String yAxisMetric) {
		this.yAxisMetric = yAxisMetric;
	}

	public String getSellMetric() {
		return sellMetric;
	}

	public void setSellMetric(String sellMetric) {
		this.sellMetric = sellMetric;
	}

	public float getSellValue() {
		return sellValue;
	}

	public void setSellValue(float sellValue) {
		this.sellValue = sellValue;
	}

	public String getStopMetric() {
		return stopMetric;
	}

	public void setStopMetric(String stopMetric) {
		this.stopMetric = stopMetric;
	}

	public Float getStopValue() {
		return stopValue;
	}

	public void setStopValue(Float stopValue) {
		this.stopValue = stopValue;
	}

	public Calendar getFromCal() {
		return fromCal;
	}

	public void setFromCal(Calendar fromCal) {
		this.fromCal = fromCal;
	}

	public Calendar getToCal() {
		return toCal;
	}

	public void setToCal(Calendar toCal) {
		this.toCal = toCal;
	}

	public int getxRes() {
		return xRes;
	}

	public void setxRes(int xRes) {
		this.xRes = xRes;
	}

	public int getyRes() {
		return yRes;
	}

	public void setyRes(int yRes) {
		this.yRes = yRes;
	}

	public String getMapColor() {
		return mapColor;
	}

	public void setMapColor(String mapColor) {
		this.mapColor = mapColor;
	}

	public boolean isShowCellTooltips() {
		return showCellTooltips;
	}

	public void setShowCellTooltips(boolean showCellTooltips) {
		this.showCellTooltips = showCellTooltips;
	}

	public boolean isShowStockTooltips() {
		return showStockTooltips;
	}

	public void setShowStockTooltips(boolean showStockTooltips) {
		this.showStockTooltips = showStockTooltips;
	}

	public boolean isSmoothMap() {
		return smoothMap;
	}

	public void setSmoothMap(boolean smoothMap) {
		this.smoothMap = smoothMap;
	}

	public int getMinNumResults() {
		return minNumResults;
	}

	public void setMinNumResults(int minNumResults) {
		this.minNumResults = minNumResults;
	}

	public boolean isShowGroups() {
		return showGroups;
	}

	public void setShowGroups(boolean showGroups) {
		this.showGroups = showGroups;
	}

	public int getMinLiquidity() {
		return minLiquidity;
	}

	public void setMinLiquidity(int minLiquidity) {
		this.minLiquidity = minLiquidity;
	}

	public float getMaxVolatility() {
		return maxVolatility;
	}

	public void setMaxVolatility(float maxVolatility) {
		this.maxVolatility = maxVolatility;
	}

	public float getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(float minPrice) {
		this.minPrice = minPrice;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public boolean isNyse() {
		return nyse;
	}

	public void setNyse(boolean nyse) {
		this.nyse = nyse;
	}

	public boolean isNasdaq() {
		return nasdaq;
	}

	public void setNasdaq(boolean nasdaq) {
		this.nasdaq = nasdaq;
	}

	public boolean isDjia() {
		return djia;
	}

	public void setDjia(boolean djia) {
		this.djia = djia;
	}

	public boolean isSp500() {
		return sp500;
	}

	public void setSp500(boolean sp500) {
		this.sp500 = sp500;
	}

	public boolean isEtf() {
		return etf;
	}

	public void setEtf(boolean etf) {
		this.etf = etf;
	}

	public boolean isBitcoin() {
		return bitcoin;
	}

	public void setBitcoin(boolean bitcoin) {
		this.bitcoin = bitcoin;
	}

	public boolean isRunFinished() {
		return runFinished;
	}

	public void setRunFinished(boolean runFinished) {
		this.runFinished = runFinished;
	}

	public String getSellOperator() {
		return sellOperator;
	}

	public void setSellOperator(String sellOperator) {
		this.sellOperator = sellOperator;
	}

	public String getFind() {
		return find;
	}

	public void setFind(String find) {
		this.find = find;
	}

	public ArrayList<String> getSymbols() {
		return symbols;
	}
	
	public ArrayList<String[]> getDurationSymbols() {
		ArrayList<String[]> durationSymbols = new ArrayList<String[]>();
		if (symbols != null) {
			for (String s : symbols) {
				durationSymbols.add(s.split(" - "));
			}
		}
		return durationSymbols;
	}

	public void setSymbols(ArrayList<String> symbols) {
		this.symbols = symbols;
	}
}