package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.HashMap;

import data.Bar;
import dbio.QueryManager;

public class OKCoinWebSocketSingleton {

	private static OKCoinWebSocketSingleton instance = null;
	
	private OKCoinWebSocketThread okThread;
	private HashMap<String, HashMap<String, String>> symbolTickerDataHash; // Last Tick info - price, bid, ask, timestamp
	private ArrayList<Bar> latestBars;
	private boolean disconnected = false;
	
	protected OKCoinWebSocketSingleton() {
		okThread = new OKCoinWebSocketThread();
		symbolTickerDataHash = new HashMap<String, HashMap<String, String>>();
		latestBars = new ArrayList<Bar>();
	}
	
	public static OKCoinWebSocketSingleton getInstance() {
		if (instance == null) {
			instance = new OKCoinWebSocketSingleton();
		}
		return instance;
	}
	
	public void setRunning(boolean running) {
		try {
			if (running) {
				if (!okThread.isRunning()) {
					okThread = new OKCoinWebSocketThread();
					okThread.setRunning(true);
					okThread.start();
				}
			}
			else {
				okThread.removeAllChannels();
				okThread.setRunning(false);
				okThread.join();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addChannel(String channel) {
		okThread.addChannel(channel);
	}
	
	public void removeChannel(String channel) {
		okThread.removeChannel(channel);
	}
	
	public HashMap<String, HashMap<String, String>> getSymbolDataHash() {
		return symbolTickerDataHash;
	}

	public void putSymbolTickerDataHash(String symbol, HashMap<String, String> tickerDataHash) {
		this.symbolTickerDataHash.put(symbol, tickerDataHash);
	}

	public synchronized boolean insertLatestBarsIntoDB() {
		for (Bar bar : latestBars) {
			QueryManager.insertOrUpdateIntoBar(bar);
		}
		if (latestBars.size() > 0) {
			latestBars.clear();
			return true;
		}
		return false;
	}

	public synchronized void setLatestBars(ArrayList<Bar> latestBars) {
		this.latestBars = latestBars;
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}
}