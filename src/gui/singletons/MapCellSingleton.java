package gui.singletons;

import gui.MapCell;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import constants.Constants;

public class MapCellSingleton {

	private static MapCellSingleton instance = null;
	private transient Object lock = new Object();

	private ArrayList<MapCell> mapCells = new ArrayList<MapCell>();
	private ArrayList<MapCell> mapCellsSmoothed = new ArrayList<MapCell>();
	
	protected MapCellSingleton() {
	}
	
	public static MapCellSingleton getInstance() {
		if (instance == null) {
			instance = new MapCellSingleton();
		}
		return instance;
	}
	
	public void addSmoothedMapCell(MapCell mc) {
		synchronized(lock) {
			mapCellsSmoothed.add(mc);
		}
	}
	
	public void addMapCell(MapCell mc) {
		synchronized(lock) {
			if (!mapCells.contains(mc)) {
				mapCells.add(mc);
				fillInAllSmoothedMapCells();
			}
		}	
	}
	
	public ArrayList<MapCell> getRequestedMapCellList(boolean smoothed) {
		synchronized(lock) {
			if (smoothed)
				return mapCellsSmoothed;
			else
				return mapCells;
		}
	}
	
	public MapCell[][] getGridVersion(ArrayList<MapCell> mapCells) {
		synchronized(lock) {
			MapCell[][] mapCellGrid = new MapCell[ParameterSingleton.getInstance().getxRes()][ParameterSingleton.getInstance().getyRes()]; // 0,0 in upper left
			for (MapCell mapCell:mapCells) {
				int xArrayPos = Math.round(mapCell.getMetricValueHash().get("X Array Pos"));
				int yArrayPos = Math.round(mapCell.getMetricValueHash().get("Y Array Pos"));
				mapCellGrid[xArrayPos][yArrayPos] = mapCell;
			}
			return mapCellGrid;
		}
	}
	
	public MapCell getSmoothedMapCellAt(float xMetricValue, float yMetricValue) {
		try {
			if (mapCellsSmoothed.size() == 0) {
				fillInAllSmoothedMapCells();
			}
			for (MapCell mapCell:mapCellsSmoothed) {
				HashMap<String, Float> mvh = mapCell.getMetricValueHash();
				float xMin = mvh.get("Map X Metric Min");
				float xMax = mvh.get("Map X Metric Max");
				float yMin = mvh.get("Map Y Metric Min");
				float yMax = mvh.get("Map Y Metric Max");
				if (xMetricValue >= xMin && xMetricValue < xMax &&
						yMetricValue >= yMin && yMetricValue < yMax) {
					return mapCell;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void fillInAllSmoothedMapCells() {
    	try {    		
    		synchronized(lock) {
	    		// Put the regular cells in a grid so I can calculate the smoothed cells
				MapCell[][] mapCellGrid = getGridVersion(mapCells);
	    		
	    		// If the Map Cell Panel was loaded from a save, keep the "good" and "bad" variables
	    		HashMap<Point, Float> goodPointValueHash = new HashMap<Point, Float>();
	    		HashMap<Point, Float> badPointValueHash = new HashMap<Point, Float>();
	    		HashMap<Point, Float> bestBullPointValueHash = new HashMap<Point, Float>();
	    		HashMap<Point, Float> bestBearPointValueHash = new HashMap<Point, Float>();
	    		
	    		for (MapCell mc:mapCellsSmoothed) {
	    			if (mc != null) {
		    			int x = mc.getMetricValueHash().get("X Array Pos").intValue();
		    			int y = mc.getMetricValueHash().get("Y Array Pos").intValue();
		    			Float g = mc.getMetricValueHash().get("good");
		    			Float b = mc.getMetricValueHash().get("bad");
		    			Float bestBullGroup = mc.getMetricValueHash().get("Part of best bull group");
		    			Float bestBearGroup = mc.getMetricValueHash().get("Part of best bear group");
		    			
		    			if (g != null) {
		    				Point p = new Point(x, y);
		    				goodPointValueHash.put(p,  g);
		    			}
		    			if (b != null) {
		    				Point p = new Point(x, y);
		    				badPointValueHash.put(p,  b);
		    			}
		    			if (bestBullGroup != null) {
		    				Point p = new Point(x, y);
		    				bestBullPointValueHash.put(p, bestBullGroup);
		    			}
		    			if (bestBearGroup != null) {
		    				Point p = new Point(x, y);
		    				bestBearPointValueHash.put(p, bestBearGroup);
		    			}
	    			}
	    		}
	
	    		setMapCellsSmoothed(new ArrayList<MapCell>());
		    	for (int x = 0; x < ParameterSingleton.getInstance().getxRes(); x++) {
					for (int y = 0; y < ParameterSingleton.getInstance().getyRes(); y++) {
						if (mapCellGrid[x][y] != null) {
							MapCell smoothedCell = getSmoothedMapCell(x, y, mapCellGrid);
							
							Iterator i = goodPointValueHash.entrySet().iterator();
							while (i.hasNext()) {
								Map.Entry pairs = (Map.Entry)i.next();
								Point p = (Point)pairs.getKey();
								Float g = (Float)pairs.getValue();
								if (p.x == x && p.y == y) {
									smoothedCell.addToMetricValueHash("good", g);
								}
							}
							
							Iterator i2 = badPointValueHash.entrySet().iterator();
							while (i2.hasNext()) {
								Map.Entry pairs = (Map.Entry)i2.next();
								Point p = (Point)pairs.getKey();
								Float b = (Float)pairs.getValue();
								if (p.x == x && p.y == y) {
									smoothedCell.addToMetricValueHash("bad", b);
								}
							}
							
							Iterator i3 = bestBullPointValueHash.entrySet().iterator();
							while (i3.hasNext()) {
								Map.Entry pairs = (Map.Entry)i3.next();
								Point p = (Point)pairs.getKey();
								Float b = (Float)pairs.getValue();
								if (p.x == x && p.y == y) {
									smoothedCell.addToMetricValueHash("Part of best bull group", b);
								}
							}
							
							Iterator i4 = bestBearPointValueHash.entrySet().iterator();
							while (i4.hasNext()) {
								Map.Entry pairs = (Map.Entry)i4.next();
								Point p = (Point)pairs.getKey();
								Float b = (Float)pairs.getValue();
								if (p.x == x && p.y == y) {
									smoothedCell.addToMetricValueHash("Part of best bear group", b);
								}
							}
	
							addSmoothedMapCell(smoothedCell);
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
     * Returns a MapCell that is a weighted representation of the center cell (x,y in the mapCellGrid)
     * and the neighboring cells.  Cardinals are weighted .5, Diagonals are weighted .25
     * 
     * @param x
     * @param y
     * @param mapCellGrid
     * @param mapColorOption
     * @return
     */
    private MapCell getSmoothedMapCell(int x, int y, MapCell[][] mapCellGrid) {
    	try {
    		synchronized(lock) {
	    		MapCell smoothedCell = new MapCell();
	    		
	    		HashMap<String, Float> centerMVH = new HashMap<String, Float>();
	    		HashMap<String, Float> tempMVH = new HashMap<String, Float>();
				centerMVH.putAll(mapCellGrid[x][y].getMetricValueHash());
	    		
				float adjacentNumResultsSum = 0f;
				float numAllPositions = 0f;
				float numMetricPositions = 0f;
				float numStopPositions = 0f;
				float numEndPositions = 0f;
				
	    		for (String mapColorOption:Constants.MAP_COLOR_OPTIONS) {
		    		adjacentNumResultsSum = 0f;
					float adjacentNumResultsTimesSum = 0f;
					float adjacentTimesSum = 0f;
					
					// Check what the color option type is here so I can do calculations accordingly
					String defaultNumPositionsConstant = Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS;
					if (mapColorOption.contains("All")) {
						defaultNumPositionsConstant = Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS;
					}
					else if (mapColorOption.contains("Metric")) {
						defaultNumPositionsConstant = Constants.MAP_COLOR_OPTION_METRIC_NUM_POSITIONS;
					}
					else if (mapColorOption.contains("Stop")) {
						defaultNumPositionsConstant = Constants.MAP_COLOR_OPTION_STOP_NUM_POSITIONS;
					}
					else if (mapColorOption.contains("End")) {
						defaultNumPositionsConstant = Constants.MAP_COLOR_OPTION_END_NUM_POSITIONS;
					}
					
					// Try to get the bordering cells.  Ignore errors
					MapCell leftCell = null;
					HashMap<String, Float> leftMVH = null;
					
					MapCell upperLeftCell = null;
					HashMap<String, Float> upperLeftMVH = null;
					
					MapCell upperCell = null;
					HashMap<String, Float> upperMVH = null;
					
					MapCell upperRightCell = null;
					HashMap<String, Float> upperRightMVH = null;
					
					MapCell rightCell = null;
					HashMap<String, Float> rightMVH = null;
					
					MapCell lowerRightCell = null;
					HashMap<String, Float> lowerRightMVH = null;
					
					MapCell lowerCell = null;
					HashMap<String, Float> lowerMVH = null;
					
					MapCell lowerLeftCell = null;
					HashMap<String, Float> lowerLeftMVH = null;
	
					try {
						leftCell = mapCellGrid[x-1][y];
						leftMVH = leftCell.getMetricValueHash();
						
						if (leftMVH != null && !leftMVH.get(mapColorOption).isNaN() && !leftMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += leftMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.5f * (leftMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.5 * (leftMVH.get(mapColorOption) * leftMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						upperLeftCell = mapCellGrid[x-1][y-1];
						upperLeftMVH = upperLeftCell.getMetricValueHash();
						if (upperLeftMVH != null && !upperLeftMVH.get(mapColorOption).isNaN() && !upperLeftMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += upperLeftMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.25f * (upperLeftMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.25 * (upperLeftMVH.get(mapColorOption) * upperLeftMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						upperCell = mapCellGrid[x][y-1];
						upperMVH = upperCell.getMetricValueHash();
						if (upperMVH != null && !upperMVH.get(mapColorOption).isNaN() && !upperMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += upperMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.5f * (upperMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.5 * (upperMVH.get(mapColorOption) * upperMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						upperRightCell = mapCellGrid[x+1][y-1];
						upperRightMVH = upperRightCell.getMetricValueHash();
						if (upperRightMVH != null && !upperRightMVH.get(mapColorOption).isNaN() && !upperRightMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += upperRightMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.25f * (upperRightMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.25 * (upperRightMVH.get(mapColorOption) * upperRightMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						rightCell = mapCellGrid[x+1][y];
						rightMVH = rightCell.getMetricValueHash();
						if (rightMVH != null && !rightMVH.get(mapColorOption).isNaN() && !rightMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += rightMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.5f * (rightMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.5 * (rightMVH.get(mapColorOption) * rightMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						lowerRightCell = mapCellGrid[x+1][y+1];
						lowerRightMVH = lowerRightCell.getMetricValueHash();
						if (lowerRightMVH != null && !lowerRightMVH.get(mapColorOption).isNaN() && !lowerRightMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += lowerRightMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.25f * (lowerRightMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.25 * (lowerRightMVH.get(mapColorOption) * lowerRightMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						lowerCell = mapCellGrid[x][y+1];
						lowerMVH = lowerCell.getMetricValueHash();
						if (lowerMVH != null && !lowerMVH.get(mapColorOption).isNaN() && !lowerMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += lowerMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.5f * (lowerMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.5 * (lowerMVH.get(mapColorOption) * lowerMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
					
					try {
						lowerLeftCell = mapCellGrid[x-1][y+1];
						lowerLeftMVH = lowerLeftCell.getMetricValueHash();
						if (lowerLeftMVH != null && !lowerLeftMVH.get(mapColorOption).isNaN() && !lowerLeftMVH.get(defaultNumPositionsConstant).isNaN()) {
							adjacentNumResultsSum += lowerLeftMVH.get(defaultNumPositionsConstant);
							adjacentNumResultsTimesSum += (.25f * (lowerLeftMVH.get(defaultNumPositionsConstant)));
							adjacentTimesSum += (.25 * (lowerLeftMVH.get(mapColorOption) * lowerLeftMVH.get(defaultNumPositionsConstant)));
						}
					} catch (Exception e) {}
	
					if (centerMVH != null && !centerMVH.get(mapColorOption).isNaN() && !centerMVH.get(defaultNumPositionsConstant).isNaN()) {
						adjacentNumResultsSum += centerMVH.get(defaultNumPositionsConstant);
						adjacentNumResultsTimesSum += (centerMVH.get(defaultNumPositionsConstant));
						adjacentTimesSum += ((centerMVH.get(mapColorOption) * centerMVH.get(defaultNumPositionsConstant)));	
					}
	
					float colorMetric = 0f;
					if (adjacentNumResultsTimesSum != 0) {
						colorMetric = adjacentTimesSum / adjacentNumResultsTimesSum;
					}
					
					// Replace the old values;
					if (mapColorOption.contains("# Positions")) {
						tempMVH.put(defaultNumPositionsConstant, adjacentNumResultsSum);
						smoothedCell.setMetricValueHash(centerMVH);
						
						// % Positions have to be calculated at the end
						if (mapColorOption.equals(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS))
							numAllPositions = adjacentNumResultsSum;
						if (mapColorOption.equals(Constants.MAP_COLOR_OPTION_METRIC_NUM_POSITIONS))
							numMetricPositions = adjacentNumResultsSum;
						if (mapColorOption.equals(Constants.MAP_COLOR_OPTION_STOP_NUM_POSITIONS))
							numStopPositions = adjacentNumResultsSum;
						if (mapColorOption.equals(Constants.MAP_COLOR_OPTION_END_NUM_POSITIONS))
							numEndPositions = adjacentNumResultsSum;
					}
					else if (!mapColorOption.contains("% Positions")) {
						tempMVH.put(mapColorOption, colorMetric);
						smoothedCell.setMetricValueHash(centerMVH);
	    			}
	    		} // End mapColorOption loop
	    		centerMVH.putAll(tempMVH);
	 
	    		// Calculate % positions and add them to the map
	    		centerMVH.put(Constants.MAP_COLOR_OPTION_METRIC_PERCENT_POSITIONS, numMetricPositions / numAllPositions * 100f);
	    		centerMVH.put(Constants.MAP_COLOR_OPTION_STOP_PERCENT_POSITIONS, numStopPositions / numAllPositions * 100f);
	    		centerMVH.put(Constants.MAP_COLOR_OPTION_END_PERCENT_POSITIONS, numEndPositions / numAllPositions * 100f);
	    		smoothedCell.setMetricValueHash(centerMVH);
	    		
	    		return smoothedCell;
    		}
		} 
    	catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}	
    }

	public ArrayList<MapCell> getMapCells() {
		synchronized(lock) {
			return mapCells;
		}
	}

	public void setMapCells(ArrayList<MapCell> mapCells) {
		synchronized(lock) {
			this.mapCells = mapCells;
		}
	}

	public ArrayList<MapCell> getMapCellsSmoothed() {
		synchronized(lock) {
			return mapCellsSmoothed;
		}
	}

	public void setMapCellsSmoothed(ArrayList<MapCell> mapCellsSmoothed) {
		synchronized(lock) {
			this.mapCellsSmoothed = mapCellsSmoothed;
		}
	}
	
	public void setLock(Object lock) {
		this.lock = lock;
	}
}