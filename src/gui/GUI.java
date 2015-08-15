package gui;

import gui.singletons.MapCellSingleton;
import gui.singletons.MapSymbolSingleton;
import gui.singletons.ParameterSingleton;
import gui.threads.MUTCoordinator;
import gui.threads.MapUpdateThread;
import gui.threads.MapUpdateThreadResultSetter;
import gui.threads.RealtimeTrackerThread;
import gui.threads.RealtimeTrackerThreadResultSetter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.math3.util.Precision;

import search.ScoreSingleton;
import utils.CalcUtils;

import com.toedter.calendar.JDateChooser;

import constants.Constants;
import dbio.QueryManager;
import evaluators.MapEvaluatorSingleton;


public class GUI {

	private JFrame frame = null;
	private JPanel panel = null;
	public static Color colorSectionBackground = new Color(38, 68, 166, 255);
	public static Color colorSelectionForeground = Color.WHITE;
	private Color colorGreen = new Color(.16f, .6f, 0f);
	
	private Color colorBlue1 = new Color(0f, .1f, .3f);
	private Color colorBlue2 = new Color(0f, .166f, .5f);
	private Color colorBlue3 = new Color(0f, .233f, .7f);
	private Color colorBlue4 = new Color(0f, .33f, 1f);
	
	private Color colorRed4 = new Color(1f, .75f, .75f);
	private Color colorRed3 = new Color(1f, .5f, .5f);
	private Color colorRed2 = new Color(1f, .25f, .25f);
	private Color colorRed1 = new Color(1f, 0f, 0f);
	
	private Color colorGreen1 = new Color(.75f, 1f, .75f);
	private Color colorGreen2 = new Color(.5f, 1f, .5f);
	private Color colorGreen3 = new Color(.25f, 1f, .25f);
	private Color colorGreen4 = new Color(0f, 1f, 0f);
	
	// Buy Panel
	private JPanel pnlBuyCriteria = null;
	private JLabel lblBuyCriteria = null;
	private JLabel lblBuyXAxis = null;
	private JLabel lblBuyYAxis = null;
	private JComboBox cbBuyXAxis = null;
	private JComboBox cbBuyYAxis = null;
	
	// Sell Panel
	private JPanel pnlSellCriteria = null;
	private JLabel lblSellCriteria = null;
	private JLabel lblMetric = null;
	private JLabel lblValue = null;
	private JComboBox cbMetric = null;
	private JTextField txtValue = null;
	private JLabel lblOperator = null;
	private JTextField txtOperator = null;
	
	// Period Panel
	private JPanel pnlPeriod = null;
	private JLabel lblPeriod = null;
	private JLabel lblFrom = null;
	private JLabel lblTo = null;
	private JDateChooser calFrom = null;
	private JDateChooser calTo = null;

	// Map Resolution Panel
	private JPanel pnlResolution = null;
	private JLabel lblResolution = null;
	private JLabel lblXRes = null;
	private JLabel lblYRes = null;
	private JLabel lblMinNumResults = null;
	private JCheckBox chkSmooth = null;
	private JTextField txtXRes = null;
	private JTextField txtYRes = null;
	private JTextField txtMinNumResults = null;
	
	// Map Options Panel
	private JPanel pnlMapOptions = null;
	private JLabel lblMapOptions = null;
	private JLabel lblOption = null;
	private JComboBox cbBasedOn = null;
	private JCheckBox chkShowGroups = null;
	private JLabel lblFind = null;
	private JTextField txtFind = null;
	
	// Filters Panel
	private JPanel pnlFilters = null;
	private JLabel lblFilters = null;
	private JLabel lblMinLiquidity = null;
	private JLabel lblMaxVolatility = null;
	private JLabel lblMinPrice = null;
	private JLabel lblSector = null;
	private JLabel lblIndustry = null;
	private JTextField txtMinLiquidity = null;
	private JTextField txtMaxVolatility = null;
	private JTextField txtMinPrice = null;
	private JComboBox cbSector = null;
	private JComboBox cbIndustry = null;
	private JCheckBox chkNYSE = null;
	private JCheckBox chkNasdaq = null;
	private JCheckBox chkDJIA = null;
	private JCheckBox chkSP500 = null;
	private JCheckBox chkETF = null;
	private JCheckBox chkBitcoin = null;
	private JList listSymbols = null;
	private JScrollPane listScroller = null;
	
	// Stop Loss Panel
	private JPanel pnlStopLoss = null;
	private JLabel lblStopLoss = null;
	private JLabel lblStopMetric = null;
	private JLabel lblStopValue = null;
	private JComboBox cbStopMetric = null;
	private JTextField txtStopValue = null;
	
	// Legend Panel
	private JPanel pnlLegend = null;
	private JPanel pnlGreen1 = null;
	private JPanel pnlGreen2 = null;
	private JPanel pnlGreen3 = null;
	private JPanel pnlGreen4 = null;
	private JPanel pnlWhite = null;
	private JPanel pnlRed1 = null;
	private JPanel pnlRed2 = null;
	private JPanel pnlRed3 = null;
	private JPanel pnlRed4 = null;
	private JPanel pnlBlue1 = null;
	private JPanel pnlBlue2 = null;
	private JPanel pnlBlue3 = null;
	private JPanel pnlBlue4 = null;
	private JLabel lblGreen = null;
	private JLabel lblRed = null;
	private JLabel lblWhite = null;
	private JLabel lblBlue = null;
	
	// Eval Panel
	private JPanel pnlEval = null;
	private JButton btnEvalMap = null;
	private JLabel lblMapBullishScore = null;
	private JLabel lblMapBearishScore = null;
	private JToggleButton btnTrackRealtime = null;
	
	// Other GUI Components
	private JButton btnBuildMap = null;
	private MapCellPanel pnlMapCell = null;
	
	private JCheckBox chkShowCellToolTips = null;
	private JCheckBox chkShowStockToolTips = null;

	private boolean runViaParams = false;

	private ArrayList<String> indexFilterList = new ArrayList<String>();
	
	private static MapWorker mapWorker = null;
	private static RealtimeTrackerWorker rttWorker = null;
	private static int NUM_MAP_THREADS = 6;
	private static MapUpdateThread[] muts = new MapUpdateThread[NUM_MAP_THREADS];
	private static RealtimeTrackerThread rtt = null;
	private static ParameterSingleton ps = ParameterSingleton.getInstance();
	
	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.initializeGUIComponents();
		gui.getJFrame().show();
		gui.resetBlankMUTs();
		mapWorker = gui.new MapWorker();
		mapWorker.done = true; // Don't set it to false until it actually starts something.
		
		if (args.length == 22) {
			gui.runViaParams(args);
		}
	}
	
	public static void runBackdoorTradeMonitor(String fileName, int mapBullishScore, int mapBearishScore, MapCellPanel mcp) {
		try {
			GUI gui = new GUI();
			gui.initializeGUIComponents();
			gui.setComponentValuesOnMapLoad(fileName, mapBullishScore, mapBearishScore);
			gui.getJFrame().show();
			gui.resetBlankMUTs();
			mapWorker = gui.new MapWorker();
			mapWorker.done = true;
			
			gui.pnlMapCell.ps = ParameterSingleton.getInstance();
			gui.pnlMapCell.setWorldDimensions(new Dimension(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
			gui.pnlMapCell.setDefaultWorldView();
			gui.pnlMapCell.setCurrentWorldView();
			gui.pnlMapCell.zoom(-83.5);
			gui.pnlMapCell.panWorldViewToCellsLocation();
			gui.pnlMapCell.updateUI();
			gui.pnlMapCell.repaint();

			gui.btnTrackRealtime.doClick();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void runBackdoor(String[] args) {
		try {
			MapCellSingleton.getInstance().setMapCells(new ArrayList<MapCell>());
			MapCellSingleton.getInstance().setMapCellsSmoothed(new ArrayList<MapCell>());
			
			GUI gui = new GUI();
			gui.initializeGUIComponents();
//			gui.getJFrame().setState(Frame.ICONIFIED); // Works, but causes some bug
			gui.getJFrame().show();
			gui.resetBlankMUTs();
			mapWorker = gui.new MapWorker();
			
			if (args.length == 22) {
				gui.runViaParams(args);
			}

			while (!mapWorker.done) {
				Thread.sleep(500);
			}

			pnlMapCell = null;
			
			if (gui != null) {
				gui.getJFrame().removeAll();
			}
			if (gui != null) {
				gui.getJFrame().dispose();
			}
			gui = null;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void runViaParams(String[] args) {
		try {
			runViaParams = true;
			
			cbBuyXAxis.setSelectedItem(args[0]);
			ps.setxAxisMetric(args[0]);
			
			cbBuyYAxis.setSelectedItem(args[1]);
			ps.setyAxisMetric(args[1]);
			
			cbMetric.setSelectedItem(args[2]);
			ps.setSellMetric(args[2]);
			
			txtOperator.setText(args[3]);
			ps.setSellOperator(args[3]);
			
			txtValue.setText(args[4]);
			ps.setSellValue(new Float(args[4]));
			
			cbStopMetric.setSelectedItem(args[5]);
			ps.setStopMetric(args[5]);
			
			txtStopValue.setText(args[6]);
			if (args[6].equals("\"\""))
				ps.setStopValue(null);
			else
				ps.setStopValue(new Float(args[6]));
			
			Date from = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(args[7]);
			calFrom.setDate(from);
			ps.setFromCal(calFrom.getCalendar());
			
			Date to = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(args[8]);
			calTo.setDate(to);
			ps.setToCal(calTo.getCalendar());
			
			txtXRes.setText(args[9]);
			ps.setxRes(new Integer(args[9]));
			
			txtYRes.setText(args[10]);
			ps.setyRes(new Integer(args[10]));
			
			txtMinLiquidity.setText(args[11]);
			ps.setMinLiquidity(new Integer(args[11]));
			
			txtMaxVolatility.setText(args[12]);
			ps.setMaxVolatility(new Float(args[12]));
			
			txtMinPrice.setText(args[13]);
			ps.setMinPrice(new Float(args[13]));
			
			cbSector.setSelectedItem(args[14]);
			ps.setSector(args[14]);
			
			cbIndustry.setSelectedItem(args[15]);
			ps.setIndustry(args[15]);
			
			chkNYSE.setSelected(Boolean.valueOf(args[16]));
			ps.setNyse(Boolean.valueOf(args[16]));
			
			chkNasdaq.setSelected(Boolean.valueOf(args[17]));
			ps.setNasdaq(Boolean.valueOf(args[17]));
			
			chkDJIA.setSelected(Boolean.valueOf(args[18]));
			ps.setDjia(Boolean.valueOf(args[18]));
			
			chkSP500.setSelected(Boolean.valueOf(args[19]));
			ps.setSp500(Boolean.valueOf(args[19]));
			
			chkETF.setSelected(Boolean.valueOf(args[20]));
			ps.setEtf(Boolean.valueOf(args[20]));
			
			args[21] = args[21].replaceAll("\\.csm", "");
			chkBitcoin.setSelected(Boolean.valueOf(args[21]));
			ps.setBitcoin(Boolean.valueOf(args[21]));
			
			btnBuildMap.doClick();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setComponentValuesOnMapLoad(String fileName, int bullishScore, int bearishScore) {
		try {
			// TODO: Might not need to set all the ps stuff here cause it's supposedly already set if you look at this method's call hierarchy
			fileName = fileName.replaceAll("% ", "%%");
			fileName = fileName.replaceAll("# ", "##");
			String[] parts = fileName.split(" ");
			parts[5] = parts[5].replaceAll("%%", "% ");
			parts[5] = parts[5].replaceAll("##", "# ");
			parts[21] = parts[21].replaceAll("\\.csm", "");
			
			cbBuyXAxis.setSelectedItem(parts[0]);
			ps.setxAxisMetric(parts[0]);
			
			cbBuyYAxis.setSelectedItem(parts[1]);
			ps.setyAxisMetric(parts[1]);
			
			cbMetric.setSelectedItem(parts[2]);
			ps.setSellMetric(parts[2]);
			
			String operator = ">=";
			if (parts[3].equals("GTE")) {
				operator = ">=";
			}
			if (parts[3].equals("LTE")) {
				operator = "<=";
			}
			txtOperator.setText(operator);
			ps.setSellOperator(operator);
			
			txtValue.setText(parts[4]);
			ps.setSellValue(new Float(parts[4]));
			
			cbStopMetric.setSelectedItem(parts[5]);
			ps.setStopMetric(parts[5]);
			
			txtStopValue.setText(parts[6]);
			ps.setStopValue(new Float(parts[6]));
			
			Date from = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(parts[7]);
			calFrom.setDate(from);
			ps.setFromCal(calFrom.getCalendar());
			
			Date to = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(parts[8]);
			calTo.setDate(to);
			ps.setToCal(calTo.getCalendar());
			
			txtXRes.setText(parts[9]);
			ps.setxRes(new Integer(parts[9]));
			
			txtYRes.setText(parts[10]);
			ps.setyRes(new Integer(parts[10]));
			
			txtMinLiquidity.setText(parts[11]);
			ps.setMinLiquidity(new Integer(parts[11]));
			
			txtMaxVolatility.setText(parts[12]);
			ps.setMaxVolatility(new Float(parts[12]));
			
			txtMinPrice.setText(parts[13]);
			ps.setMinPrice(new Float(parts[13]));
			
			cbSector.setSelectedItem(parts[14]);
			ps.setSector(parts[14]);
			
			cbIndustry.setSelectedItem(parts[15]);
			ps.setIndustry(parts[15]);
			
			chkNYSE.setSelected(Boolean.valueOf(parts[16]));
			ps.setNyse(Boolean.valueOf(parts[16]));
			
			chkNasdaq.setSelected(Boolean.valueOf(parts[17]));
			ps.setNasdaq(Boolean.valueOf(parts[17]));
			
			chkDJIA.setSelected(Boolean.valueOf(parts[18]));
			ps.setDjia(Boolean.valueOf(parts[18]));
			
			chkSP500.setSelected(Boolean.valueOf(parts[19]));
			ps.setSp500(Boolean.valueOf(parts[19]));
			
			chkETF.setSelected(Boolean.valueOf(parts[20]));
			ps.setEtf(Boolean.valueOf(parts[20]));
			
			chkBitcoin.setSelected(Boolean.valueOf(parts[21]));
			ps.setBitcoin(Boolean.valueOf(parts[21]));
			
			ps.setMapColor(cbBasedOn.getSelectedItem().toString());
			ps.setShowCellTooltips(chkShowCellToolTips.isSelected());
			ps.setShowStockTooltips(chkShowStockToolTips.isSelected());
			ps.setSmoothMap(chkSmooth.isSelected());
			ps.setMinNumResults(Integer.parseInt(txtMinNumResults.getText()));
			ps.setShowGroups(chkShowGroups.isSelected());
			
			txtMinNumResults.setText(new Integer(ps.getMinNumResults()).toString());
			chkSmooth.setSelected(ps.isSmoothMap());
			
			lblWhite.setText(pnlMapCell.getLegendMiddle());
			lblRed.setText(pnlMapCell.getLegendMin());
			lblGreen.setText(pnlMapCell.getLegendMax());
			lblMapBullishScore.setText(new Integer(bullishScore).toString());
			lblMapBearishScore.setText(new Integer(bearishScore).toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resetBlankMUTs() {
		for (int a = 0; a < NUM_MAP_THREADS; a++) {
			muts[a] = null;
			muts[a] = new MapUpdateThread();
		}
	}
	
	public void initializeGUIComponents() {
		panel = new JPanel();
		panel.setLayout(null);
		
		// MapCell Panel
	    pnlMapCell = getMapCellPanel();
		
		// Buy Criteria Panel
		pnlBuyCriteria = new JPanel();
		pnlBuyCriteria.setLayout(null);
		pnlBuyCriteria.setBounds(new Rectangle(2, 2, 240, 71));
		pnlBuyCriteria.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblBuyCriteria = new JLabel();
		lblBuyCriteria.setBounds(new Rectangle(0, 0, 240, 20));
		lblBuyCriteria.setBackground(colorSectionBackground);
		lblBuyCriteria.setForeground(colorSelectionForeground);
		lblBuyCriteria.setOpaque(true);
		lblBuyCriteria.setText(" Buy Criteria");
		lblBuyCriteria.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblBuyXAxis = new JLabel();
		lblBuyXAxis.setBounds(new Rectangle(3, 21, 80, 22));
		lblBuyXAxis.setText("X Axis");
		lblBuyXAxis.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblBuyYAxis = new JLabel();
		lblBuyYAxis.setBounds(new Rectangle(3, 45, 80, 22));
		lblBuyYAxis.setText("Y Axis");
		lblBuyYAxis.setFont(new Font("Dialog", Font.BOLD, 12));

		pnlBuyCriteria.add(lblBuyCriteria);
		pnlBuyCriteria.add(lblBuyXAxis);
		pnlBuyCriteria.add(getCbBuyXAxis());
		pnlBuyCriteria.add(lblBuyYAxis);
		pnlBuyCriteria.add(getCbBuyYAxis());
		
		// Sell Criteria Panel
		pnlSellCriteria = new JPanel();
		pnlSellCriteria.setLayout(null);
		pnlSellCriteria.setBounds(new Rectangle(2, 75, 240, 71));
		pnlSellCriteria.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblSellCriteria = new JLabel();
		lblSellCriteria.setBounds(new Rectangle(0, 0, 240, 20));
		lblSellCriteria.setBackground(colorSectionBackground);
		lblSellCriteria.setForeground(colorSelectionForeground);
		lblSellCriteria.setOpaque(true);
		lblSellCriteria.setText(" Sell Criteria");
		lblSellCriteria.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblMetric = new JLabel();
		lblMetric.setBounds(new Rectangle(3, 21, 80, 22));
		lblMetric.setText("Metric");
		lblMetric.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblValue = new JLabel();
		lblValue.setBounds(new Rectangle(130, 46, 80, 22));
		lblValue.setText("Value");
		lblValue.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblOperator = new JLabel();
		lblOperator.setBounds(new Rectangle(3, 46, 40, 22));
		lblOperator.setText("Op");
		lblOperator.setFont(new Font("Dialog", Font.BOLD, 12));

		pnlSellCriteria.add(lblSellCriteria);
		pnlSellCriteria.add(lblMetric);
		pnlSellCriteria.add(getCbMetric());
		pnlSellCriteria.add(lblValue);
		pnlSellCriteria.add(getTxtValue());
		pnlSellCriteria.add(lblOperator);
		pnlSellCriteria.add(getTxtOperator());
		
		// Stop Loss Panel
		pnlStopLoss = new JPanel();
		pnlStopLoss.setLayout(null);
		pnlStopLoss.setBounds(new Rectangle(2, 148, 240, 47));
		pnlStopLoss.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblStopLoss = new JLabel();
		lblStopLoss.setBounds(new Rectangle(0, 0, 240, 20));
		lblStopLoss.setBackground(colorSectionBackground);
		lblStopLoss.setForeground(colorSelectionForeground);
		lblStopLoss.setOpaque(true);
		lblStopLoss.setText(" Stop Loss");
		lblStopLoss.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblStopMetric = new JLabel();
		lblStopMetric.setBounds(new Rectangle(3, 21, 80, 22));
		lblStopMetric.setText("Metric");
		lblStopMetric.setFont(new Font("Dialog", Font.BOLD, 12));

		lblStopValue = new JLabel();
		lblStopValue.setBounds(new Rectangle(130, 21, 80, 23));
		lblStopValue.setText("Value");
		lblStopValue.setFont(new Font("Dialog", Font.BOLD, 12));
		
		pnlStopLoss.add(lblStopLoss);
		pnlStopLoss.add(lblStopMetric);
		pnlStopLoss.add(getCbStopMetric());
		pnlStopLoss.add(lblStopValue);
		pnlStopLoss.add(getTxtStopValue());
		
		// Period Panel
		pnlPeriod = new JPanel();
		pnlPeriod.setLayout(null);
		pnlPeriod.setBounds(new Rectangle(2, 197, 240, 47));
		pnlPeriod.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblPeriod = new JLabel();
		lblPeriod.setBounds(new Rectangle(0, 0, 240, 20));
		lblPeriod.setBackground(colorSectionBackground);
		lblPeriod.setForeground(colorSelectionForeground);
		lblPeriod.setOpaque(true);
		lblPeriod.setText(" Simulation Period");
		lblPeriod.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblFrom = new JLabel();
		lblFrom.setBounds(new Rectangle(3, 22, 80, 22));
		lblFrom.setText("From");
		lblFrom.setFont(new Font("Dialog", Font.BOLD, 12));

		lblTo = new JLabel();
		lblTo.setBounds(new Rectangle(130, 22, 80, 22));
		lblTo.setText("To");
		lblTo.setFont(new Font("Dialog", Font.BOLD, 12));
			
		pnlPeriod.add(lblPeriod);
		pnlPeriod.add(lblFrom);
		pnlPeriod.add(getCalFrom());
		pnlPeriod.add(lblTo);
		pnlPeriod.add(getCalTo());
		
		// Map Resolution Panel
		pnlResolution = new JPanel();
		pnlResolution.setLayout(null);
		pnlResolution.setBounds(new Rectangle(2, 246, 240, 47));
		pnlResolution.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblResolution = new JLabel();
		lblResolution.setBounds(new Rectangle(0, 0, 240, 20));
		lblResolution.setBackground(colorSectionBackground);
		lblResolution.setForeground(colorSelectionForeground);
		lblResolution.setOpaque(true);
		lblResolution.setText(" Map Resolution");
		lblResolution.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblXRes = new JLabel();
		lblXRes.setBounds(new Rectangle(3, 22, 80, 22));
		lblXRes.setText("X # Cells");
		lblXRes.setFont(new Font("Dialog", Font.BOLD, 12));

		lblYRes = new JLabel();
		lblYRes.setBounds(new Rectangle(100, 22, 80, 22));
		lblYRes.setText("Y # Cells");
		lblYRes.setFont(new Font("Dialog", Font.BOLD, 12));
		
		pnlResolution.add(lblResolution);
		pnlResolution.add(lblXRes);
		pnlResolution.add(getTxtXRes());
		pnlResolution.add(lblYRes);
		pnlResolution.add(getTxtYRes());

		// Map Options Panel
		pnlMapOptions = new JPanel();
		pnlMapOptions.setLayout(null);
		pnlMapOptions.setBounds(new Rectangle(2, 295, 240, 108));
		pnlMapOptions.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblMapOptions = new JLabel();
		lblMapOptions.setBounds(new Rectangle(0, 0, 240, 20));
		lblMapOptions.setBackground(colorSectionBackground);
		lblMapOptions.setForeground(colorSelectionForeground);
		lblMapOptions.setOpaque(true);
		lblMapOptions.setText(" Map Options");
		lblMapOptions.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblOption = new JLabel();
		lblOption.setBounds(new Rectangle(3, 22, 120, 22));
		lblOption.setText("Color");
		lblOption.setForeground(colorGreen);
		lblOption.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblMinNumResults = new JLabel();
		lblMinNumResults.setBounds(new Rectangle(3, 83, 80, 22));
		lblMinNumResults.setText("Min # Results");
		lblMinNumResults.setForeground(colorGreen);
		lblMinNumResults.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblFind = new JLabel();
		lblFind.setBounds(new Rectangle(130, 83, 44, 22));
		lblFind.setText("Find");
		lblFind.setForeground(colorGreen);
		lblFind.setFont(new Font("Dialog", Font.BOLD, 12));
		
		pnlMapOptions.add(lblMapOptions);
		pnlMapOptions.add(lblOption);
		pnlMapOptions.add(getCbBasedOn());
		pnlMapOptions.add(lblMinNumResults);
		pnlMapOptions.add(getTxtMinNumResults());
		pnlMapOptions.add(getChkShowCellToolTips());
		pnlMapOptions.add(getChkShowStockToolTips());
		pnlMapOptions.add(getChkSmooth());
		pnlMapOptions.add(getChkShowGroups());
		pnlMapOptions.add(lblFind);
		pnlMapOptions.add(getTxtFind());
		
		// Filters Panel
		pnlFilters = new JPanel();
		pnlFilters.setLayout(null);
		pnlFilters.setBounds(new Rectangle(2, 405, 240, 257));
		pnlFilters.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblFilters = new JLabel();
		lblFilters.setBounds(new Rectangle(0, 0, 240, 20));
		lblFilters.setBackground(colorSectionBackground);
		lblFilters.setForeground(colorSelectionForeground);
		lblFilters.setOpaque(true);
		lblFilters.setText(" Filters");
		lblFilters.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblMinLiquidity = new JLabel();
		lblMinLiquidity.setBounds(new Rectangle(3, 22, 80, 22));
		lblMinLiquidity.setText("Mn Liq");
		lblMinLiquidity.setFont(new Font("Dialog", Font.BOLD, 12));
		
		lblMaxVolatility = new JLabel();
		lblMaxVolatility.setBounds(new Rectangle(98, 22, 80, 22));
		lblMaxVolatility.setText("Mx Vol");
		lblMaxVolatility.setFont(new Font("Dialog", Font.BOLD, 12));
			
		lblMinPrice = new JLabel();
		lblMinPrice.setBounds(new Rectangle(167, 22, 60, 22));
		lblMinPrice.setText("Mn Prc");
		lblMinPrice.setFont(new Font("Dialog", Font.BOLD, 12));
			
		lblSector = new JLabel();
		lblSector.setBounds(new Rectangle(3, 56, 80, 22));
		lblSector.setText("Sector");
		lblSector.setFont(new Font("Dialog", Font.BOLD, 12));

		lblIndustry = new JLabel();
		lblIndustry.setBounds(new Rectangle(3, 70, 80, 22));
		lblIndustry.setText("Industry");
		lblIndustry.setFont(new Font("Dialog", Font.BOLD, 12));

		pnlFilters.add(lblFilters);
		pnlFilters.add(lblMinLiquidity);
		pnlFilters.add(getTxtMinLiquidity());
		pnlFilters.add(lblMaxVolatility);
		pnlFilters.add(getTxtMaxVolatility());
		pnlFilters.add(lblMinPrice);
		pnlFilters.add(getTxtMinPrice());
		pnlFilters.add(lblSector);
		pnlFilters.add(getCbSector());
		pnlFilters.add(lblIndustry);
		pnlFilters.add(getCbIndustry());
		pnlFilters.add(getChkNYSE());
		pnlFilters.add(getChkNasdaq());
		pnlFilters.add(getChkDJIA());
		pnlFilters.add(getChkSP500());
		pnlFilters.add(getChkETF());
		pnlFilters.add(getChkBitcoin());
		pnlFilters.add(getListScroller());
		
		// Legend Panel
		pnlLegend = new JPanel();
		pnlLegend.setLayout(null);
		pnlLegend.setBounds(new Rectangle(244, 664, 230, 34));
		pnlLegend.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblBlue = new JLabel();
		lblBlue.setBounds(new Rectangle(3, 16, 90, 18));
		lblBlue.setText("Lacking Data");
		lblBlue.setFont(new Font("Dialog", Font.BOLD, 10));
		
		pnlBlue1 = new JPanel();
		pnlBlue1.setLayout(null);
		pnlBlue1.setBounds(new Rectangle(3, 3, 16, 16));
		pnlBlue1.setBackground(colorBlue1);
		pnlBlue1.setOpaque(true);
		
		pnlBlue2 = new JPanel();
		pnlBlue2.setLayout(null);
		pnlBlue2.setBounds(new Rectangle(19, 3, 16, 16));
		pnlBlue2.setBackground(colorBlue2);
		pnlBlue2.setOpaque(true);
		
		pnlBlue3 = new JPanel();
		pnlBlue3.setLayout(null);
		pnlBlue3.setBounds(new Rectangle(35, 3, 16, 16));
		pnlBlue3.setBackground(colorBlue3);
		pnlBlue3.setOpaque(true);
		
		pnlBlue4 = new JPanel();
		pnlBlue4.setLayout(null);
		pnlBlue4.setBounds(new Rectangle(51, 3, 16, 16));
		pnlBlue4.setBackground(colorBlue4);
		pnlBlue4.setOpaque(true);

		lblRed = new JLabel();
		lblRed.setBounds(new Rectangle(83, 16, 57, 18));
		lblRed.setText("-1.00");
		lblRed.setFont(new Font("Dialog", Font.BOLD, 10));
		
		pnlRed1 = new JPanel();
		pnlRed1.setLayout(null);
		pnlRed1.setBounds(new Rectangle(83, 3, 16, 16));
		pnlRed1.setBackground(colorRed1);
		pnlRed1.setOpaque(true);
		
		pnlRed2 = new JPanel();
		pnlRed2.setLayout(null);
		pnlRed2.setBounds(new Rectangle(99, 3, 16, 16));
		pnlRed2.setBackground(colorRed2);
		pnlRed2.setOpaque(true);
		
		pnlRed3 = new JPanel();
		pnlRed3.setLayout(null);
		pnlRed3.setBounds(new Rectangle(115, 3, 16, 16));
		pnlRed3.setBackground(colorRed3);
		pnlRed3.setOpaque(true);
		
		pnlRed4 = new JPanel();
		pnlRed4.setLayout(null);
		pnlRed4.setBounds(new Rectangle(131, 3, 16, 16));
		pnlRed4.setBackground(colorRed4);
		pnlRed4.setOpaque(true);
		
		lblWhite = new JLabel();
		lblWhite.setBounds(new Rectangle(140, 16, 28, 18));
		lblWhite.setText("0.0");
		lblWhite.setHorizontalAlignment(SwingConstants.CENTER);
		lblWhite.setFont(new Font("Dialog", Font.BOLD, 10));
		
		pnlWhite = new JPanel();
		pnlWhite.setLayout(null);
		pnlWhite.setBounds(new Rectangle(147, 3, 16, 16));
		pnlWhite.setBackground(Color.WHITE);
		pnlWhite.setOpaque(true);
		
		lblGreen = new JLabel();
		lblGreen.setBounds(new Rectangle(170, 16, 57, 18));
		lblGreen.setText("1.00");
		lblGreen.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGreen.setFont(new Font("Dialog", Font.BOLD, 10));
		
		pnlGreen1 = new JPanel();
		pnlGreen1.setLayout(null);
		pnlGreen1.setBounds(new Rectangle(163, 3, 16, 16));
		pnlGreen1.setBackground(colorGreen1);
		pnlGreen1.setOpaque(true);
		
		pnlGreen2 = new JPanel();
		pnlGreen2.setLayout(null);
		pnlGreen2.setBounds(new Rectangle(179, 3, 16, 16));
		pnlGreen2.setBackground(colorGreen2);
		pnlGreen2.setOpaque(true);
		
		pnlGreen3 = new JPanel();
		pnlGreen3.setLayout(null);
		pnlGreen3.setBounds(new Rectangle(195, 3, 16, 16));
		pnlGreen3.setBackground(colorGreen3);
		pnlGreen3.setOpaque(true);
		
		pnlGreen4 = new JPanel();
		pnlGreen4.setLayout(null);
		pnlGreen4.setBounds(new Rectangle(211, 3, 16, 16));
		pnlGreen4.setBackground(colorGreen4);
		pnlGreen4.setOpaque(true);
		
		pnlLegend.add(lblBlue);
		pnlLegend.add(pnlBlue1);
		pnlLegend.add(pnlBlue2);
		pnlLegend.add(pnlBlue3);
		pnlLegend.add(pnlBlue4);
		pnlLegend.add(lblRed);
		pnlLegend.add(pnlRed1);
		pnlLegend.add(pnlRed2);
		pnlLegend.add(pnlRed3);
		pnlLegend.add(pnlRed4);
		pnlLegend.add(lblWhite);
		pnlLegend.add(pnlWhite);
		pnlLegend.add(lblGreen);
		pnlLegend.add(pnlGreen1);
		pnlLegend.add(pnlGreen2);
		pnlLegend.add(pnlGreen3);
		pnlLegend.add(pnlGreen4);
		
		// Eval panel
		pnlEval = new JPanel();
		pnlEval.setLayout(null);
		pnlEval.setBounds(new Rectangle(576, 664, 328, 34));
		pnlEval.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		lblMapBullishScore = new JLabel();
		lblMapBullishScore.setBounds(new Rectangle(66, 1, 90, 18));
		lblMapBullishScore.setText("Bullish Score");
		lblMapBullishScore.setForeground(colorGreen);
		lblMapBullishScore.setFont(new Font("Dialog", Font.BOLD, 13));
		
		lblMapBearishScore = new JLabel();
		lblMapBearishScore.setBounds(new Rectangle(66, 16, 90, 18));
		lblMapBearishScore.setText("Bearish Score");
		lblMapBearishScore.setForeground(Color.RED);
		lblMapBearishScore.setFont(new Font("Dialog", Font.BOLD, 13));
		
		pnlEval.add(lblMapBullishScore);
		pnlEval.add(lblMapBearishScore);
		pnlEval.add(getBtnEvalMap());
		pnlEval.add(getBtnTrackRealtime());
		
		// Add everything to main panel
		panel.add(pnlBuyCriteria);
		panel.add(pnlSellCriteria);
		panel.add(pnlStopLoss);
		panel.add(pnlPeriod);
		panel.add(pnlResolution);
		panel.add(pnlMapOptions);
		panel.add(pnlFilters);
		panel.add(getBtnBuildMap());
		panel.add(pnlMapCell);
		panel.add(pnlLegend);
		panel.add(pnlEval);
	}
	
	public JFrame getJFrame() {
		if (frame == null) {
			frame = new JFrame();
			frame.setSize(new Dimension(922, 738));
			frame.setTitle("Chip Swinger Championship Stock Picker .42");
			frame.setContentPane(panel);
			frame.setVisible(true);
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					try {
						// Stop any running map threads
						for (MapUpdateThread mut:muts) {
							if (mut != null && mut.isRunning()) {
								mut.setRunning(false);
								mut.join();
							}
						}
						
						// Stop the realtime tracker thread
						if (rtt != null && rtt.isRunning()) {
							rtt.setRunning(false);
							rtt.join();
						}

						mapWorker.cancel(true);
						frame.dispose();
						System.exit(0);
					}
					catch (Exception ex) {
						ex.printStackTrace();
						frame.dispose();
						System.exit(0);
					}
				}
			});
		}
		return frame;
	}
	
	public JCheckBox getChkSmooth() {
		chkSmooth = new JCheckBox("Smooth Map");
		chkSmooth.setBounds(new Rectangle(142, 45, 96, 18));
		chkSmooth.setFont(new Font("Dialog", Font.BOLD, 12));
		chkSmooth.setForeground(colorGreen);
		chkSmooth.setHorizontalTextPosition(SwingConstants.LEFT);
		chkSmooth.setSelected(true);
		ps.setSmoothMap(chkSmooth.isSelected());
		chkSmooth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setSmoothMap(chkSmooth.isSelected());
				pnlMapCell.updateUI();
				lblWhite.setText(pnlMapCell.getLegendMiddle());
				lblRed.setText(pnlMapCell.getLegendMin());
				lblGreen.setText(pnlMapCell.getLegendMax());
			}
		});
		
		return chkSmooth;
	}
	
	public JCheckBox getChkShowCellToolTips() {
		chkShowCellToolTips = new JCheckBox("Show Cell Tooltips    ");
		chkShowCellToolTips.setBounds(new Rectangle(-1, 45, 138, 18));
		chkShowCellToolTips.setHorizontalAlignment(SwingConstants.RIGHT);
		chkShowCellToolTips.setFont(new Font("Dialog", Font.BOLD, 12));
		chkShowCellToolTips.setMargin(new Insets(0, 0, 0, 0));
		chkShowCellToolTips.setOpaque(false);
		chkShowCellToolTips.setForeground(colorGreen);
		chkShowCellToolTips.setHorizontalTextPosition(SwingConstants.LEFT);
		chkShowCellToolTips.setSelected(true);
		ps.setShowCellTooltips(chkShowCellToolTips.isSelected());
		chkShowCellToolTips.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setShowCellTooltips(chkShowCellToolTips.isSelected());
				pnlMapCell.updateUI();
			}
		});
		return chkShowCellToolTips;
	}
	
	public JCheckBox getChkShowStockToolTips() {
		chkShowStockToolTips = new JCheckBox("Show Stock Tooltips");
		chkShowStockToolTips.setBounds(new Rectangle(-1, 65, 138, 18));
		chkShowStockToolTips.setHorizontalAlignment(SwingConstants.RIGHT);
		chkShowStockToolTips.setFont(new Font("Dialog", Font.BOLD, 12));
		chkShowStockToolTips.setMargin(new Insets(0, 0, 0, 0));
		chkShowStockToolTips.setOpaque(false);
		chkShowStockToolTips.setForeground(colorGreen);
		chkShowStockToolTips.setHorizontalTextPosition(SwingConstants.LEFT);
		chkShowStockToolTips.setSelected(true);
		ps.setShowStockTooltips(chkShowStockToolTips.isSelected());
		chkShowStockToolTips.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setShowStockTooltips(chkShowStockToolTips.isSelected());
				pnlMapCell.updateUI();
			}
		});
		return chkShowStockToolTips;
	}
	
	public JCheckBox getChkShowGroups() {
		chkShowGroups = new JCheckBox("Show Groups");
		chkShowGroups.setBounds(new Rectangle(137, 65, 102, 18));
		chkShowGroups.setFont(new Font("Dialog", Font.BOLD, 12));
		chkShowGroups.setForeground(colorGreen);
		chkShowGroups.setHorizontalTextPosition(SwingConstants.LEFT);
		chkShowGroups.setSelected(true);
		ps.setShowGroups(chkShowGroups.isSelected());
		chkShowGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setShowGroups(chkShowGroups.isSelected());
				pnlMapCell.updateUI();
			}
		});
		
		return chkShowGroups;
	}
	
	public JComboBox getCbBasedOn() {
		cbBasedOn = new JComboBox(Constants.MAP_COLOR_OPTIONS.toArray());
		cbBasedOn.setSelectedItem(Constants.MAP_COLOR_OPTION_ALL_GEOMEAN_PER_BAR);
		cbBasedOn.setBounds(new Rectangle(58, 22, 179, 22));
		cbBasedOn.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setMapColor(cbBasedOn.getSelectedItem().toString());
		cbBasedOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        ps.setMapColor(cbBasedOn.getSelectedItem().toString());
		        pnlMapCell.updateUI();
				lblWhite.setText(pnlMapCell.getLegendMiddle());
				lblRed.setText(pnlMapCell.getLegendMin());
				lblGreen.setText(pnlMapCell.getLegendMax());	
		    }
		});
		
		return cbBasedOn;
	}
	
	public JComboBox getCbBuyXAxis() {
		cbBuyXAxis = new JComboBox(Constants.METRICS.toArray());
		cbBuyXAxis.setSelectedItem("rsi2");
		cbBuyXAxis.setBounds(new Rectangle(44, 22, 193, 22));
		cbBuyXAxis.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setxAxisMetric(cbBuyXAxis.getSelectedItem().toString());
		
		cbBuyXAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setxAxisMetric(cbBuyXAxis.getSelectedItem().toString());
		    }
		});
		
		return cbBuyXAxis;
	}
	
	public JComboBox getCbBuyYAxis() {
		cbBuyYAxis = new JComboBox(Constants.METRICS.toArray());
		cbBuyYAxis.setSelectedItem("rsi14");
		cbBuyYAxis.setBounds(new Rectangle(44, 46, 193, 22));
		cbBuyYAxis.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setyAxisMetric(cbBuyYAxis.getSelectedItem().toString());
		
		cbBuyYAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setyAxisMetric(cbBuyYAxis.getSelectedItem().toString());
		    }
		});
		
		return cbBuyYAxis;
	}

	public JComboBox getCbMetric() {		
		ArrayList<String> sellMetrics = new ArrayList<String>();
		sellMetrics.addAll(Constants.METRICS);
		sellMetrics.addAll(Constants.OTHER_SELL_METRICS);
		Collections.sort(sellMetrics);
		cbMetric = new JComboBox(sellMetrics.toArray());
		cbMetric.setSelectedItem("consecutiveups");
		cbMetric.setBounds(new Rectangle(44, 22, 193, 22));
		cbMetric.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setSellMetric(cbMetric.getSelectedItem().toString());
		
		cbMetric.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setSellMetric(cbMetric.getSelectedItem().toString());
		    }
		});
		
		return cbMetric;
	}
	
	private JComboBox getCbStopMetric() {
		cbStopMetric = new JComboBox(Constants.STOP_METRICS.toArray());
		cbStopMetric.setSelectedItem("None");
		cbStopMetric.setBounds(new Rectangle(44, 22, 80, 22));
		cbStopMetric.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setStopMetric(cbStopMetric.getSelectedItem().toString());
		
		cbStopMetric.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setStopMetric(cbStopMetric.getSelectedItem().toString());
		    }
		});
		
		return cbStopMetric;
	}
	
	private JComboBox getCbSector() {
		cbSector = new JComboBox(QueryManager.getSectorList().toArray());
		cbSector.setSelectedItem("All");
		cbSector.setBounds(new Rectangle(58, 46, 179, 22));
		cbSector.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setSector(cbSector.getSelectedItem().toString());
		
		cbSector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setSector(cbSector.getSelectedItem().toString());
		    }
		});
		
		return cbSector;
	}
	
	private JComboBox getCbIndustry() {
		cbIndustry = new JComboBox(QueryManager.getIndustryList().toArray());
		cbIndustry.setSelectedItem("All");
		cbIndustry.setBounds(new Rectangle(58, 70, 179, 22));
		cbIndustry.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setIndustry(cbIndustry.getSelectedItem().toString());
		
		cbIndustry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setIndustry(cbIndustry.getSelectedItem().toString());
		    }
		});
		
		return cbIndustry;
	}
	
	private JDateChooser getCalFrom() {
		Calendar cFrom = Calendar.getInstance();
		cFrom.add(Calendar.MONTH, -3);
		calFrom = new JDateChooser();
		calFrom.setBounds(new Rectangle(36, 22, 90, 23));
		calFrom.setDate(cFrom.getTime());
		calFrom.setDateFormatString("MM/dd/yyyy");
		calFrom.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setFromCal(cFrom);
		
		calFrom.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				ps.setFromCal(calFrom.getCalendar());
		    }
		});
		
		return calFrom;
	}
	
	private JDateChooser getCalTo() {
		Calendar cTo = Calendar.getInstance();
		calTo = new JDateChooser();
		calTo.setBounds(new Rectangle(147, 22, 90, 23));
		calTo.setDate(cTo.getTime());
		calTo.setDateFormatString("MM/dd/yyyy");
		calTo.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setToCal(cTo);
		
		calTo.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				ps.setToCal(calTo.getCalendar());
		    }
		});
		
		return calTo;
	}
	
	private JButton getBtnBuildMap() {
		if (btnBuildMap == null) {
			btnBuildMap = new JButton();
			btnBuildMap.setBounds(new Rectangle(2, 664, 240, 34));
			btnBuildMap.setText("Run Simulation / Build Map");
			btnBuildMap.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					try {
						lblMapBullishScore.setText("Bullish Score");
						lblMapBearishScore.setText("Bearish Score");
						
						// Do any cleanup on the input if needed
						if (cbStopMetric.getSelectedItem().equals("None")) {
							txtStopValue.setText("0");
							ps.setStopValue(0f);
						}
						
						// Stop any running map threads
						for (int a = 0; a < NUM_MAP_THREADS; a++) {
							if (muts[a] != null && muts[a].isRunning()) {
								muts[a].setRunning(false);
								muts[a].join();
							}
						}
						resetBlankMUTs();
						MUTCoordinator.getInstance().clearCells();
						
						MapSymbolSingleton.getInstance().setMapSymbols(new ArrayList<MapSymbol>());
						MapCellSingleton mcs = MapCellSingleton.getInstance();
						mcs.setMapCells(new ArrayList<MapCell>());
						mcs.setMapCellsSmoothed(new ArrayList<MapCell>());
						
						pnlMapCell.setDefaultWorldView();
						pnlMapCell.setCurrentWorldView();
						pnlMapCell.zoom(-83.5);
						pnlMapCell.panWorldViewToCellsLocation();
						
						mapWorker = new MapWorker();
						mapWorker.execute();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		return btnBuildMap;
	}
	
	private JButton getBtnEvalMap() {
		if (btnEvalMap == null) {
			btnEvalMap = new JButton();
			btnEvalMap.setBounds(new Rectangle(3, 3, 58, 28));
			btnEvalMap.setMargin(new Insets(0,0,0,0));
			btnEvalMap.setText("Eval Map");
			btnEvalMap.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					try {
						// Evaluate, update GUI, then save
						synchronized(pnlMapCell) {
							while (!mapWorker.done) {
								System.out.println("Waiting for MapWorker to finish.  Sleeping for 500ms");
								Thread.sleep(500);
							}
							MapEvaluatorSingleton mes = MapEvaluatorSingleton.getInstance();
							HashMap<String, Float> results = mes.evaluate(pnlMapCell);
							
							float bullScore = 0;
							Float bullScoreF = results.get("bull");
							if (bullScoreF != null)
								bullScore = bullScoreF.floatValue();
							
							float bearScore = 0;
							Float bearScoreF = results.get("bear");
							if (bearScoreF != null)
								bearScore = bearScoreF.floatValue();
							
							lblMapBullishScore.setText(new Integer(new Float(bullScore).intValue()).toString());
							lblMapBearishScore.setText(new Integer(new Float(bearScore).intValue()).toString());
							ScoreSingleton ss = ScoreSingleton.getInstance();
							ss.setBullScore(bullScore);
							ss.setBearScore(bearScore);
							pnlMapCell.setMapBullishScore((int)bullScore);
							pnlMapCell.setMapBearishScore((int)bearScore);
							pnlMapCell.repaint();
							int rBullScore = (int)Precision.round(bullScore, -2);
							int rBearScore = (int)Precision.round(bearScore, -2);
							saveMapToDisk(new Integer(rBullScore).toString(), new Integer(rBearScore).toString());
							QueryManager.saveSearchResults(new Float(bullScore), new Float(bearScore));
							ps.setRunFinished(true);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		return btnEvalMap;
	}
	
	private JToggleButton getBtnTrackRealtime() {
		if (btnTrackRealtime == null) {
			btnTrackRealtime = new JToggleButton();
			btnTrackRealtime.setBounds(new Rectangle(267, 3, 58, 28));
			btnTrackRealtime.setMargin(new Insets(0,0,0,0));
			btnTrackRealtime.setText("Track");
			btnTrackRealtime.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					try {
						AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				        boolean selected = abstractButton.getModel().isSelected();
						
				        if (selected) {
				        	btnTrackRealtime.setText("Tracking");
				        	
				        	// Stop anything that might already be running
				        	if (rtt != null && rtt.isRunning()) {
								rtt.setRunning(false);
								rtt.join();
							}
				        	
				        	// Start the realtime tracking thread
//				        	MapSymbolSingleton.getInstance().setMapSymbols(QueryManager.getMapSymbols());

				        	rttWorker = new RealtimeTrackerWorker();
							rttWorker.execute();
				        }
				        else {
				        	btnTrackRealtime.setText("Track");
				        	
				        	// Stop anything that might be running
				        	if (rtt != null && rtt.isRunning()) {
				        		rtt.setRunning(false);
				        		rtt.join();
				        	}
				        	
//				        	MapSymbolSingleton.getInstance().setMapSymbols(new ArrayList<MapSymbol>());
//				        	MapSymbolSingleton.getInstance().setHighPriorityMapSymbols(new ArrayList<MapSymbol>());
//				        	pnlMapCell.updateUI();
				        }
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		return btnTrackRealtime;
	}
	
	private JTextField getTxtFind() {
		txtFind = new JTextField("");
		txtFind.setBounds(new Rectangle(156, 83, 82, 23));
		txtFind.setAlignmentY(.5f);
		txtFind.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setFind(txtFind.getText());
		txtFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setFind(txtFind.getText());
		        pnlMapCell.updateUI();
		    }
		});
		
		return txtFind;
	}
	
	private JTextField getTxtMinNumResults() {
		txtMinNumResults = new JTextField("200");
		txtMinNumResults.setBounds(new Rectangle(82, 83, 40, 23));
		txtMinNumResults.setAlignmentY(.5f);
		txtMinNumResults.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setMinNumResults(new Integer(txtMinNumResults.getText()));
		txtMinNumResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ps.setMinNumResults(new Integer(txtMinNumResults.getText()));
		        pnlMapCell.updateUI();
		        lblWhite.setText(pnlMapCell.getLegendMiddle());
				lblRed.setText(pnlMapCell.getLegendMin());
				lblGreen.setText(pnlMapCell.getLegendMax());
		    }
		});
		
		return txtMinNumResults;
	}
	
	private JTextField getTxtMinLiquidity() {
		txtMinLiquidity = new JTextField("1"); // 2000000 Stocks
		txtMinLiquidity.setBounds(new Rectangle(42, 22, 54, 23));
		txtMinLiquidity.setAlignmentY(.5f);
		txtMinLiquidity.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setMinLiquidity(new Integer(txtMinLiquidity.getText()));
		txtMinLiquidity.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (CalcUtils.isInteger(txtMinLiquidity.getText()))
					ps.setMinLiquidity(new Integer(txtMinLiquidity.getText()));
			}
		});
		
		return txtMinLiquidity;
	}
	
	private JTextField getTxtMaxVolatility() {
		txtMaxVolatility = new JTextField("5"); // 1.0 Stocks
		txtMaxVolatility.setBounds(new Rectangle(138, 22, 26, 23));
		txtMaxVolatility.setAlignmentY(.5f);
		txtMaxVolatility.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setMaxVolatility(new Float(txtMaxVolatility.getText()));
		txtMaxVolatility.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (CalcUtils.isFloat(txtMaxVolatility.getText()))
					ps.setMaxVolatility(new Float(txtMaxVolatility.getText()));
			}
		});
		
		return txtMaxVolatility;
	}
	
	private JTextField getTxtMinPrice() {
		txtMinPrice = new JTextField("0.01");
		txtMinPrice.setBounds(new Rectangle(208, 22, 30, 23));
		txtMinPrice.setAlignmentY(.5f);
		txtMinPrice.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setMinPrice(new Float(txtMinPrice.getText()));
		txtMinPrice.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (CalcUtils.isFloat(txtMinPrice.getText()))
					ps.setMinPrice(new Float(txtMinPrice.getText()));
			}
		});
		
		return txtMinPrice;
	}
	
	public JTextField getTxtValue() {
		txtValue = new JTextField("2");
		txtValue.setBounds(new Rectangle(167, 46, 71, 23));
		txtValue.setAlignmentY(.5f);
		txtValue.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setSellValue(new Float(txtValue.getText()));
		txtValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (CalcUtils.isFloat(txtValue.getText()))
					ps.setSellValue(new Float(txtValue.getText()));
			}
		});
		
		return txtValue;
	}
	
	public JTextField getTxtOperator() {
		txtOperator = new JTextField(">=");
		txtOperator.setBounds(new Rectangle(44, 46, 22, 23));
		txtOperator.setAlignmentY(.5f);
		txtOperator.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setSellOperator(txtOperator.getText());
		txtOperator.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				ps.setSellOperator(txtOperator.getText());
			}
		});
		
		return txtOperator;
	}
	
	private JTextField getTxtStopValue() {
		txtStopValue = new JTextField();
		txtStopValue.setBounds(new Rectangle(167, 22, 71, 23));
		txtStopValue.setAlignmentY(.5f);
		txtStopValue.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setStopValue(null);
		txtStopValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (txtStopValue.getText() != null && !txtStopValue.getText().equals("")) {
					if (CalcUtils.isFloat(txtStopValue.getText()))
						ps.setStopValue(new Float(txtStopValue.getText()));
				}
				else {
					ps.setStopValue(null);
				}
			}
		});
		
		
		return txtStopValue;
	}
	
	private JTextField getTxtXRes() {
		txtXRes = new JTextField("20");
		txtXRes.setBounds(new Rectangle(58, 22, 32, 23));
		txtXRes.setAlignmentY(.5f);
		txtXRes.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setxRes(new Integer(txtXRes.getText()));
		txtXRes.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (CalcUtils.isInteger(txtXRes.getText()))
					ps.setxRes(new Integer(txtXRes.getText()));
			}
		});
		
		return txtXRes;
	}
	
	private JTextField getTxtYRes() {
		txtYRes = new JTextField("20");
		txtYRes.setBounds(new Rectangle(155, 22, 32, 23));
		txtYRes.setAlignmentY(.5f);
		txtYRes.setFont(new Font("Dialog", Font.BOLD, 12));
		ps.setyRes(new Integer(txtYRes.getText()));
		txtYRes.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			private void update() {
				if (CalcUtils.isInteger(txtYRes.getText()))
					ps.setyRes(new Integer(txtYRes.getText()));
			}
		});
		
		return txtYRes;
	}
	
	private JScrollPane getListScroller() {
		listScroller = new JScrollPane(getListSymbols());
		listScroller.setBounds(new Rectangle(3, 132, 235, 123));
		
		return listScroller;
	}
	
	private JList getListSymbols() {
		listSymbols = new JList();
		listSymbols.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listSymbols.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		listSymbols.setVisibleRowCount(-1);
		
		indexFilterList.clear();
		if (chkNYSE.isSelected()) {
			indexFilterList.add("NYSE");
		}
		if (chkNasdaq.isSelected()) {
			indexFilterList.add("Nasdaq");
		}
		if (chkDJIA.isSelected()) {
			indexFilterList.add("DJIA");
		}
		if (chkSP500.isSelected()) {
			indexFilterList.add("SP500");
		}
		if (chkETF.isSelected()) {
			indexFilterList.add("ETF");
		}
		if (chkBitcoin.isSelected()) {
			indexFilterList.add("Bitcoin");
		}
		
		ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
		listSymbols.setListData(symbols.toArray());
	    ps.setSymbols(new ArrayList<String>());
		
		listSymbols.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
			    ArrayList<String> selectedTextList = (ArrayList<String>)listSymbols.getSelectedValuesList();
			    ArrayList<String> selectedSymbols = new ArrayList<String>();
			    
			    if (selectedTextList != null) {
			    	for (String selectedText : selectedTextList) {
			    		String[] pieces1 = selectedText.split(" - ");
			    		String duration = pieces1[0];
			    		String[] pieces2 = pieces1[1].split(" \\(");
			    		String symbol = pieces2[0];
			    		selectedSymbols.add(duration + " - " + symbol);
			    	}
			    }
			    ps.setSymbols(selectedSymbols);
			}
		});
		
		return listSymbols;
	}
	
	private MapCellPanel getMapCellPanel() {
		MapCellPanel mcp = new MapCellPanel(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, this);
		mcp.setBounds(new Rectangle(244, 2, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
		mcp.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
		mcp.setSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
		mcp.setBackground(Color.WHITE);
		mcp.setDoubleBuffered(true);
		mcp.setLayout(null);
		mcp.setVisible(true);
		mcp.setDefaultWorldView();
		mcp.setCurrentWorldView();
		mcp.zoom(-83.5);
		mcp.resetBuffer();
		mcp.repaint();
		return mcp;
	}
	
	private String saveMapToDisk(String bullishScore, String bearishScore) {
		try {
			StringBuilder sb = new StringBuilder();
			synchronized (pnlMapCell) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
				sb.append(ps.getxAxisMetric() + " ");
				sb.append(ps.getyAxisMetric() + " ");
				sb.append(ps.getSellMetric() + " ");
				String sellOperator = ps.getSellOperator();
				if (sellOperator.equals("<=")) {
					sellOperator = "LTE";
				}
				if (sellOperator.equals(">=")) {
					sellOperator = "GTE";
				}
				sb.append(sellOperator + " " );
				sb.append(new Float(ps.getSellValue()) + " ");
				sb.append(ps.getStopMetric() + " ");
				String stopValue = "\"\"";
				if (ps.getStopValue() != null) {
					stopValue = new Float(ps.getStopValue()).toString();
				}
				if (stopValue == null || stopValue.equals("")) {
					stopValue = "\"\"";
				}
				sb.append(stopValue + " ");
				sb.append(sdf.format(ps.getFromCal().getTime()) + " ");
				sb.append(sdf.format(ps.getToCal().getTime()) + " ");
				sb.append(new Integer(ps.getxRes()) + " ");
				sb.append(new Integer(ps.getyRes()) + " ");
				sb.append(new Integer(ps.getMinLiquidity()) + " ");
				sb.append(new Float(ps.getMaxVolatility()) + " ");
				sb.append(new Float(ps.getMinPrice()) + " ");
				sb.append(ps.getSector() + " ");
				sb.append(ps.getIndustry() + " ");
				sb.append(ps.isNyse() + " ");
				sb.append(ps.isNasdaq() + " ");
				sb.append(ps.isDjia() + " ");
				sb.append(ps.isSp500() + " ");
				sb.append(ps.isEtf() + " ");
				sb.append(ps.isBitcoin());
	
				pnlMapCell.saveMaps(sb.toString(), bullishScore, bearishScore);
			}
			return sb.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public JCheckBox getChkNYSE() {
		chkNYSE = new JCheckBox("NYSE");
		chkNYSE.setBounds(new Rectangle(1, 94, 55, 20));
		chkNYSE.setFont(new Font("Dialog", Font.BOLD, 12));
		chkNYSE.setSelected(false);
		ps.setNyse(chkNYSE.isSelected());
		
		chkNYSE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setNyse(chkNYSE.isSelected());
				if (chkNYSE.isSelected()) {
					indexFilterList.add("NYSE");
				}
				else {
					indexFilterList.remove("NYSE");
				}
				ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
				listSymbols.setListData(symbols.toArray());
			}
		});
		
		return chkNYSE;
	}

	public JCheckBox getChkNasdaq() {
		chkNasdaq = new JCheckBox("Nasdaq");
		chkNasdaq.setBounds(new Rectangle(80, 94, 68, 20));
		chkNasdaq.setFont(new Font("Dialog", Font.BOLD, 12));
		chkNasdaq.setSelected(false);
		ps.setNasdaq(chkNasdaq.isSelected());
		
		chkNasdaq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setNasdaq(chkNasdaq.isSelected());
				if (chkNasdaq.isSelected()) {
					indexFilterList.add("Nasdaq");
				}
				else {
					indexFilterList.remove("Nasdaq");
				}
				ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
				listSymbols.setListData(symbols.toArray());
			}
		});
		
		return chkNasdaq;
	}

	public JCheckBox getChkDJIA() {
		chkDJIA = new JCheckBox("DJIA");
		chkDJIA.setBounds(new Rectangle(160, 94, 51, 20));
		chkDJIA.setFont(new Font("Dialog", Font.BOLD, 12));
		chkDJIA.setSelected(false);
		ps.setDjia(chkDJIA.isSelected());
		
		chkDJIA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setDjia(chkDJIA.isSelected());
				if (chkDJIA.isSelected()) {
					indexFilterList.add("DJIA");
				}
				else {
					indexFilterList.remove("DJIA");
				}
				ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
				listSymbols.setListData(symbols.toArray());
			}
		});
		
		return chkDJIA;
	}

	public JCheckBox getChkSP500() {
		chkSP500 = new JCheckBox("SP500");
		chkSP500.setBounds(new Rectangle(1, 112, 66, 20));
		chkSP500.setFont(new Font("Dialog", Font.BOLD, 12));
		chkSP500.setSelected(false);
		ps.setSp500(chkSP500.isSelected());
		
		chkSP500.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setSp500(chkSP500.isSelected());
				if (chkSP500.isSelected()) {
					indexFilterList.add("SP500");
				}
				else {
					indexFilterList.remove("SP500");
				}
				ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
				listSymbols.setListData(symbols.toArray());
			}
		});
		
		return chkSP500;
	}

	public JCheckBox getChkETF() {
		chkETF = new JCheckBox("ETF");
		chkETF.setBounds(new Rectangle(80, 112, 45, 20));
		chkETF.setFont(new Font("Dialog", Font.BOLD, 12));
		chkETF.setSelected(false);
		ps.setEtf(chkETF.isSelected());
		
		chkETF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setEtf(chkETF.isSelected());
				if (chkETF.isSelected()) {
					indexFilterList.add("ETF");
				}
				else {
					indexFilterList.remove("ETF");
				}
				ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
				listSymbols.setListData(symbols.toArray());
			}
		});
		
		return chkETF;
	}

	public JCheckBox getChkBitcoin() {
		chkBitcoin = new JCheckBox("Bitcoin");
		chkBitcoin.setBounds(new Rectangle(160, 112, 75, 20));
		chkBitcoin.setFont(new Font("Dialog", Font.BOLD, 12));
		chkBitcoin.setSelected(true);
		ps.setBitcoin(chkBitcoin.isSelected());
		
		chkBitcoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ps.setBitcoin(chkBitcoin.isSelected());
				if (chkBitcoin.isSelected()) {
					indexFilterList.add("Bitcoin");
				}
				else {
					indexFilterList.remove("Bitcoin");
				}
				ArrayList<String> symbols = QueryManager.getDistinctSymbolDurations(indexFilterList);
				listSymbols.setListData(symbols.toArray());
			}
		});
		
		return chkBitcoin;
	}

	/**
	 * Inner class to do all the realtime symbol tracking on the map
	 * @author zottower
	 *
	 */
	public class RealtimeTrackerWorker extends SwingWorker<Void, HashMap<String, String>> {

		RealtimeTrackerThreadResultSetter setter = new RealtimeTrackerThreadResultSetter() {
			public synchronized void setResult(HashMap<String, String> result) {
				ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
				results.add(result);
				process(results);
			}
		};
		
		@Override
		protected Void doInBackground() throws Exception {
			rtt = new RealtimeTrackerThread(pnlMapCell);
			rtt.setMapUpdateThreadResultSetter(setter);
			rtt.start();
			rtt.join();
			return null;
		}
		
		@Override
		protected void process(List<HashMap<String, String>> results) {
			// Nothing important is coming via the results param, just redraw
			pnlMapCell.updateUI();
		}
	}
	
	/**
	 * Inner class to do all the map creation work
	 */
	public class MapWorker extends SwingWorker<Void, HashMap<String, MapCell>> {
		
		public boolean done = false;
		
		MapUpdateThreadResultSetter setter = new MapUpdateThreadResultSetter() {
			public synchronized void setResult(HashMap<String, MapCell> result) {
				ArrayList<HashMap<String, MapCell>> results = new ArrayList<HashMap<String, MapCell>>();
				results.add(result);
				process(results);
			}
		};
		
		@Override
		public Void doInBackground() {
			try {
				Calendar startC = Calendar.getInstance();

				// Stop any running map threads
				for (int a = 0; a < NUM_MAP_THREADS; a++) {
					if (muts[a] != null && muts[a].isRunning()) {
						muts[a].setRunning(false);
						muts[a].join();
					}
				}
				
				// Make a MUTCoordinator to serve work (cells) to the MU Threads
				float xMetricMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getxAxisMetric());
				float xMetricMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getxAxisMetric());
				float xCellSize = (xMetricMax - xMetricMin) / (float)ps.getxRes();
				
				float yMetricMin = Constants.METRIC_MIN_MAX_VALUE.get("min_" + ps.getyAxisMetric());
				float yMetricMax = Constants.METRIC_MIN_MAX_VALUE.get("max_" + ps.getyAxisMetric());
				float yCellSize = (yMetricMax - yMetricMin) / (float)ps.getyRes();
				
				// The MUTCoordinator holds all the cells (coordinates only) and servers them out to the threads as needed
				MUTCoordinator mutCoordinator = MUTCoordinator.getInstance();
				int xArrayPos = 0;
				int yArrayPos = 0;
				for (float xMetricPos = xMetricMin; xMetricPos < xMetricMax - .001f; xMetricPos+= xCellSize) {
					for (float yMetricPos = yMetricMin; yMetricPos < yMetricMax - .001f; yMetricPos+= yCellSize) {
						mutCoordinator.addCell(xMetricPos, yMetricPos, xArrayPos, yArrayPos);
						yArrayPos++;
						if (yArrayPos == ParameterSingleton.getInstance().getyRes()) {
							yArrayPos = 0;
						}
					}
					xArrayPos++;
				}
				// The MUTCoordinator will also have a copy of all the map data needed to calculate the cell specifics
				mutCoordinator.loadRawDataIntoMemory();
				
				// Start up the threads
				for (int a = 0; a < NUM_MAP_THREADS; a++) {
					muts[a] = new MapUpdateThread();
					muts[a].setMapUpdateThreadResultSetter(setter);
					muts[a].start();
				}

				// Stop the threads
				for (int a = 0; a < NUM_MAP_THREADS; a++) {
					if (muts[a] != null && muts[a].isRunning()) {
						muts[a].join();
					}
				}
				
				Calendar endC = Calendar.getInstance();
				long ms = endC.getTimeInMillis() - startC.getTimeInMillis();
				float t = ms / 1000f;
				System.out.println("MapWorker took " + t + " seconds");
				
				done = true;
				if (runViaParams) {
					mapWorker.cancel(true);
					btnEvalMap.doClick();
				}

				return null;
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}  
		}

		@Override
		protected synchronized void process(List<HashMap<String, MapCell>> chunks) {
			HashMap<String, MapCell> allResults = chunks.get(chunks.size() - 1);
		
			// Update GUI
			MapCell mc = allResults.get("cell");
			MapCellSingleton.getInstance().addMapCell(mc);
			pnlMapCell.updateUI();
			
			lblWhite.setText(pnlMapCell.getLegendMiddle());
			lblRed.setText(pnlMapCell.getLegendMin());
			lblGreen.setText(pnlMapCell.getLegendMax());	
		}
	}
}