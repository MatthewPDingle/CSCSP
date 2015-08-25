package trading;

import java.util.ArrayList;
import java.util.HashMap;

import constants.Constants;
import data.BarKey;
import dbio.QueryManager;
import gui.MapSymbol;
import gui.singletons.MapSymbolSingleton;
import gui.singletons.MetricSingleton;
import gui.threads.RealtimeTrackerThread;
import metrics.MetricsUpdater;

public class TradeMonitor {

	private MapSymbolSingleton mss = MapSymbolSingleton.getInstance();
	
	public void monitorTrading() {
		// Potential new trades
		monitorOpen();
		// Potential closing of existing positions
		monitorClose();
	}
	
	/**
	 * Monitors potential new trades
	 */
	private void monitorOpen() {
		try {
			ArrayList<MapSymbol> hpSymbols = mss.getHighPriorityMapSymbols();
			if (hpSymbols.size() > 0) {
				// Figure out if we want more long or short positions - TODO: this is really simplistic and needs to be redone.
				boolean[] requests = getDesiredTradeTypes();
				boolean requestShort = requests[0];
				boolean requestLong = requests[1];

				// Calculate long position size
				if (requestLong) {
					MapSymbol bestBullSymbol = getBestBullSymbolBasedOnPositionSizeAdjustedScore();
					if (bestBullSymbol != null) {
						float cash = QueryManager.getTradingAccountCash();
						int numShares = PositionSizing.getPositionSize(bestBullSymbol.getSymbol(), bestBullSymbol.getPrice());
						float commission = Commission.getIBEstimatedCommission(numShares, bestBullSymbol.getPrice());
						float tradeCost = (numShares * bestBullSymbol.getPrice()) + commission;
						
						if (cash > tradeCost && tradeCost >= PositionSizing.MIN_POSITION_VALUE) {
							// Send buy long signal
							System.out.println("Opening LONG position on " + bestBullSymbol.getSymbol());
							int tradeID = QueryManager.makeTrade("long", bestBullSymbol.getSymbol(), bestBullSymbol.getPrice(), numShares, commission);
							QueryManager.updateTradingAccountCash(cash - tradeCost);
							QueryManager.saveMapCell(tradeID, bestBullSymbol.getParentMapCell());
						}
					}
				}
				
				// Calculate short position size
				if (requestShort) {
					MapSymbol bestBearSymbol = getBestBearSymbolBasedOnPositionSizeAdjustedScore();
					if (bestBearSymbol != null) {
						float cash = QueryManager.getTradingAccountCash();
						int numShares = PositionSizing.getPositionSize(bestBearSymbol.getSymbol(), bestBearSymbol.getPrice());
						float commission = Commission.getIBEstimatedCommission(numShares, bestBearSymbol.getPrice());
						float tradeCost = (numShares * bestBearSymbol.getPrice()) + commission;
						
						if (cash > tradeCost && tradeCost >= PositionSizing.MIN_POSITION_VALUE) {
							// Send sell short signal
							System.out.println("Opening SHORT position on " + bestBearSymbol.getSymbol());
							int tradeID = QueryManager.makeTrade("short", bestBearSymbol.getSymbol(), bestBearSymbol.getPrice(), numShares, commission);
							QueryManager.updateTradingAccountCash(cash - tradeCost);
							QueryManager.saveMapCell(tradeID, bestBearSymbol.getParentMapCell());
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Monitors existing open trades that might need to be closed
	 */
	private void monitorClose() {
		try {
			ArrayList<HashMap<String, Object>> openPositions = QueryManager.getOpenPositions();
			for (HashMap<String, Object> openPosition:openPositions) {
				String type = openPosition.get("type").toString();
				String symbol = openPosition.get("symbol").toString();
				String duration = openPosition.get("duration").toString();
				int shares = (int)openPosition.get("shares");
				String sell = openPosition.get("sell").toString();
				String sellop = openPosition.get("sellop").toString();
				String stop = openPosition.get("stop").toString();
				float stopvalue = (float)openPosition.get("stopvalue");
				float entryPrice = (float)openPosition.get("entryprice");
				float commission = (float)openPosition.get("commission");
				
				// Calculate the metrics needed to check this position's sell exit.
				ArrayList<String> sellMetrics = new ArrayList<String>();
				sellMetrics.add(sell);
				ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
				barKeys.add(new BarKey(symbol, duration));
				QueryManager.deleteMostRecentTradingDayFromMetricTables(barKeys, sellMetrics);
				MetricsUpdater.calculateMetrics();
				
				// See if this position's exit (sell or stop) criteria has been met
				HashMap<String, Object> answers = QueryManager.doICloseThisPosition(symbol, sell, sellop, stop, stopvalue);
				boolean sellAnswer = (boolean)answers.get("sell");
				boolean stopAnswer = (boolean)answers.get("stop");
				float adjclose = (float)answers.get("adjclose");

				if (sellAnswer || stopAnswer) {	
					// Calculate some final values for this trade
					float addedCommission = Commission.getIBEstimatedCommission(shares, adjclose);
					float totalCommission = commission + addedCommission;
					float changePerShare = adjclose - entryPrice;
					float revenue = (adjclose * shares) - addedCommission;
					float grossProfit = changePerShare * shares;
					if (type.equals("short"))
						grossProfit = -grossProfit;
					float netProfit = grossProfit - totalCommission;
					String reason = "stop";
					if (sellAnswer) reason = "sell";
					
					System.out.println("Exiting position on " + symbol);
					// Close the position
					QueryManager.closePosition(symbol, reason, adjclose, totalCommission, netProfit, grossProfit);
					// Add/Subtract money to/from account
					float accountValuePreClose = QueryManager.getTradingAccountCash();
					QueryManager.updateTradingAccountCash(accountValuePreClose + revenue);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean[] getDesiredTradeTypes() {
		boolean[] results = new boolean[2];
		try {
			float valueOfLongOpenTrades = QueryManager.getCurrentValueOfLongOpenTrades();
			float valueOfShortOpenTrades = QueryManager.getCurrentValueOfShortOpenTrades();
			boolean requestLong = false;
			boolean requestShort = false;
			if (valueOfLongOpenTrades - valueOfShortOpenTrades > 2000) {
				requestShort = true;
			}
			else if (valueOfShortOpenTrades - valueOfLongOpenTrades > 2000) {
				requestLong = true;
			}
			else {
				requestShort = true;
				requestLong = true;
			}
			
			results[0] = requestShort;
			results[1] = requestLong;
			
		}
		catch (Exception e) {
			e.printStackTrace();
			results[0] = false;
			results[1] = false;
		}
		return results;
	}
	
	private MapSymbol getBestBullSymbolBasedOnPositionSizeAdjustedScore() {
		MapSymbol bestSymbol = null;
		try {
			ArrayList<String> positionSymbols = QueryManager.getTradingPositionSymbols();
			float bestScore = 0f;
			for (MapSymbol hpSymbol:mss.getHighPriorityMapSymbols()) {
				// TODO: make sure this hpSymbol was last updated just now
				if (!positionSymbols.contains(hpSymbol.getSymbol())) {
					int numShares = PositionSizing.getPositionSizeIgnoreCash(hpSymbol.getSymbol(), hpSymbol.getPrice());
					float positionValue = numShares * hpSymbol.getPrice();
					float estimatedOneWayCommission = Commission.getIBEstimatedCommission(numShares, hpSymbol.getPrice());
					float estimatedTwoWayCommission = estimatedOneWayCommission * 2;
					positionValue = positionValue - estimatedTwoWayCommission;
					float thisScore = hpSymbol.getCellBullScore() * positionValue;
					if (thisScore > bestScore) {
						bestScore = thisScore;
						bestSymbol = hpSymbol;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bestSymbol;
	}
	
	private MapSymbol getBestBearSymbolBasedOnPositionSizeAdjustedScore() {
		MapSymbol bestSymbol = null;
		try {
			ArrayList<String> positionSymbols = QueryManager.getTradingPositionSymbols();
			float bestScore = 0f;
			for (MapSymbol hpSymbol:mss.getHighPriorityMapSymbols()) {
				// TODO: make sure this hpSymbol was last updated just now
				if (!positionSymbols.contains(hpSymbol.getSymbol())) {
					int numShares = PositionSizing.getPositionSizeIgnoreCash(hpSymbol.getSymbol(), hpSymbol.getPrice());
					float positionValue = numShares * hpSymbol.getPrice();
					float estimatedOneWayCommission = Commission.getIBEstimatedCommission(numShares, hpSymbol.getPrice());
					float estimatedTwoWayCommission = estimatedOneWayCommission * 2;
					positionValue = positionValue - estimatedTwoWayCommission;
					float thisScore = hpSymbol.getCellBearScore() * positionValue;
					if (thisScore < bestScore) {
						bestScore = thisScore;
						bestSymbol = hpSymbol;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bestSymbol;
	}
	
	private MapSymbol getBestBullSymbolBasedOnScore() {
		MapSymbol bestSymbol = null;
		try {
			ArrayList<String> positionSymbols = QueryManager.getTradingPositionSymbols();
			float bestScore = 0f;
			for (MapSymbol hpSymbol:mss.getHighPriorityMapSymbols()) {
				if (!positionSymbols.contains(hpSymbol.getSymbol())) {
					float thisScore = hpSymbol.getCellBullScore();
					if (thisScore > bestScore) {
						bestScore = thisScore;
						bestSymbol = hpSymbol;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bestSymbol;
	}
	
	private MapSymbol getBestBearSymbolBasedOnScore() {
		MapSymbol bestSymbol = null;
		try {
			ArrayList<String> positionSymbols = QueryManager.getTradingPositionSymbols();
			float bestScore = 0f;
			for (MapSymbol hpSymbol:mss.getHighPriorityMapSymbols()) {
				if (!positionSymbols.contains(hpSymbol.getSymbol())) {
					float thisScore = hpSymbol.getCellBearScore();
					if (thisScore < bestScore) {
						bestScore = thisScore;
						bestSymbol = hpSymbol;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bestSymbol;
	}
	
	public static void getRealtimeQuotesForEverything() {
		try {
			ArrayList<BarKey> barKeys = QueryManager.getUniqueBarKeys();
			MetricSingleton.getInstance().init(barKeys, Constants.METRICS);
			
			// Delete most recent trading day for the symbols we're tracking during this loop
			for (BarKey bk : barKeys) {
				QueryManager.deleteMostRecentBar(bk.symbol, bk.duration.toString());
				QueryManager.deleteMostRecentMetrics(bk.symbol, bk.duration.toString(), Constants.METRICS);
			}

			// Create URL strings
			ArrayList<String> symbolStrings = RealtimeTrackerThread.getSymbolStrings(barKeys);
			
			// Connect to Yahoo Finance & Update basicr with today's current info
			RealtimeTrackerThread.getUpdatedYahooQuotesAndSaveToDB(symbolStrings);
			
			// Recalculate the metrics for today
			MetricsUpdater.calculateMetrics();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}