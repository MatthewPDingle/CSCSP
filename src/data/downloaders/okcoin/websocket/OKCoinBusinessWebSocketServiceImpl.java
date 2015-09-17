package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import data.downloaders.okcoin.OKCoinConstants;

public class OKCoinBusinessWebSocketServiceImpl implements OKCoinWebSocketService {

	@Override
	public void onReceive(String msg) {
		try {
			Gson gson = new Gson();
			Object messageObject = gson.fromJson(msg, Object.class);
			if (messageObject instanceof ArrayList<?>) {
				ArrayList<LinkedTreeMap<String, Object>> messageList = gson.fromJson(msg, ArrayList.class);

				OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
				
				for (LinkedTreeMap<String, Object> message : messageList) {
					String channel = message.get("channel").toString();
					String symbol = OKCoinConstants.WEBSOCKET_SYMBOL_TO_TICK_SYMBOL_HASH.get(channel);
					LinkedTreeMap<String, String> data = (LinkedTreeMap<String, String>)message.get("data");
					HashMap dataHash = new HashMap<String, String>();
					
					dataHash.putAll(data);
					okss.putSymbolDataHash(symbol, dataHash);
				}
			}
			else {
				// {'event':'pong'} probably
				
			}
			System.out.println(msg);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}