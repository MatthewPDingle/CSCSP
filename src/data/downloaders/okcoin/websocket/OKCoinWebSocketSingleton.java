package data.downloaders.okcoin.websocket;

import java.util.HashMap;

public class OKCoinWebSocketSingleton {

	private static OKCoinWebSocketSingleton instance = null;
	
	private OKCoinWebSocketThread okThread;
	private HashMap<String, HashMap<String, String>> symbolDataHash;
	private boolean disconnected = false;
	
	protected OKCoinWebSocketSingleton() {
		okThread = new OKCoinWebSocketThread();
		symbolDataHash = new HashMap<String, HashMap<String, String>>();
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
		return symbolDataHash;
	}

	public void putSymbolDataHash(String symbol, HashMap<String, String> symbolDataHash) {
		this.symbolDataHash.put(symbol, symbolDataHash);
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}
}