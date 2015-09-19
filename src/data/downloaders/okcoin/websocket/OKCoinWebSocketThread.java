package data.downloaders.okcoin.websocket;

import java.util.HashMap;
import java.util.Map.Entry;

import data.downloaders.okcoin.OKCoinConstants;

public class OKCoinWebSocketThread extends Thread {

	private boolean running = false;
	private OKCoinWebSocketService service = null;
	private OKCoinWebSocketClient client = null;
	private HashMap<String, Boolean> channels = new HashMap<String, Boolean>();
	
	public OKCoinWebSocketThread() {
		service = new OKCoinWebSocketListener();
		client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public synchronized void addChannel(String channel) {
		channels.put(channel, false);
	}
	
	public synchronized void removeChannel(String channel) {
		client.removeChannel(channel);
		channels.remove(channel);
	}
	
	public synchronized void removeAllChannels() {
		client.removeAllChannels();
	}

	@Override
	public void run () {
		if (running) {
			boolean success = client.start();
			if (success) {
				OKCoinWebSocketSingleton.getInstance().setDisconnected(false);
			}
		}
		while (running) {
			try {
				if (OKCoinWebSocketSingleton.getInstance().isDisconnected()) {
					System.out.println("Reconnecting");
					service = new OKCoinWebSocketListener();
					client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
					boolean success = client.start();
					if (success) {
						OKCoinWebSocketSingleton.getInstance().setDisconnected(false);
					}
					for (Entry<String, Boolean> entry : channels.entrySet()) {
						client.addChannel(entry.getKey());
						entry.setValue(true);
					}
				}
				
				for (Entry<String, Boolean> entry : channels.entrySet()) {
					if (!entry.getValue()) {
						client.addChannel(entry.getKey());
						entry.setValue(true);
					}
				}
			
				Thread.sleep(5000);
				System.out.println("sentPing");
				client.sentPing();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
}