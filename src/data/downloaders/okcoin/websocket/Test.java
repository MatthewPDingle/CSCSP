package data.downloaders.okcoin.websocket;

public class Test {

	public static void main(String[] args) {

		OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
		okss.setRunning(true);
		okss.addChannel("ok_btccny_ticker");
		
//
//		OKCoinWebSocketService service = new OKCoinBusinessWebSocketServiceImpl();
//		OKCoinWebSocketClient client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
//	
//		client.start();
//		client.addChannel("ok_btccny_ticker");

	}
}
