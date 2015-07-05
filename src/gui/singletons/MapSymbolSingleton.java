package gui.singletons;

import gui.MapCell;
import gui.MapCellPanel;
import gui.MapSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MapSymbolSingleton {

	private ArrayList<MapSymbol> mapSymbols = new ArrayList<MapSymbol>();
	private ArrayList<MapSymbol> highPriorityMapSymbols = new ArrayList<MapSymbol>();
	
	private static MapSymbolSingleton instance = null;
	
	protected MapSymbolSingleton() {
	}
	
	public static MapSymbolSingleton getInstance() {
		if (instance == null) {
			instance = new MapSymbolSingleton();
		}
		return instance;
	}
	
	public void prioritizeMapSymbols(MapCellPanel mcp) {
		try {
			synchronized(mcp) {
				this.highPriorityMapSymbols.clear();
				for (MapSymbol ms:mapSymbols) {
					float xMetricValue = ms.getXMetricValue();
					float yMetricValue = ms.getYMetricValue();
					
					ArrayList<MapCell> mapCellsThisSymbolFallsIn = new ArrayList<MapCell>(); // This is a list because a symbol could be on the border.
		
					// Check each map cell to see if this symbol falls in it
					ArrayList<MapCell> workingMapCells = new ArrayList<MapCell>(); // Working copy to avoid concurrent modification
					workingMapCells.addAll(MapCellSingleton.getInstance().getMapCellsSmoothed());
					
					for (MapCell mc:workingMapCells) {
						HashMap<String, Float> mvh = mc.getMetricValueHash();
						float cellXMin = mvh.get("Map X Metric Min");
						float cellXMax = mvh.get("Map X Metric Max");
						float cellYMin = mvh.get("Map Y Metric Min");
						float cellYMax = mvh.get("Map Y Metric Max");
	
						if (xMetricValue >= cellXMin && xMetricValue <= cellXMax &&
								yMetricValue >= cellYMin && yMetricValue <= cellYMax) {
							mapCellsThisSymbolFallsIn.add(mc);
						}
					}
					
					// Average out the 1+ cells that the symbol does fall in
					float bullCellScoreSum = 0f;
					float bearCellScoreSum = 0f;
					Float partOfBestBullGroup = 0f; // null if not, 1 if yes
					Float partOfBestBearGroup = 0f; // null if not, 1 if yes
					for (MapCell mc:mapCellsThisSymbolFallsIn) {
						HashMap<String, Float> mvh = mc.getMetricValueHash();
						Float bullCellScore = mvh.get("good");
						if (bullCellScore != null)
							bullCellScoreSum += bullCellScore;
						Float bearCellScore = mvh.get("bad");
						if (bearCellScore != null)
							bearCellScoreSum += bearCellScore;
						partOfBestBullGroup = mvh.get("Part of best bull group");
						partOfBestBearGroup = mvh.get("Part of best bear group");
					}
					
					float bullCellScore = 0;
					if (mapCellsThisSymbolFallsIn.size() > 0) {
						bullCellScore = bullCellScoreSum / (float)mapCellsThisSymbolFallsIn.size();
					}
					float bearCellScore = 0;
					if (mapCellsThisSymbolFallsIn.size() > 0) {
						bearCellScore = bearCellScoreSum / (float)mapCellsThisSymbolFallsIn.size();
					}
					
					ms.setCellBullScore(bullCellScore);
					ms.setCellBearScore(bearCellScore);
					if (partOfBestBullGroup != null && partOfBestBullGroup > .1) {
						ms.setPartOfBestBullGroup(true);
						ms.setGroupBullishScore(mcp.getMapBullishScore());
					}
					if (partOfBestBearGroup != null && partOfBestBearGroup > .1) {
						ms.setPartOfBestBearGroup(true);
						ms.setGroupBearishScore(mcp.getMapBearishScore());
					}
				}
	
				Collections.sort(mapSymbols, Collections.reverseOrder());
				for (MapSymbol ms:mapSymbols) {
					if (ms.isPartOfBestBullGroup()) {
						this.highPriorityMapSymbols.add(ms);
					}
					if (ms.isPartOfBestBearGroup()) {
						this.highPriorityMapSymbols.add(ms);
					}
				}
			} // End sync
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<MapSymbol> getMapSymbols() {
		return mapSymbols;
	}

	public void setMapSymbols(ArrayList<MapSymbol> mapSymbols) {
		for (MapSymbol newms:mapSymbols) {
			for (MapSymbol oldms:this.mapSymbols) {
				if (newms.getSymbol().equals(oldms.getSymbol())) {
					newms.setLastUpdated(oldms.getLastUpdated());
				}
			}
		}
		this.mapSymbols = mapSymbols;
	}

	public ArrayList<MapSymbol> getHighPriorityMapSymbols() {
		return highPriorityMapSymbols;
	}

	public void setHighPriorityMapSymbols(ArrayList<MapSymbol> highPriorityMapSymbols) {
		this.highPriorityMapSymbols = highPriorityMapSymbols;
	}
}