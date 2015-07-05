 package gui;

import evaluators.MapEvaluatorSingleton;
import gui.singletons.MapCellSingleton;
import gui.singletons.MapSymbolSingleton;
import gui.singletons.ParameterSingleton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.TransferHandler;

import utils.TransformUtil;
import constants.Constants;

public class MapCellPanel extends JPanel implements ComponentListener, Runnable, Serializable {

	private transient MapCellSingleton mcs = MapCellSingleton.getInstance();
	public transient ParameterSingleton ps = ParameterSingleton.getInstance(); 
	
	private ArrayList<MapCell> mapCells = new ArrayList<MapCell>(); // ONLY use these during saving & loading
	private ArrayList<MapCell> mapCellsSmoothed = new ArrayList<MapCell>();
	
	private transient GUI gui = null;

	private MapCellPanel thisMapPanel = null;
	
	private transient Graphics2D doubleBufferG = null;
	private transient Image doubleBufferImage = null;
	private transient Dimension screenDimensions = null;
	private transient Rectangle2D currentWorldView = null;
	private transient Rectangle2D defaultWorldView = null;
	private transient Dimension worldDimensions = null;
	private transient Point currentWorldViewMousePressedPoint = new Point();

	private float lowestColorMetric = 1000f;
	private float highestColorMetric = -1000f;
	private int mapBullishScore = 0;
	private int mapBearishScore = 0;
	private boolean running = true;
	
	public MapCellPanel(int worldWidth, int worldHeight, GUI gui) {
		super();
		this.gui = gui;
		this.worldDimensions = new Dimension(worldWidth, worldHeight);
		this.addComponentListener(this);
		this.setFocusable(true);
		addMouseListeners();
		currentWorldView = new Rectangle2D.Double();
		defaultWorldView = new Rectangle2D.Double();
		this.setTransferHandler(handler);
		thisMapPanel = this;
	}
	
	private TransferHandler handler = new TransferHandler() {
		@Override
		public boolean canImport(TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			Transferable t = support.getTransferable();
			try {
				List<File> l = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
				for (File f:l) {
					FileInputStream fis = new FileInputStream(f);
					ObjectInputStream ois = new ObjectInputStream(fis);
					Object o = ois.readObject();
					if (o instanceof MapCellPanel) {
						thisMapPanel = (MapCellPanel)o;
						// Do the hack where I put the MapCells back into the singleton because the singleton isn't serialized.
						mcs.setMapCells(thisMapPanel.mapCells);
						mcs.setMapCellsSmoothed(thisMapPanel.mapCellsSmoothed);
						mcs.setLock(new Object());
						lowestColorMetric = thisMapPanel.getLowestColorMetric();
						highestColorMetric = thisMapPanel.getHighestColorMetric();
						mapBullishScore = thisMapPanel.getMapBullishScore();
						mapBearishScore = thisMapPanel.getMapBearishScore();
						running = true;
						
						gui.setComponentValuesOnMapLoad(f.getName(), mapBullishScore, mapBearishScore);

						setDefaultWorldView();
						setCurrentWorldView();
						zoom(-80.5);
						panWorldViewToCellsLocation();
						recalculateHighAndLowMetrics();
						resetBuffer();
						repaint();
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
	};

	public synchronized void saveMaps(String params, String bullishScore, String bearishScore) {
		try {
			// First do the hack where I save the MapCells to this local object so they are serialized.
			setMapCells(mcs.getMapCells());
			setMapCellsSmoothed(mcs.getMapCellsSmoothed());
			
			String fileName = params.replaceAll("\"", "").replaceAll("/", "-");

			Calendar c = Calendar.getInstance();
			String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);

			// Bullish
			File bullishFile;
			String bullishPath = "";
			if (bullishScore == null) {
				bullishFile = new File("maps/" + date);
				bullishPath = "maps/" + date + "/Bullish/" + fileName + ".csm";
			}
			else {
				bullishFile = new File("maps/" + date + "/Bullish/" + bullishScore);
				bullishPath = "maps/" + date + "/Bullish/" + bullishScore + "/" + fileName + ".csm";
			}
			if (!bullishFile.exists())
				bullishFile.mkdirs();			
			
			FileOutputStream fos_BullishMCs = new FileOutputStream(bullishPath);
			ObjectOutputStream oos_BullishMCs = new ObjectOutputStream(fos_BullishMCs);
			oos_BullishMCs.writeObject(thisMapPanel);
			oos_BullishMCs.close();
			
			// Bearish
			File bearishFile;
			String bearishPath = "";
			if (bearishScore == null) {
				bearishFile = new File("maps/" + date);
				bearishPath = "maps/" + date + "/Bearish/" + fileName + ".csm";
			}
			else {
				bearishFile = new File("maps/" + date + "/Bearish/" + bearishScore);
				bearishPath = "maps/" + date + "/Bearish/" + bearishScore + "/" + fileName + ".csm";
			}
			if (!bearishFile.exists())
				bearishFile.mkdirs();			
			
			FileOutputStream fos_BearishMCs = new FileOutputStream(bearishPath);
			ObjectOutputStream oos_BearishMCs = new ObjectOutputStream(fos_BearishMCs);
			oos_BearishMCs.writeObject(thisMapPanel);
			oos_BearishMCs.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     * Sets the defaultWorldView. Only called once.
     */
    public void setDefaultWorldView() {
    	screenDimensions = this.getSize();
		int leftBorder = (int)((worldDimensions.width / 2f) - (screenDimensions.width / 2f));
		int topBorder = (int)((worldDimensions.height / 2f) - (screenDimensions.height / 2f));
	
		defaultWorldView = new Rectangle2D.Double();
		defaultWorldView.setFrame(leftBorder, topBorder, screenDimensions.getWidth(), screenDimensions.getHeight());
		resetBuffer();
    }
	    
    /**
     * Sets the currentWorldView variable, which is a window
     * the users sees to the world.
     * @return
     */
    public void setCurrentWorldView() {
    	screenDimensions = this.getSize();
		int leftBorder = (int)((worldDimensions.width / 2f) - (screenDimensions.width / 2f));
		int topBorder = (int)((worldDimensions.height / 2f) - (screenDimensions.height / 2f));
		
		currentWorldView = new Rectangle2D.Double();
		currentWorldView.setFrame(leftBorder, topBorder, screenDimensions.getWidth(), screenDimensions.getHeight());
    }
    
    public void panWorldViewToCellsLocation() {    	
    	float xMin = 0;
		float yMin = 0;
		float xMax = 0; 
		float yMax = 0;
		Point2D metricMins = new Point2D.Float(0, 0);
		if (ps.getxAxisMetric() != null && ps.getyAxisMetric() != null ) {
			xMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getxAxisMetric());
			yMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getyAxisMetric());
			xMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getxAxisMetric());
			yMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getyAxisMetric());
			
			float xRange = xMax - xMin;
			float yRange = yMax - yMin;
			
			float xScale = xRange / (float)Constants.WORLD_WIDTH;
			float yScale = yRange / (float)Constants.WORLD_HEIGHT;

			metricMins = new Point2D.Float(xMin / xScale, yMin / yScale );
		}

    	currentWorldView.setFrame(currentWorldView.getX() + metricMins.getX() - 3, currentWorldView.getY() + metricMins.getY() - 3, 
    			currentWorldView.getWidth(), currentWorldView.getHeight());
    }
    
	public void resetWorldView() {
		currentWorldView = (Rectangle2D)defaultWorldView.clone();
		repaint();
	}
	
	public void addMouseListeners () {
		this.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved (MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches > 0) {
					for (int a = 0; a < 10; a++) {
						zoom(.3);
						update(thisMapPanel.getGraphics());
					}
				}
				else {
					for (int a = 0; a < 10; a++) {
						zoom(-.3);
						update(thisMapPanel.getGraphics());
					}
				}
				repaint(); 
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			// For hovering over the map
			public void mouseMoved(MouseEvent me) {
				int mouseEventScreenX = (int)me.getLocationOnScreen().getX();
				int mouseEventScreenY = (int)me.getLocationOnScreen().getY();
				Point2D mouseEventScreenPoint = new Point2D.Double(me.getLocationOnScreen().getX(), me.getLocationOnScreen().getY());
				Point2D mouseEventPoint = new Point2D.Float(me.getX(), me.getY());
				Point2D worldP = TransformUtil.screenToWorld(mouseEventPoint, screenDimensions, currentWorldView);
				
				boolean overMap = false;
				MapCell highlightedMapCell = null;
				float mapXMetricMin = 0;
				float mapXMetricMax = 0;
				float mapYMetricMin = 0;
				float mapYMetricMax = 0;
				float xCellSize100Scale = 0;
				float yCellSize100Scale = 0;
				float xMetricScale = 0;
				float yMetricScale = 0;
				
				for (MapCell mapCell:mcs.getRequestedMapCellList(ps.isSmoothMap())) {
					if (mapCell != null) {
						HashMap<String, Float> metricValueHash = mapCell.getMetricValueHash();
						
						float cellX = metricValueHash.get("Map X 100 Scale");
						float cellY = metricValueHash.get("Map Y 100 Scale");
						float cellXSize = metricValueHash.get("Map X Cell Size 100 Scale");
						float cellYSize = metricValueHash.get("Map Y Cell Size 100 Scale");
						
						if (worldP.getX() >= cellX - 3 && worldP.getX() < cellX - 3 + cellXSize &&
								worldP.getY() >= cellY - 3 && worldP.getY() < cellY - 3 + cellYSize) {
							
							highlightedMapCell = new MapCell(mapCell);
							
							mapXMetricMin = metricValueHash.get("Map X Metric Min");
							mapXMetricMax = metricValueHash.get("Map X Metric Max");
							mapYMetricMin = metricValueHash.get("Map Y Metric Min");
							mapYMetricMax = metricValueHash.get("Map Y Metric Max");
							
							if (mapCell.getMetricValueHash().get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS) > 0)
								overMap = true;
						}
						
						// This is the cell size stuff mentioned just above
						xCellSize100Scale = mapCell.getMetricValueHash().get("Map X Cell Size 100 Scale");
						yCellSize100Scale = mapCell.getMetricValueHash().get("Map Y Cell Size 100 Scale");
						
						// Scale size
						xMetricScale = mapCell.getMetricValueHash().get("Map X Metric Scale");
						yMetricScale = mapCell.getMetricValueHash().get("Map Y Metric Scale");
					}
				}
				
				// Cell Tool Tips
				CellToolTip cellToolTip = CellToolTip.getInstance();
				if (overMap && ps.isShowCellTooltips()) {
					cellToolTip.update(thisMapPanel, highlightedMapCell, mouseEventScreenX, mouseEventScreenY, mouseEventPoint, 
							mapXMetricMin, mapXMetricMax, mapYMetricMin, mapYMetricMax);
				}
				else {
					cellToolTip.setVisible(false);
				}
				
				// Stock Tool Tips
				StockToolTip stockToolTip = StockToolTip.getInstance();
				MapSymbol stockToolTipSymbol = null;
				for (MapSymbol mapSymbol:MapSymbolSingleton.getInstance().getMapSymbols()) {
					// Determine the symbol's location
					Point2D location = new Point2D.Float(mapSymbol.getXMetricValue() * xMetricScale, mapSymbol.getYMetricValue() * yMetricScale);
					Point2D screenLocation = TransformUtil.worldToScreen(location, thisMapPanel.getSize(), currentWorldView);
					
					// Have to adjust 1/2 cell both to the left and up because cells are drawn based on their centers?
					Point2D dimensions = TransformUtil.worldToScreenScaleOnly(new Point2D.Double(xCellSize100Scale + .2f, yCellSize100Scale + .2f), thisMapPanel.getSize(), currentWorldView);
					screenLocation.setLocation(screenLocation.getX() - (dimensions.getX() / 2f), screenLocation.getY() - (dimensions.getY() / 2f));
					
					double distance = screenLocation.distance(mouseEventPoint);
					if (distance < 3) {		
						stockToolTipSymbol = mapSymbol;
					}
				}
				if (stockToolTipSymbol != null && ps.isShowStockTooltips()) {
					stockToolTip.update(stockToolTipSymbol, mouseEventScreenPoint);
				}
				else {
					stockToolTip.setVisible(false);
				}
			}
			
			public void mouseDragged(MouseEvent me) {
				double deltaX = currentWorldViewMousePressedPoint.getX() - me.getX();
				double deltaY = currentWorldViewMousePressedPoint.getY() - me.getY();
				
				// Convert the screen delta vector to a world delta vector
				Point2D scaleVector = TransformUtil.screenToWorldScaleOnly(new Point2D.Double(deltaX, deltaY), screenDimensions, currentWorldView);
				deltaX = scaleVector.getX();
				deltaY = scaleVector.getY();

				currentWorldView.setFrame(currentWorldView.getX() + deltaX, currentWorldView.getY() + deltaY, 
						currentWorldView.getWidth(), currentWorldView.getHeight());
				
				update(thisMapPanel.getGraphics());
				currentWorldViewMousePressedPoint = me.getPoint();
				thisMapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				repaint();
			}
		});
		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				currentWorldViewMousePressedPoint = me.getPoint();
			}
			
			public void mouseReleased(MouseEvent me) {
				thisMapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
			public void mouseExited(MouseEvent me) {
				CellToolTip.getInstance().setVisible(false);
				StockToolTip.getInstance().setVisible(false);
			}
		});
	}

	/**
	 * Zooms on the MapPanel.
	 * The amount parameter is a positive or negative number
	 * representing the percentage you want to zoom in our out.
	 * For example, 33 zooms in 33%.  -50.5 zooms out 50.5%
	 * @param amount 
	 */
	public void zoom(double amount) {
		amount = 1 + (amount / 100f);
		Point2D currentWorldViewSize = new Point2D.Double(currentWorldView.getWidth(), currentWorldView.getHeight());
		AffineTransform scaleTransform = new AffineTransform();
		scaleTransform.scale(amount, amount);
		Point2D newWorldViewSize = scaleTransform.transform(currentWorldViewSize, null);
		
		double xOffset = (currentWorldView.getCenterX()) - (worldDimensions.width / 2f);
		double yOffset = (currentWorldView.getCenterY()) - (worldDimensions.height / 2f);
		double leftBorder = (worldDimensions.width / 2f) + xOffset - (newWorldViewSize.getX() / 2f);
		double topBorder = (worldDimensions.height / 2f) + yOffset - (newWorldViewSize.getY() / 2f);
		
		currentWorldView.setFrame(leftBorder, topBorder, newWorldViewSize.getX(), newWorldViewSize.getY());
	}

	/**
     * Resets the image buffer for smooth graphics.
     */
    public void resetBuffer() {
    	if (doubleBufferG != null) {
    		doubleBufferG.dispose();
    		doubleBufferG = null;
    	}
    	if (doubleBufferImage != null) {
    		doubleBufferImage.flush();
    		doubleBufferImage = null;
    	}
    	System.gc();
    	doubleBufferImage = new BufferedImage(screenDimensions.width, screenDimensions.height, BufferedImage.TYPE_INT_ARGB);
    	doubleBufferG = (Graphics2D)doubleBufferImage.getGraphics();
    	
    	// Set Background
    	Composite currComposite = doubleBufferG.getComposite();
		Color currColor = doubleBufferG.getColor();
		doubleBufferG.fillRect(0, 0, worldDimensions.width, worldDimensions.height);
		
		// Set composite back to what it was originally
		if (currComposite != null && doubleBufferG != null)
			doubleBufferG.setComposite(currComposite);
					
		// Set color back to what it was originally
		if (doubleBufferG != null)
			doubleBufferG.setColor(currColor);
    }
    
	/**
	 * Paints everything, but to a buffer which is copied over to
	 * the screen when each frame is completely rendered.
	 * @param g
	 */
	public void paintBuffer(Graphics2D g) {
		try {		
			// Draw Background
			Composite currComposite = g.getComposite();
			Color currColor = g.getColor();
			
			// Set composite back to what it was originally
			g.setComposite(currComposite);
			// Set color back to what it was originally
			g.setColor(currColor);
			
			// Store the screen locations of the cells to help locate where to draw the axis
			ArrayList<Point2D> screenCellLocations = new ArrayList<Point2D>();
			
			if (mcs.getMapCells().size() == 0) return;
			
			// Draw Map Cells
			synchronized(mcs.getMapCells()) {
				// So I know the size of each cell
				float xCellSize100Scale2 = 0f;
				float yCellSize100Scale2 = 0f;
				
				// So I know the location of each cell
				float xMetricScale = 0f;
				float yMetricScale = 0f;

				for (MapCell mapCell:mcs.getMapCells()) {					
					// This is the cell size stuff mentioned just above
					xCellSize100Scale2 = mapCell.getMetricValueHash().get("Map X Cell Size 100 Scale");
					yCellSize100Scale2 = mapCell.getMetricValueHash().get("Map Y Cell Size 100 Scale");
					
					// This is the scale size stuff mentioned just above
					xMetricScale = mapCell.getMetricValueHash().get("Map X Metric Scale");
					yMetricScale = mapCell.getMetricValueHash().get("Map Y Metric Scale");
				}
				
				MapCell[][]mapCellGrid = mcs.getGridVersion(mcs.getRequestedMapCellList(ps.isSmoothMap()));
				
				// Recalculate High & Low Metrics for coloring
				recalculateHighAndLowMetrics();

				// Go through the cells and draw them
				for (int x = 0; x < ps.getxRes(); x++) {
					for (int y = 0; y < ps.getyRes(); y++) {
						if (mapCellGrid[x][y] != null) {
							HashMap<String, Float> centerMVH = mapCellGrid[x][y].getMetricValueHash();

							float x100Scale = centerMVH.get("Map X 100 Scale");
							float y100Scale = centerMVH.get("Map Y 100 Scale");
							float xCellSize100Scale = centerMVH.get("Map X Cell Size 100 Scale");
							float yCellSize100Scale = centerMVH.get("Map Y Cell Size 100 Scale");
							Float good = centerMVH.get("good");
							Float bad = centerMVH.get("bad");
		
							// Determine the cell location
							Point2D cellCenter = new Point2D.Float(x100Scale, y100Scale);
							
							Point2D screenLocation = TransformUtil.worldToScreen(cellCenter, this.getSize(), currentWorldView);
							screenCellLocations.add(screenLocation);
							Point2D dimensions = TransformUtil.worldToScreenScaleOnly(new Point2D.Double(xCellSize100Scale + .2f, yCellSize100Scale + .2f), this.getSize(), currentWorldView);

							float numResults = centerMVH.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
							float colorMetric = centerMVH.get(ps.getMapColor());

							// Calculate a color
							if (ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT) ||
									ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT) ||
									ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT) ||
									ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT)) {
								colorMetric -= 50f;
							}
	
							float red = 1f;
							float green = 1f;
							float blue = 1f;
							
							// Red White Green
							if (numResults >= ps.getMinNumResults()) {			
								float zeroToHigher = Math.abs(lowestColorMetric);
								if (Math.abs(highestColorMetric) > zeroToHigher) {
									zeroToHigher = Math.abs(highestColorMetric);
								}
								
								if (colorMetric > 0) {
									green = 1f;
									red = 1f - (colorMetric / zeroToHigher);
									blue = 1f - (colorMetric / zeroToHigher);

									if (red > 1f) red = 1f;
									if (blue > 1f) blue = 1f;
									if (red < 0f) red = 0f;
									if (blue < 0f) blue = 0f;
								}
								if (colorMetric < 0) {
									red = 1f;
									if ((Math.abs(colorMetric) / zeroToHigher) > 1f) {
										green = 0;
									}
									else {
										green = 1f - (Math.abs(colorMetric) / zeroToHigher);
									}
									if ((Math.abs(colorMetric) / zeroToHigher) > 1f) {
										blue = 0;
									}
									else {
										blue = 1f - (Math.abs(colorMetric) / zeroToHigher);
									}
									
									if (green > 1f) green = 1f;
									if (blue > 1f) blue = 1f;
								}
								
							}
							// Blue
							else if (numResults > 0) {
								blue = (numResults / (float)(ps.getMinNumResults() - 1));
								red = 0f;
								green = 0f + (blue / 3f);
							}
							// Grey
							else {
								blue = .15f;
								red = .15f;
								green = .15f;
							}
		
							Color c = new Color(red, green, blue);
							g.setColor(c);
							
							// Draw the Cell
							g.fillRect((int)(screenLocation.getX() - (dimensions.getX() / 2)),
									(int)(screenLocation.getY() - (dimensions.getY() / 2)),
									(int)(dimensions.getX()), 
									(int)(dimensions.getY()));
							// Bullish / Bearish Group Overlay
							if (ParameterSingleton.getInstance().isShowGroups()) {
								if ((	good != null && good >= MapEvaluatorSingleton.MIN_SCORE) ||
										(bad != null && bad <= MapEvaluatorSingleton.MAX_SCORE)) {
									BufferedImage mask = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
									Graphics2D maskG = mask.createGraphics();
									Float bullGroup = centerMVH.get("Part of best bull group");
									Float bearGroup = centerMVH.get("Part of best bear group");
									if ((bullGroup != null && bullGroup == 1) || (bearGroup != null && bearGroup == 1)) {
										maskG.setColor(Color.BLACK);
									}
									else {
										maskG.setColor(new Color(160, 160, 160));
									}
									maskG.drawLine(4,0,7,3);
									maskG.drawLine(0,7,7,0);
									maskG.drawLine(0,4,3,7);
									Rectangle r = new Rectangle(0, 0, 8, 8);
									TexturePaint tp = new TexturePaint(mask, r);
									g.setPaint(tp);
									g.fillRect((int)(screenLocation.getX() - (dimensions.getX() / 2)),
											(int)(screenLocation.getY() - (dimensions.getY() / 2)),
											(int)(dimensions.getX()), 
											(int)(dimensions.getY()));
								}
							}
						}
					}
				} // End going through the cells
				
				// Go through map symbols and draw them
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Draw map symbols
				for (MapSymbol mapSymbol:MapSymbolSingleton.getInstance().getMapSymbols()) {
					// Determine the symbol's location
					Point2D location = new Point2D.Float(mapSymbol.getXMetricValue() * xMetricScale, mapSymbol.getYMetricValue() * yMetricScale);
					Point2D screenLocation = TransformUtil.worldToScreen(location, this.getSize(), currentWorldView);
					
					// Have to adjust 1/2 cell both to the left and up because cells are drawn based on their centers?
					Point2D dimensions = TransformUtil.worldToScreenScaleOnly(new Point2D.Double(xCellSize100Scale2 + .2f, yCellSize100Scale2 + .2f), this.getSize(), currentWorldView);
					screenLocation.setLocation(screenLocation.getX() - (dimensions.getX() / 2f), screenLocation.getY() - (dimensions.getY() / 2f));
					
					// Figure out the inside color based on how stale it is
					Color insideColor = getMapSymbolColor(mapSymbol.getLastUpdated().getTimeInMillis());
					
					// Check to see if this symbol is being searched for
					if (mapSymbol.getSymbol().equalsIgnoreCase(ps.getFind().trim())) {
						// Draw the outside
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(3.0f));
						g.drawOval((int)screenLocation.getX() - 6, (int)screenLocation.getY() - 6, 12, 12);
						
						// Draw the inside
						g.setColor(insideColor);
						g.setStroke(new BasicStroke(2f));
						g.drawOval((int)screenLocation.getX() - 4, (int)screenLocation.getY() - 4, 8, 8);
					}
					else {
						// Draw the outside
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(1.5f));
						g.drawOval((int)screenLocation.getX() - 3, (int)screenLocation.getY() - 3, 6, 6);						
	
						// Draw the inside
						g.setColor(insideColor);
						g.setStroke(new BasicStroke(1f));
						g.drawOval((int)screenLocation.getX() - 2, (int)screenLocation.getY() - 2, 4, 4);
					}
					
					g.setStroke(new BasicStroke(1f));
				}
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			} // End sync
		
			// Draw Axis Labels
			drawAxisLabels(g, screenCellLocations);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Color getMapSymbolColor(long lastUpdated) {
		Calendar c = Calendar.getInstance();
		long now = c.getTimeInMillis();
		long ageMS = now - lastUpdated;
//		System.out.println(ageMS);
		long maxAgeMS = 15000;
		float percentOfMaxAge = ageMS / (float)maxAgeMS;
		if (percentOfMaxAge > 1)
			percentOfMaxAge = 1;
		
		int orangeR = 255;
		int orangeG = 196;
		int orangeB = 0;
		
		int grayR = 196;
		int grayG = 196;
		int grayB = 196;
		
		int changeR = orangeR - grayR;
		int changeG = orangeG - grayG;
		int changeB = orangeB - grayB;
		
		float deltaR = (changeR * percentOfMaxAge);
		float deltaG = (changeG * percentOfMaxAge);
		float deltaB = (changeB * percentOfMaxAge);
		
		int newR = (int)(orangeR - deltaR);
		int newG = (int)(orangeG - deltaG);
		int newB = (int)(orangeB - deltaB);
		
//		System.out.println(newR + ", " + newG + ", " + newB);
		
		return new Color(newR, newG, newB);
	}
	
	public void recalculateHighAndLowMetrics() {
		// Figure out the highest and lowest metrics first (NEED TO IMPROVE THIS)
		this.lowestColorMetric = 100.001f;
		this.highestColorMetric = -100.001f;
		
		ArrayList<MapCell> mapCells = new ArrayList<MapCell>();
		mapCells.addAll(mcs.getRequestedMapCellList(ps.isSmoothMap()));
		
		for (MapCell mapCell:mapCells) {
			if (mapCell != null) {
				HashMap<String, Float> metricValueHash = mapCell.getMetricValueHash();
				float numResults = metricValueHash.get(Constants.MAP_COLOR_OPTION_ALL_NUM_POSITIONS);
				
				float colorMetric = metricValueHash.get(ps.getMapColor());
				
				// Calculate a color
				if (ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT) ||
						ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT) ||
						ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT) ||
						ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT)) {
					colorMetric -= 50f;
				}
	
				if (numResults >= ps.getMinNumResults()) {
					if (colorMetric < this.lowestColorMetric) {
						this.lowestColorMetric = colorMetric;
					}
					if (colorMetric > this.highestColorMetric) {
						this.highestColorMetric = colorMetric;
					}
				}
			}
		}
	}
	
	public void drawAxisLabels(Graphics g, ArrayList<Point2D> screenCellLocations) {
		try {
			if (screenCellLocations != null && screenCellLocations.size() > 0) {
			
				g.setColor(Color.WHITE);
				g.setFont(new Font("Dialog", Font.PLAIN, 9));
				
				// Get the min and max coordinates of each axis
				int minX = 1000;
				int minY = 1000;
				int maxX = -1000;
				int maxY = -1000;
				for (Point2D p:screenCellLocations) {
					if (p.getX() < minX)
						minX = (int)p.getX();
					if (p.getX() > maxX)
						maxX = (int)p.getX();
					if (p.getY() < minY)
						minY = (int)p.getY();
					if (p.getY() > maxY)
						maxY = (int)p.getY();
				}
				
				float xMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getxAxisMetric());
				float xMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getxAxisMetric());
				float yMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getyAxisMetric());
				float yMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getyAxisMetric());
				
				float xRange = xMax - xMin;
				float yRange = yMax - yMin;
				float xIncrement = xRange / (float)ps.getxRes();
				float yIncrement = yRange / (float)ps.getyRes();
				
				// Draw axis labels
				float x = xMin;
				float y = yMin;
				
				// Y
				float lastY = 0f;
				float deltaY = 0f;
				for (Point2D p:screenCellLocations) {
					if (y < yMax - .01) {
						g.drawString(String.format("%.1f", y), minX - 36, (int)(p.getY() - 15));
					}
					else {
						break;
					}
					y += yIncrement;
					deltaY = (float)p.getY() - lastY;
					lastY = (float)p.getY();
				}
				if (deltaY != 0) {
					g.drawString(String.format("%.1f", y), minX - 36, (int)(lastY + deltaY - 15));
				}
				
				// X
				int index = 0;
				float lastX = 0f;
				float deltaX = 0f;
				for (Point2D p:screenCellLocations) {
					if (index % ps.getxRes() == 0) {
						if (x < xMax - .01) {
							g.drawString(String.format("%.1f", x), (int)(p.getX() - 18), maxY + 26);
						}
						else {
							break;
						}
						x += xIncrement;
						deltaX = (float)p.getX() - lastX;
						lastX = (float)p.getX();
					}
					index++;
				}
				if (deltaX != 0) {
					g.drawString(String.format("%.1f", x), (int)(lastX + deltaX - 18), maxY + 26);
				}
			}
		}
		catch (Exception e) {
			System.out.println("***** " + ps.getxAxisMetric() + ", " + ps.getyAxisMetric());
			e.printStackTrace();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		if (doubleBufferImage == null || doubleBufferG == null) {
			resetBuffer();
		}

		doubleBufferG.clearRect(0, 0, screenDimensions.width, screenDimensions.height);
		paintBuffer(doubleBufferG);
		if (g != null)
			g.drawImage(doubleBufferImage, 0, 0, this);
	}
	
	public void run() {
		while (running) {}
	}
	
	@Override
	public void updateUI() {
		super.updateUI();
		repaint();
	}

	public String getLegendMax() {
		try {
			recalculateHighAndLowMetrics();
			
			String max = "";

			float lowestAbs = Math.abs(lowestColorMetric);
			float highestAbs = Math.abs(highestColorMetric);
			float r = highestAbs;
			if (lowestAbs > highestAbs)
				r = lowestAbs;
			
			if (ps.getMapColor().contains("#")) {
				max = String.format("%.0f", r);
			}
			else if (ps.getMapColor().contains("Metric - Mean Win %") || ps.getMapColor().contains("Stop - Mean Win %")) {
				max = String.format("%.1f", r) + "%";
			}
			else if (ps.getMapColor().contains("Win %")) {
				r += 50;
				max = String.format("%.1f", r) + "%";
			}
			else if (ps.getMapColor().contains("Mean Return") || ps.getMapColor().contains("%") || ps.getMapColor().contains("Median") || ps.getMapColor().contains("Geo-Mean /")) {
				max = String.format("%.1f", r) + "%";
			}
			else {
				max = String.format("%.1f", r);
			}
			return max;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Err";
		}
	}
	
	public String getLegendMin() {
		try {
			recalculateHighAndLowMetrics();
			
			String min = "";
			
			float r = Math.abs(highestColorMetric);
			if (Math.abs(lowestColorMetric) > Math.abs(highestColorMetric))
				r = Math.abs(lowestColorMetric);
			
			if (ps.getMapColor().contains("#")) {
				min = "-" + String.format("%.0f", r);
			}
			else if (ps.getMapColor().contains("Metric - Mean Win %") || ps.getMapColor().contains("Stop - Mean Win %")) {
				min = "-" + String.format("%.1f", r) + "%";
			}
			else if (ps.getMapColor().contains("Win %")) {
				r += 50;
				float diff = 100 - r;
				min = String.format("%.1f", diff) + "%";
			}
			else if (ps.getMapColor().contains("Mean Return") || ps.getMapColor().contains("%") || ps.getMapColor().contains("Median") || ps.getMapColor().contains("Geo-Mean /")) {
				min = "-" + String.format("%.1f", r) + "%";
			}
			else {
				min = "-" + String.format("%.1f", r);
			}
			
			return min;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Err";
		}
	}
	
	public String getLegendMiddle() {
		try {	
			recalculateHighAndLowMetrics();
			
			if (ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_ALL_MEAN_WIN_PERCENT) ||
					ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_STOP_MEAN_WIN_PERCENT) ||
					ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_END_MEAN_WIN_PERCENT) ||
					ps.getMapColor().equals(Constants.MAP_COLOR_OPTION_ALL_ALPHA_MEAN_WIN_PERCENT)) {
				return "50.0";
			}
			else {
				return "0.0";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Err";
		}
	}
	
    public void componentResized(ComponentEvent e) {
    	try {
	    	setCurrentWorldView();
			resetBuffer();
			repaint();
    	}
    	catch (Exception ex) {}
    }
    
    public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}
   
    public void componentShown(ComponentEvent e) {}
 

	public float getLowestColorMetric() {
		return lowestColorMetric;
	}

	public float getHighestColorMetric() {
		return highestColorMetric;
	}

	public void setDoubleBufferG(Graphics2D doubleBufferG) {
		this.doubleBufferG = doubleBufferG;
	}

	public void setDoubleBufferImage(Image doubleBufferImage) {
		this.doubleBufferImage = doubleBufferImage;
	}

	public GUI getGui() {
		return gui;
	}

	public int getMapBullishScore() {
		return mapBullishScore;
	}

	public void setWorldDimensions(Dimension worldDimensions) {
		this.worldDimensions = worldDimensions;
	}

	public void setMapBullishScore(int bullishScore) {
		this.mapBullishScore = bullishScore;
	}

	public int getMapBearishScore() {
		return mapBearishScore;
	}

	public void setMapBearishScore(int bearishScore) {
		this.mapBearishScore = bearishScore;
	}
	
	public void setMapCells(ArrayList<MapCell> mapCells) {
		this.mapCells = mapCells;
	}

	public void setMapCellsSmoothed(ArrayList<MapCell> mapCellsSmoothed) {
		this.mapCellsSmoothed = mapCellsSmoothed;
	}

	public ArrayList<MapCell> getMapCells() {
		return mapCells;
	}

	public ArrayList<MapCell> getMapCellsSmoothed() {
		return mapCellsSmoothed;
	}
}