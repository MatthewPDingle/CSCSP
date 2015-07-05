package trading;

import gui.GUI;
import gui.MapCellPanel;
import gui.singletons.MapCellSingleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import dbio.QueryManager;

public class ActiveMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		loadMap();
	}

	private static boolean loadMap() {
		try {
			// Decide whether to open a bullish map or a bearish map
			File activeFolder = new File("maps/active/bull");
			float valueOfLongOpenTrades = QueryManager.getCurrentValueOfLongOpenTrades();
			float valueOfShortOpenTrades = QueryManager.getCurrentValueOfShortOpenTrades();
			if (valueOfLongOpenTrades > valueOfShortOpenTrades) {
				activeFolder = new File("maps/active/bear");
			}
			
			File[] mapFiles = activeFolder.listFiles();
			if (mapFiles.length > 0) {
				FileInputStream fis = new FileInputStream(mapFiles[0]);
				ObjectInputStream ois = new ObjectInputStream(fis);
				Object o = ois.readObject();
				if (o instanceof MapCellPanel) {
					MapCellPanel mcp = (MapCellPanel)o;
					// Do the hack where I put the MapCells back into the singleton because the singleton isn't serialized.
					MapCellSingleton mcs = MapCellSingleton.getInstance();
					mcs.setMapCells(mcp.getMapCells());
					mcs.setMapCellsSmoothed(mcp.getMapCellsSmoothed());
					mcs.setLock(new Object());
					
					int mapBullishScore = mcp.getMapBullishScore();
					int mapBearishScore = mcp.getMapBearishScore();

					GUI.runBackdoorTradeMonitor(mapFiles[0].getName(), mapBullishScore, mapBearishScore, mcp);
				}
				ois.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}