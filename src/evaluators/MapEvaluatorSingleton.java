package evaluators;

import gui.MapCell;
import gui.MapCellPanel;
import gui.singletons.MapCellSingleton;
import gui.singletons.ParameterSingleton;

import java.util.ArrayList;
import java.util.HashMap;

import constants.Constants;

public class MapEvaluatorSingleton {

	private static int MIN_NUM_POSITIONS = 200;
	private static int MIN_NUM_CELLS_IN_GROUP = 4;
	private static int MAX_PERCENT_END_POSITION_FOR_CELL = 20;
	
	// Bullish Cell Criteria
	private static float MIN_AGMPD = .012f; // .14f; 1D Bar Stocks
	private static float MIN_GMPD = .012f; // .32f; 1D Bar Stocks
	private static float MIN_SHARPE = .25f; /// .16f; 1D Bar Stocks
	public static float MIN_SCORE = .6f; // 10AGMPD + 10GMPD + 1SHARPE for 15M Bar Bitcoin // 1.10f; (3AGMPD + 2SHARPE + 1GMPD) for 1D Bar Stocks
	
	// Bearish Cell Criteria
	private static float MAX_AGMPD = -.012f; // -.15f; 1D Bar Stocks
	private static float MAX_GMPD = -.012f; // -.05f; 1D Bar Stocks
	private static float MAX_SHARPE = -.25f; // -.15f; 1D Bar Stocks
	public static float MAX_SCORE = -.6f; // 10AGMPD + 10GMPD + 1SHARPE for 15M Bar Bitcoin // -.85f; // (3AGMPD + 2SHARPE + 1GMPD) for 1D Bar Stocks
	
	private static MapEvaluatorSingleton instance = null;

	private ArrayList<MapCell> bullishEvaluateList = new ArrayList<MapCell>();
	private ArrayList<MapCell> bearishEvaluateList = new ArrayList<MapCell>();
	private ArrayList<ArrayList<MapCell>> bullishGroupList = new ArrayList<ArrayList<MapCell>>();
	private ArrayList<ArrayList<MapCell>> bearishGroupList = new ArrayList<ArrayList<MapCell>>();
		
	protected MapEvaluatorSingleton() {
	}
	
	public static MapEvaluatorSingleton getInstance() {
		if (instance == null) {
			instance = new MapEvaluatorSingleton();
		}
		return instance;
	}
	
	public synchronized HashMap<String, Float> evaluate(MapCellPanel mcp) {
		HashMap<String, Float> resultHash = new HashMap<String, Float>();
		try {
			bullishEvaluateList.clear();
			bearishEvaluateList.clear();
			
			bullishGroupList.clear();
			bearishGroupList.clear();
			
			MapCellSingleton mcs = MapCellSingleton.getInstance();
			mcs.fillInAllSmoothedMapCells();
			
			// Load the possibly un-arranged mapCells into an arranged grid
			ArrayList<MapCell> mapCells = new ArrayList<MapCell>();
			mapCells.addAll(mcs.getMapCellsSmoothed());
			MapCell[][] mcGrid = mcs.getGridVersion(mapCells);
			
			for (MapCell mc:mapCells) {
				bullishEvaluateList.add(mc);
				bearishEvaluateList.add(mc);
			}
			mapCells.clear();
			
			// Iterate through cells in order
			for (int x = 0; x < ParameterSingleton.getInstance().getxRes(); x++) {
				for (int y = 0; y < ParameterSingleton.getInstance().getyRes(); y++) {
					MapCell mc = mcGrid[x][y];
					
					// Bullish
					if (!isCellBullish(mc)) {
						bullishEvaluateList.remove(mc);
					}
					else {
						if (!isInExistingBullishGroup(mc)) {
							ArrayList<MapCell> newGroup = new ArrayList<MapCell>();
							newGroup.add(mc);
							bullishGroupList.add(newGroup);
							ArrayList<MapCell> adjacentCellList = getAdjacentCellList(x, y, mcs.getMapCellsSmoothed());
							boolean anyNewCells = false;
							for (MapCell adjacentMapCell:adjacentCellList) {
								if (isAdjacentCellSuitableForBullishGroup(adjacentMapCell)) {
									newGroup.add(adjacentMapCell);
									bullishEvaluateList.remove(adjacentMapCell);
									anyNewCells = true;
								}
							}
							while (anyNewCells) {
								anyNewCells = false;
								ArrayList<MapCell> adjacentCellsToAdd = new ArrayList<MapCell>();
								for (MapCell cell:newGroup) {
									HashMap<String, Float> mvh = cell.getMetricValueHash();
									int tx = mvh.get("X Array Pos").intValue();
									int ty = mvh.get("Y Array Pos").intValue();
									ArrayList<MapCell> moreAdjacentCellList = getAdjacentCellList(tx, ty, mcs.getMapCellsSmoothed());
									for (MapCell adjacentCell:moreAdjacentCellList) {
										if (isAdjacentCellSuitableForBullishGroup(adjacentCell)) {
											adjacentCellsToAdd.add(adjacentCell);
											bullishEvaluateList.remove(adjacentCell);
											anyNewCells = true;
										}
									}
								}
								newGroup.addAll(adjacentCellsToAdd);
							}
						}
					}
					// Bearish
					if (!isCellBearish(mc)) {
						bearishEvaluateList.remove(mc);
					}
					else {
						if (!isInExistingBearishGroup(mc)) {
							ArrayList<MapCell> newGroup = new ArrayList<MapCell>();
							newGroup.add(mc);
							bearishGroupList.add(newGroup);
							ArrayList<MapCell> adjacentCellList = getAdjacentCellList(x, y, mcs.getMapCellsSmoothed());
							boolean anyNewCells = false;
							for (MapCell adjacentMapCell:adjacentCellList) {
								if (isAdjacentCellSuitableForBearishGroup(adjacentMapCell)) {
									newGroup.add(adjacentMapCell);
									bearishEvaluateList.remove(adjacentMapCell);
									anyNewCells = true;
								}
							}
							while (anyNewCells) {
								anyNewCells = false;
								ArrayList<MapCell> adjacentCellsToAdd = new ArrayList<MapCell>();
								for (MapCell cell:newGroup) {
									HashMap<String, Float> mvh = cell.getMetricValueHash();
									int tx = mvh.get("X Array Pos").intValue();
									int ty = mvh.get("Y Array Pos").intValue();
									ArrayList<MapCell> moreAdjacentCellList = getAdjacentCellList(tx, ty, mcs.getMapCellsSmoothed());
									for (MapCell adjacentCell:moreAdjacentCellList) {
										if (isAdjacentCellSuitableForBearishGroup(adjacentCell)) {
											adjacentCellsToAdd.add(adjacentCell);
											bearishEvaluateList.remove(adjacentCell);
											anyNewCells = true;
										}
									}
								}
								newGroup.addAll(adjacentCellsToAdd);
							}
						}
					}
					if (mc != null)
						mapCells.add(mc);
				}
			}

			// Set the cells so they know if they're in the best groups or not
			ArrayList<MapCell> bestBullishGroup = getBestBullishGroup();
			ArrayList<MapCell> bestBearishGroup = getBestBearishGroup();
			
			for (MapCell mc:mapCells) {
				Float mcx = mc.getMetricValueHash().get("X Array Pos");
				Float mcy = mc.getMetricValueHash().get("Y Array Pos");
				for (MapCell bullMC:bestBullishGroup) {
					Float bmcx = bullMC.getMetricValueHash().get("X Array Pos");
					Float bmcy = bullMC.getMetricValueHash().get("Y Array Pos");
					if (mcx == bmcx && mcy == bmcy) {
						mc.addToMetricValueHash("Part of best bull group", 1f);
					}
				}
				for (MapCell bearMC:bestBearishGroup) {
					Float bmcx = bearMC.getMetricValueHash().get("X Array Pos");
					Float bmcy = bearMC.getMetricValueHash().get("Y Array Pos");
					if (mcx == bmcx && mcy == bmcy) {
						mc.addToMetricValueHash("Part of best bear group", 1f);
					}
				}
			}
			
			mcs.setMapCellsSmoothed(mapCells);

			resultHash.put("bull", getGroupScore(bestBullishGroup));
			resultHash.put("bear", getGroupScore(bestBearishGroup));
			
			return resultHash;
		}
		catch (Exception e) {
			e.printStackTrace();
			return resultHash;
		}
	}
	
	private ArrayList<MapCell> getBestBullishGroup() {
		ArrayList<MapCell> bestGroup = new ArrayList<MapCell>();
		try {
			float highestGroupScore = 0f;

			for (ArrayList<MapCell> group:bullishGroupList) {
				float totalNumPositions = 0f;
				float totalWSharpe = 0f;
				float totalWAGMPD = 0f;
				float totalWGMPD = 0f;
				for (MapCell mc:group) {
					HashMap<String, Float> mvh = mc.getMetricValueHash();
		
					float numPositions = mvh.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
					float agmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR);
					float gmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR);
					float sharpe = mvh.get(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO);
					
					totalNumPositions += numPositions;
					
					float wsharpe = numPositions * sharpe; 
					float wagmpd = numPositions * agmpd; 
					float wgmpd = numPositions * gmpd;
					
					totalWSharpe += wsharpe;
					totalWAGMPD += wagmpd;
					totalWGMPD += wgmpd;
				}
				
				float waSharpe = totalWSharpe / totalNumPositions * group.size();
				float waAGMPD = totalWAGMPD / totalNumPositions * group.size();
				float waGMPD = totalWGMPD / totalNumPositions * group.size();
				float waScore = (3 * waAGMPD) + (2 * waSharpe) + (1f * waGMPD);
				float groupScore = waScore * totalNumPositions / 1000f;
				
				if (groupScore > highestGroupScore &&  group.size() >= MIN_NUM_CELLS_IN_GROUP) {
					highestGroupScore = groupScore;
					bestGroup = group;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bestGroup;
	}
	
	private ArrayList<MapCell> getBestBearishGroup() {
		ArrayList<MapCell> bestGroup = new ArrayList<MapCell>();
		try {
			float lowestGroupScore = 1000000f;

			for (ArrayList<MapCell> group:bearishGroupList) {
				float totalNumPositions = 0f;
				float totalWSharpe = 0f;
				float totalWAGMPD = 0f;
				float totalWGMPD = 0f;
				for (MapCell mc:group) {
					HashMap<String, Float> mvh = mc.getMetricValueHash();
		
					float numPositions = mvh.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
					float agmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR);
					float gmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR);
					float sharpe = mvh.get(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO);
					
					totalNumPositions += numPositions;
					
					float wsharpe = numPositions * sharpe; 
					float wagmpd = numPositions * agmpd; 
					float wgmpd = numPositions * gmpd;
					
					totalWSharpe += wsharpe;
					totalWAGMPD += wagmpd;
					totalWGMPD += wgmpd;
				}
				
				float waSharpe = totalWSharpe / totalNumPositions * group.size();
				float waAGMPD = totalWAGMPD / totalNumPositions * group.size();
				float waGMPD = totalWGMPD / totalNumPositions * group.size();
				float waScore = (3 * waAGMPD) + (2 * waSharpe) + (1f * waGMPD);
				float groupScore = waScore * totalNumPositions / 1000f;
				
				if (groupScore < lowestGroupScore &&  group.size() >= MIN_NUM_CELLS_IN_GROUP) {
					lowestGroupScore = groupScore;
					bestGroup = group;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bestGroup;
	}
	
	private float getGroupScore(ArrayList<MapCell> bestGroup) {
		try {
			if (bestGroup == null || bestGroup.size() == 0) {
				return 0f;
			}

			float totalWSharpe = 0f;
			float totalWAGMPD = 0f;
			float totalWGMPD = 0f;
			for (MapCell mc:bestGroup) {
				HashMap<String, Float> mvh = mc.getMetricValueHash();

				float agmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR);
				float gmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR);
				float sharpe = mvh.get(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO);
				
				totalWSharpe += sharpe;
				totalWAGMPD += agmpd;
				totalWGMPD += gmpd;
			}
			
			float waScore = (3 * totalWAGMPD) + (2 * totalWSharpe) + (1f * totalWGMPD);
			waScore = waScore / (float)bestGroup.size();
			
			float multiplier = 1 + (bestGroup.size() / 10f);
			waScore *= multiplier;
			
			float groupScore = waScore * 500;
			
			return groupScore;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	private boolean isAdjacentCellSuitableForBullishGroup(MapCell adjacentMapCell) {
		try {
			if (!bullishEvaluateList.contains(adjacentMapCell)) {
				return false;
			}
			if (!isCellBullish(adjacentMapCell)) {
				return false;
			}
			if (isInExistingBullishGroup(adjacentMapCell)) {
				return false;
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean isAdjacentCellSuitableForBearishGroup(MapCell adjacentMapCell) {
		try {
			if (!bearishEvaluateList.contains(adjacentMapCell)) {
				return false;
			}
			if (!isCellBearish(adjacentMapCell)) {
				return false;
			}
			if (isInExistingBearishGroup(adjacentMapCell)) {
				return false;
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private ArrayList<MapCell> getAdjacentCellList(int x, int y, ArrayList<MapCell> mapCells) {
		ArrayList<MapCell> adjacentCellList = new ArrayList<MapCell>();
		try {
			for (MapCell mc:mapCells) {
				HashMap<String, Float> mvh = mc.getMetricValueHash();
				int ex = Math.round(mvh.get("X Array Pos"));
				int ey = Math.round(mvh.get("Y Array Pos"));
				
				// Check top
				if (ex == x && ey - 1 == y) {
					adjacentCellList.add(mc);
				}
				// Check bottom
				if (ex == x && ey + 1 == y) {
					adjacentCellList.add(mc);
				}
				// Check left
				if (ey == y && ex - 1 == x) {
					adjacentCellList.add(mc);
				}
				// Check right
				if (ey == y && ex + 1 == x) {
					adjacentCellList.add(mc);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return adjacentCellList;
	}
	
	private boolean isInExistingBullishGroup(MapCell mc) {
		try {
			for (ArrayList<MapCell> group:bullishGroupList) {
				for (MapCell tmc:group) {
					if (tmc == mc) {
						return true;
					}
				}
			}
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean isInExistingBearishGroup(MapCell mc) {
		try {
			for (ArrayList<MapCell> group:bearishGroupList) {
				for (MapCell tmc:group) {
					if (tmc == mc) {
						return true;
					}
				}
			}
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean isCellBullish(MapCell mc) {
		try {
			if (mc == null) return false; 
			HashMap<String, Float> mvh = mc.getMetricValueHash();
			if (mvh.get(Constants.MAP_COLOR_OPTION_END_PERCENT_POSITIONS) > MAX_PERCENT_END_POSITION_FOR_CELL) return false;
			
			float numPositions = mvh.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
			float agmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR);
			float gmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR);
			float sharpe = mvh.get(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO);			
//			float score = (3 * agmpd) + (2 * sharpe) + (1f * gmpd); // Stocks
			float score = (10 * agmpd) + (1 * sharpe) + (10 * gmpd); // Bitcoin

			if (numPositions < MIN_NUM_POSITIONS) {
				return false;
			}

			if (agmpd >= MIN_AGMPD && gmpd >= MIN_GMPD && sharpe >= MIN_SHARPE && score >= MIN_SCORE) {
				mc.addToMetricValueHash("good", score);
				return true;
			}
			
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean isCellBearish(MapCell mc) {
		try {
			if (mc == null) return false; 
			HashMap<String, Float> mvh = mc.getMetricValueHash();
			if (mvh.get(Constants.MAP_COLOR_OPTION_END_PERCENT_POSITIONS) > MAX_PERCENT_END_POSITION_FOR_CELL) return false;
			
			float numPositions = mvh.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
			float agmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_ALPHA_GEOMEAN_PER_BAR);
			float gmpd = mvh.get(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR);
			float sharpe = mvh.get(Constants.MAP_COLOR_OPTION_ALL_SHARPE_RATIO);
//			float score = (3 * agmpd) + (2 * sharpe) + (1f * gmpd); // Stocks
			float score = (10 * agmpd) + (1 * sharpe) + (10 * gmpd); // Bitcoin

			if (numPositions < MIN_NUM_POSITIONS) {
				return false;
			}

			if (agmpd <= MAX_AGMPD && gmpd <= MAX_GMPD && sharpe <= MAX_SHARPE && score <= MAX_SCORE) {
				mc.addToMetricValueHash("bad", score);
				return true;
			}
			
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}