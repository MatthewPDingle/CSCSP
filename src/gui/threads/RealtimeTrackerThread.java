package gui.threads;

import gui.MapCellPanel;
import gui.MapSymbol;
import gui.singletons.MapSymbolSingleton;
import gui.singletons.ParameterSingleton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import metrics.MetricsCalculatorRealtime;
import trading.TradeMonitor;
import constants.Constants;
import dbio.QueryManager;

public class RealtimeTrackerThread extends Thread {

	private ArrayList<String> usedMetrics = new ArrayList<String>();
	private RealtimeTrackerThreadResultSetter setter = null;
	private HashMap<String, String> result = new HashMap<String, String>();
	private boolean running = false;
	private MapCellPanel mcp = null;
	private MapSymbolSingleton mss = MapSymbolSingleton.getInstance();
	private ParameterSingleton ps = ParameterSingleton.getInstance();
	private int updateAllFrequency = 2;
	private int updateCounter = 0;
	
	/**
	 * Constructor
	 * @param maps
	 */
	public RealtimeTrackerThread(MapCellPanel mcp) {
		usedMetrics.add("priceboll20");
		usedMetrics.addAll(getUsedMetrics(mcp));
		this.mcp = mcp;
		running = true;
	}
	
	@Override
	public void run() {
		try {
			// Delete any junk that might be in basicr or metric tables
			QueryManager.deleteTodayFromBasic();
			QueryManager.deleteTodayFromMetricTables();
			
			while (this.running) {
				runOnMaps();
				updateCounter++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void runOnMaps() {
		try {
			System.out.println(Calendar.getInstance().getTime().toString());
			System.out.println("MapSymbols.size() = " + mss.getMapSymbols().size());
			System.out.println("HPMapSymbols.size() = " + mss.getHighPriorityMapSymbols().size());
			
			// Get list of symbols that need updates (Last Trading Day)
			ArrayList<String> symbolList = new ArrayList<String>();
			
			// First round do everything
			if (updateCounter == 0) {
				for (MapSymbol ms:mss.getMapSymbols()) {
					symbolList.add(ms.getSymbol());
				}
			}
			// Every X times, track (1 / updateAllFrequency) of everything.  Each time this hits it shifts to the next set.
			else if (updateCounter % updateAllFrequency == 0) {
				int chunk = (updateCounter / updateAllFrequency) % 10; // 0-3
				for (MapSymbol ms:mss.getMapSymbols()) {
					if (mss.getMapSymbols().indexOf(ms) % 10 == chunk)
						symbolList.add(ms.getSymbol());
				}
			}
			// Otherwise track high priorities if they exist
			else {	
				if (mss.getHighPriorityMapSymbols().size() == 0) {
					for (MapSymbol ms:mss.getMapSymbols()) {
						symbolList.add(ms.getSymbol());
					}
				}
				else {
					for (MapSymbol ms:mss.getHighPriorityMapSymbols()) {
						symbolList.add(ms.getSymbol());
					}
				}
			}
			// Add open positions to symbolList if needed
			ArrayList<HashMap<String, Object>> openPositions = QueryManager.getOpenPositions();
			for (HashMap<String, Object> openPosition:openPositions) {
				String symbol = openPosition.get("symbol").toString();
				if (!symbolList.contains(symbol))
					symbolList.add(symbol);
			}
			
			// Add SPY to the symbolList if needed
			if (!symbolList.contains("SPY"))
				symbolList.add("SPY");
			System.out.println("symbolList.size() = " + symbolList.size());
			
			// Delete most recent trading day for the symbols we're tracking during this loop
			QueryManager.deleteMostRecentTradingDayFromBasic(symbolList);
			QueryManager.deleteMostRecentTradingDayFromMetricTables(symbolList, usedMetrics);
			
			// Create URL strings
			ArrayList<String> symbolStrings = getSymbolStrings(symbolList);
			
			// Connect to Yahoo Finance & Update basicr with today's current info
			getUpdatedYahooQuotesAndSaveToDB(symbolStrings);
//			stockMovementTester(symbolList);
			
			// Update the MapSymbol's last updated time
			for (MapSymbol ms:mss.getMapSymbols()) {
				if (symbolList.contains(ms.getSymbol())) {
					ms.setLastUpdated(Calendar.getInstance());
				}
			}
			
			// Calculate the metrics
			MetricsCalculatorRealtime.calculateMetricsRealtime(this.usedMetrics, symbolList);
			mss.setMapSymbols(QueryManager.getMapSymbols());
			
			// Notify the GUI
			result.put("Update Complete", "");
			setter.setResult(result);
			
			// See what symbols are high priority
			mss.prioritizeMapSymbols(this.mcp);
			
			// Monitor trading signals
			TradeMonitor tradeMonitor = new TradeMonitor();
			tradeMonitor.monitorTrading();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getUsedMetrics(MapCellPanel mcp) {
		ArrayList<String> metrics = new ArrayList<String>();
		try {	
			String buy1 = ps.getxAxisMetric();
			String buy2 = ps.getyAxisMetric();
			String sell = ps.getSellMetric();
			metrics.add(buy1);
			metrics.add(buy2);
			metrics.add(sell);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}

	public static ArrayList<String> getSymbolStrings(ArrayList<String> symbolList) {
		ArrayList<String> symbolStrings = new ArrayList<String>();
		try {
			int c = 0;
			String oneString = "";
			for (String symbol:symbolList) {
				c++;
				if (c <= 100) {
					oneString += symbol + "+";
				}
				else {
					if (oneString.endsWith("+")) {
						oneString = oneString.substring(0, oneString.length() - 1);
					}
					if (!symbolStrings.contains(oneString))
						symbolStrings.add(oneString);
					c = 0;
					oneString = "";
				}
			}
			if (oneString.endsWith("+")) {
				oneString = oneString.substring(0, oneString.length() - 1);
			}
			if (!symbolStrings.contains(oneString))
				symbolStrings.add(oneString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbolStrings;
	}
	
	private void stockMovementTester(ArrayList<String> symbolStrings) {
		try {
			ArrayList<HashMap<String, Object>> hashList = new ArrayList<HashMap<String, Object>>();
			for (String symbol:symbolStrings) {
				HashMap<String, Object> hash = QueryManager.getLatestQuote(symbol);
				hashList.add(hash);
			}
			QueryManager.saveFakeQuotes(hashList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getUpdatedYahooQuotesAndSaveToDB(ArrayList<String> symbolStrings) {
		try  {
			for (String symbolString:symbolStrings) {
				System.out.println("Hitting URL #" + symbolStrings.indexOf(symbolString));
				
				String url = Constants.YAHOO_REALTIME_QUOTE_URL + symbolString;
				
				ArrayList<String> records = new ArrayList<String>();
	
				URL yahoo = new URL(url);
			    URLConnection yahooConnection = yahoo.openConnection();
			    BufferedReader in = new BufferedReader(new InputStreamReader(yahooConnection.getInputStream()));
				
			    // Reverse it so moving averages go in the right direction
			    String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			    	try {
				    	String[] lineValues = inputLine.split(",");
					  	String symbol = lineValues[0];
					  	String closeString = lineValues[1];
				    	String date = lineValues[2];
				    	String changeString = lineValues[3];
				    	changeString = changeString.replaceAll("\\+", "");
				    	changeString = changeString.replaceAll("\"", "");
				    	float change = new Float(changeString);
					  	float open = new Float(lineValues[4]);
					  	float high = new Float(lineValues[5]);
					  	float low = new Float(lineValues[6]);
					  	long volume = new Long(lineValues[7]);
					  	
					  	symbol = symbol.replaceAll("\"", "");
					  	
					  	// Get rid of the time and xml from the close
					  	closeString = closeString.substring(closeString.indexOf(">") + 1);
					  	closeString = closeString.substring(0,closeString.indexOf("<"));
					  	float close = new Float(closeString);
					  	
					  	if (close < low) low = close;
					  	if (close > high) high = close;
					  	
					  	float gap = open - (close - change);
					  
					  	change = (float)(Math.round(change*100.0f)/100.0f);
					  	gap = (float)(Math.round(gap*100.0f)/100.0f);
					  	
					  	// Convert from 4/8/2010 to 2010-04-08 format
					  	date = date.replaceAll("\"", "");
					    String month = date.substring(0, date.indexOf("/"));
					    String dates = date.substring(date.indexOf("/") + 1, date.indexOf("/201"));
					    String year = date.substring(date.length() - 4);
					    if (month.length() == 1) {
					    	month = "0" + month;
					    }
					    if (dates.length() == 1) {
					    	dates = "0" + dates;
					    }
					    date = year + "-" + month + "-" + dates;
					  	
					  	StringBuilder sb = new StringBuilder();
					  	sb.append("('");
					  	sb.append(symbol);
					  	sb.append("', '");
					  	sb.append(date);
					  	sb.append("', ");
					  	sb.append(volume);
					  	sb.append(", ");
					  	sb.append(open);
					  	sb.append(", ");
					  	sb.append(close);
					  	sb.append(", ");
					  	sb.append(high);
					  	sb.append(", ");
					  	sb.append(low);
					  	sb.append(", ");
					  	sb.append(change);
					  	sb.append(", ");
					  	sb.append(gap);
					  	sb.append(", ");
					  	sb.append("true");
					  	sb.append(")");
					  	
					  	records.add(sb.toString());
			    	}
			    	catch (Exception e) {}
			    }
			    in.close();
				
				if (records.size() > 0) {
					QueryManager.insertBasicPartials(records);
				}
			} // End for 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void setMapUpdateThreadResultSetter (RealtimeTrackerThreadResultSetter setter) {
		this.setter = setter;
	}
}