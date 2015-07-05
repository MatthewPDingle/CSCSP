package trading;

import dbio.QueryManager;

public class AccountHistoryTracker {

	public static void main(String[] args) {
		
		TradeMonitor.getRealtimeQuotesForEverything();
		float accountValue = QueryManager.getTradingAccountValue();
		QueryManager.saveAccountHistoryValue(accountValue);
	}
}