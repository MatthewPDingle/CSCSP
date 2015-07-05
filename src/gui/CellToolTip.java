package gui;

import gui.singletons.ParameterSingleton;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import constants.Constants;

public class CellToolTip extends JWindow {

	private final int WIDTH = 396;
	private final int HEIGHT = 206;
	
	private JPanel pnl = null;
	private ParameterSingleton ps = ParameterSingleton.getInstance();
	
	private static CellToolTip instance = null;
	
	public static CellToolTip getInstance() {
		if (instance == null) {
			instance = new CellToolTip();
		}
		return instance;
	}
	
	protected CellToolTip() {
		super();
		
    	this.setLayout(null);
    	this.setBounds(0, 0, WIDTH, HEIGHT);	

    	pnl = new JPanel();
    	pnl.setName("panel");
    	pnl.setLayout(null);
    	pnl.setBounds(new Rectangle(0, 0, WIDTH, HEIGHT));
    	pnl.setBorder(BorderFactory.createLineBorder(GUI.colorSectionBackground, 1));
    	pnl.setBackground(Color.WHITE);
    	pnl.setOpaque(true);
    	pnl.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent me) {}
			
			public void mouseEntered(MouseEvent me) {
				CellToolTip.getInstance().setVisible(false);
			}
			
			public void mouseExited(MouseEvent me) {}
			
			public void mouseReleased(MouseEvent me) {}
			
			public void mousePressed(MouseEvent me) {}
		});
    	
    	this.add(pnl);
	}
	
	public void update(MapCellPanel mcp, MapCell highlightedMapCell, 
			int mouseEventScreenX, int mouseEventScreenY, Point2D mouseEventPoint,
			float mapXMetricMin, float mapXMetricMax, float mapYMetricMin, float mapYMetricMax) {
		try {
			// Adjust the location of the tooltip if it'll otherwise appear off the map
			int xAdjustment = 0;
			int yAdjustment = 0;
			if (mouseEventPoint.getY() + this.getHeight() + 8 > mcp.getHeight()) {
				yAdjustment = yAdjustment - this.getHeight() - 16;
			}
			if (mouseEventPoint.getX() + this.getWidth() + 8 > mcp.getWidth()) {
				xAdjustment = xAdjustment - this.getWidth() - 16;
			}

			this.setLocation(mouseEventScreenX + xAdjustment + 8, mouseEventScreenY + yAdjustment + 8);
			this.setVisible(true);
			
			HashMap<String, Float> metricValueHash = highlightedMapCell.getMetricValueHash();
			int locationY = 23;
			int numStopResults = 0;
			int numEndResults = 0;
			int numMetricResults = 0;
			for (String colorOption:Constants.MAP_COLOR_OPTIONS) {
				Float value = metricValueHash.get(colorOption);

				if (value != null) {
					JLabel lblLabel = null;
					JLabel lblValue = null;
					
					Component[] components = pnl.getComponents();
					
					// X Metric value
					JLabel lblXMetric = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("lblXMetric")) {
							lblXMetric = (JLabel)components[a];
						}
					}
					if (lblXMetric == null)
						lblXMetric = new JLabel();
					lblXMetric.setName("lblXMetric");
					lblXMetric.setText(" X: " + String.format("%.1f", mapXMetricMin) + "/" + String.format("%.1f", mapXMetricMax));
					lblXMetric.setFont(new Font("Dialog", Font.BOLD, 12));
					lblXMetric.setBounds(new Rectangle(3, 3, 70, 20));
					lblXMetric.setBackground(GUI.colorSectionBackground);
					lblXMetric.setForeground(Color.ORANGE);
					lblXMetric.setOpaque(true);
					
					// Y Metric value
					JLabel lblYMetric = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("lblYMetric")) {
							lblYMetric = (JLabel)components[a];
						}
					}
					if (lblYMetric == null)
						lblYMetric = new JLabel();
					lblYMetric.setName("lblYMetric");         
					lblYMetric.setText("Y: " + String.format("%.1f", mapYMetricMin) + "/" + String.format("%.1f", mapYMetricMax));
					lblYMetric.setFont(new Font("Dialog", Font.BOLD, 12));
					lblYMetric.setHorizontalAlignment(SwingConstants.RIGHT);
					lblYMetric.setBounds(new Rectangle(73, 3, 70, 20));
					lblYMetric.setBackground(GUI.colorSectionBackground);
					lblYMetric.setForeground(new Color(.72f, .92f, 1f));
					lblYMetric.setOpaque(true);
					
					// All 
					JLabel lblAll = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("all_label")) {
							lblAll = (JLabel)components[a];
						}
					}
					if (lblAll == null)
						lblAll = new JLabel();
					lblAll.setName("all_label");
					lblAll.setHorizontalAlignment(SwingConstants.CENTER);
					lblAll.setText(" All");
					lblAll.setFont(new Font("Dialog", Font.BOLD, 12));
					lblAll.setBounds(new Rectangle(143, 3, 50, 20));
					if (ps.getMapColor().contains("All") && !ps.getMapColor().contains("Alpha"))
						lblAll.setForeground(Color.YELLOW);
					else
						lblAll.setForeground(GUI.colorSelectionForeground);
					lblAll.setBackground(GUI.colorSectionBackground);
					lblAll.setOpaque(true);
					
					// Alpha
					JLabel lblAlpha = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("alpha_label")) {
							lblAlpha = (JLabel)components[a];
						}
					}
					if (lblAlpha == null)
						lblAlpha = new JLabel();
					lblAlpha.setName("alpha_label");
					lblAlpha.setHorizontalAlignment(SwingConstants.CENTER);
					lblAlpha.setText(" (Alpha)");
					lblAlpha.setFont(new Font("Dialog", Font.BOLD, 12));
					lblAlpha.setBounds(new Rectangle(193, 3, 50, 20));
					if (ps.getMapColor().contains("Alpha"))
						lblAlpha.setForeground(Color.YELLOW);
					else
						lblAlpha.setForeground(GUI.colorSelectionForeground);
					lblAlpha.setBackground(GUI.colorSectionBackground);
					lblAlpha.setOpaque(true);
					
					// VDiv Line
					JPanel pnlVDiv = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("pnlVDiv")) {
							pnlVDiv = (JPanel)components[a];
						}
					}
					if (pnlVDiv == null)
						pnlVDiv = new JPanel();
					pnlVDiv.setName("pnlVDiv");
					pnlVDiv.setBackground(GUI.colorSectionBackground);
					pnlVDiv.setBounds(new Rectangle(244, 25, 1, 178));
					
					// HDiv Line
					JPanel pnlHDiv = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("pnlHDiv")) {
							pnlHDiv = (JPanel)components[a];
						}
					}
					if (pnlHDiv == null)
						pnlHDiv = new JPanel();
					pnlHDiv.setName("pnlHDiv");
					pnlHDiv.setBackground(GUI.colorSectionBackground);
					pnlHDiv.setBounds(new Rectangle(145, 57, 248, 1));
					
					// Metric
					JLabel lblMetric = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("metric_label")) {
							lblMetric = (JLabel)components[a];
						}
					}
					if (lblMetric == null)
						lblMetric = new JLabel();
					lblMetric.setName("metric_label");
					lblMetric.setHorizontalAlignment(SwingConstants.CENTER);
					lblMetric.setText(" Metric");
					lblMetric.setFont(new Font("Dialog", Font.BOLD, 12));
					lblMetric.setBounds(new Rectangle(243, 3, 50, 20));
					if (ps.getMapColor().contains("Metric"))
						lblMetric.setForeground(Color.YELLOW);
					else
						lblMetric.setForeground(GUI.colorSelectionForeground);
					lblMetric.setBackground(GUI.colorSectionBackground);
					lblMetric.setOpaque(true);
					
					// Stop
					JLabel lblStop = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("stop_label")) {
							lblStop = (JLabel)components[a];
						}
					}
					if (lblStop == null) 							
						lblStop = new JLabel();
					lblStop.setName("stop_label");
					lblStop.setHorizontalAlignment(SwingConstants.CENTER);
					lblStop.setText(" Stop");
					lblStop.setFont(new Font("Dialog", Font.BOLD, 12));
					lblStop.setBounds(new Rectangle(293, 3, 50, 20));
					if (ps.getMapColor().contains("Stop"))
						lblStop.setForeground(Color.YELLOW);
					else
						lblStop.setForeground(GUI.colorSelectionForeground);
					lblStop.setBackground(GUI.colorSectionBackground);
					lblStop.setOpaque(true);
					
					// End
					JLabel lblEnd = null;
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals("end_label")) {
							lblEnd = (JLabel)components[a];
						}
					}
					if (lblEnd == null)
						lblEnd = new JLabel();
					lblEnd.setName("end_label");
					lblEnd.setHorizontalAlignment(SwingConstants.CENTER);
					lblEnd.setText(" End");
					lblEnd.setFont(new Font("Dialog", Font.BOLD, 12));
					lblEnd.setBounds(new Rectangle(343, 3, 50, 20));
					if (ps.getMapColor().contains("End"))
						lblEnd.setForeground(Color.YELLOW);
					else
						lblEnd.setForeground(GUI.colorSelectionForeground);
					lblEnd.setBackground(GUI.colorSectionBackground);
					lblEnd.setOpaque(true);
				
					boolean blbl = false;
					boolean bval = false;
					
					
					// Note the number of Stop & End results so I don't display anything if there're 0
					if (colorOption.contains("Stop") && colorOption.contains("# Positions")) {
						numStopResults = value.intValue();
					}
					if (colorOption.contains("End") && colorOption.contains("# Positions")) {
						numEndResults = value.intValue();
					}
					if (colorOption.contains("Metric") && colorOption.contains("# Positions")) {
						numMetricResults = value.intValue();
					}
					
					for (int a = 0; a < components.length; a++) {
						if (components[a].getName().equals(colorOption + "_label")) {
							lblLabel = (JLabel)components[a];
							lblLabel.setHorizontalAlignment(SwingConstants.RIGHT);
							String label = colorOption.replaceAll("All - ", "").replaceAll("Alpha ", "").replaceAll("Stop - ", "").replaceAll("End - ", "");
							if (colorOption.contains("All") && !colorOption.contains("Alpha")) {
								lblLabel.setText(label + " ");
							}
							else {
								lblLabel.setText("");
							}
							if (ps.getMapColor().contains(label)) {
								if ((label.contains("Geo") && ps.getMapColor().contains("Geo")) ||
										(!label.contains("Geo") && !ps.getMapColor().contains("Geo"))) {
						    		lblLabel.setFont(new Font("Dialog", Font.BOLD, 12));
						    		lblLabel.setForeground(Color.YELLOW);
								}
					    	}
					    	else {
					    		lblLabel.setFont(new Font("Dialog", Font.BOLD, 12));
					    	}
							blbl = true;
						}
						else if (components[a].getName().equals(colorOption + "_value")) {
							lblValue = (JLabel)components[a];
							
							String text = "";
							if (colorOption.contains("#")) {
								text = String.format("%.0f", value);
							}
							else if (colorOption.contains("%") || colorOption.contains("Mean Return") || colorOption.contains("Median")) {
								text = String.format("%.1f", value) + "%";
							}
							else if (colorOption.contains("Geo-Mean / Day")) {
								text = String.format("%.2f", value) + "%";
							}
							else if (colorOption.contains("Sharpe") || colorOption.contains("Sortino")) {
								text = String.format("%.2f", value);
							}
							else {
								text = String.format("%.1f", value);
							}
							
							if (!text.contains("NaN")) {
								lblValue.setText(text);
							}
							if (colorOption.contains("Stop") && numStopResults == 0 && !colorOption.contains("Positions")) {
					    		lblValue.setText("");
					    	}
							if (colorOption.contains("End") && numEndResults == 0 && !colorOption.contains("Positions")) {
					    		lblValue.setText("");
					    	}
							if (colorOption.contains("Metric") && numMetricResults == 0 && !colorOption.contains("Positions")) {
					    		lblValue.setText("");
					    	}
					    	if (colorOption.equals(ps.getMapColor())) {
					    		lblValue.setFont(new Font("Dialog", Font.BOLD, 13));
					    	}
					    	else {
					    		lblValue.setFont(new Font("Dialog", Font.PLAIN, 12));
					    	}
							bval = true;
						}
					}
					
					if (!blbl) {
						if (colorOption.contains("All") && !colorOption.contains("Alpha")) {
							lblLabel = new JLabel();
							lblLabel.setName(colorOption + "_label");
							lblLabel.setHorizontalAlignment(SwingConstants.RIGHT);
							String label = colorOption.replaceAll("All - ", "").replaceAll("Alpha ", "").replaceAll("Stop - ", "").replaceAll("End - ", "");
							lblLabel.setText(label + " ");
					    	lblLabel.setBackground(GUI.colorSectionBackground);
					    	lblLabel.setForeground(GUI.colorSelectionForeground);
					    	lblLabel.setOpaque(true);
					    	lblLabel.setBounds(new Rectangle(3, locationY, 140, 20));
					    	if (ps.getMapColor().contains(label)) {
					    		if ((label.contains("Geo") && ps.getMapColor().contains("Geo")) ||
										(!label.contains("Geo") && !ps.getMapColor().contains("Geo"))) {
						    		lblLabel.setFont(new Font("Dialog", Font.BOLD, 12));
						    		lblLabel.setForeground(Color.YELLOW);
								}
					    	}
					    	else {
					    		lblLabel.setFont(new Font("Dialog", Font.BOLD, 12));
					    	}
						}
					}
					if (!bval) {
						lblValue = new JLabel();
						lblValue.setName(colorOption + "_value");
						lblValue.setHorizontalAlignment(SwingConstants.CENTER);

						int y = 1;
						if (colorOption.contains("# Positions")) {
							y = 24;
						}
						if (colorOption.contains("% Positions")) {
							y = 40;
						}
						if (colorOption.contains("Mean Return")) {
							y = 56;
						}
						if (colorOption.contains("Geo-Mean Return")) {
							y = 72;
						}
						if (colorOption.contains("Median Return")) {
							y = 88;
						}
						if (colorOption.contains("Mean Win %")) {
							y = 104;
						}
						if (colorOption.contains("Mean Position Duration")) {
							y = 120;
						}
						if (colorOption.contains("Drawdown")) {
							y = 136;
						}
						if (colorOption.contains("Geo-Mean / Day")) {
							y = 152;
						}
						if (colorOption.contains("Sharpe")) {
							y = 168;
						}
						if (colorOption.contains("Sortino")) {
							y = 184;
						}
						
						if (colorOption.contains("All") && !colorOption.contains("Alpha")) {
							lblValue.setBounds(new Rectangle(143, y, 62, 18));
						}
						if (colorOption.contains("Alpha")) {
							lblValue.setBounds(new Rectangle(193, y, 62, 18));
						}
						if (colorOption.contains("Metric")) {
							lblValue.setBounds(new Rectangle(243, y, 62, 18));
						}
						if (colorOption.contains("Stop")) {
							lblValue.setBounds(new Rectangle(293, y, 62, 18));
						}
						if (colorOption.contains("End")) {
							lblValue.setBounds(new Rectangle(343, y, 62, 18));
						}
				    	
				    	String text = "";
				    	if (colorOption.contains("#")) {
							text = String.format("%.0f", value);
						}
						else if (colorOption.contains("%") || colorOption.contains("Mean Return") || colorOption.contains("Median")) {
							text = String.format("%.1f", value) + "%";
						}
						else if (colorOption.contains("Geo-Mean / Day")) {
							text = String.format("%.2f", value) + "%";
						}
						else if (colorOption.contains("Sharpe") || colorOption.contains("Sortino")) {
							text = String.format("%.2f", value);
						}
						else {
							text = String.format("%.1f", value);
						}
						
				    	if (!text.contains("NaN")) {
							lblValue.setText(text);
						}
				    	if (colorOption.contains("Stop") && numStopResults == 0 && !colorOption.contains("Positions")) {
				    		lblValue.setText("");
				    	}
				    	if (colorOption.contains("End") && numEndResults == 0 && !colorOption.contains("Positions")) {
				    		lblValue.setText("");
				    	}
				    	if (colorOption.contains("Metric") && numMetricResults == 0 && !colorOption.contains("Positions")) {
				    		lblValue.setText("");
				    	}
				    	if (colorOption.equals(ps.getMapColor())) {
				    		lblValue.setFont(new Font("Dialog", Font.BOLD, 13));
				    	}
				    	else {
				    		lblValue.setFont(new Font("Dialog", Font.PLAIN, 12));
				    	}
					}

					if (lblLabel != null)
						pnl.add(lblLabel);
			    	pnl.add(lblValue);
			    	pnl.add(lblXMetric);
			    	pnl.add(lblYMetric);
			    	pnl.add(lblAll);
			    	pnl.add(lblAlpha);
			    	pnl.add(lblMetric);
			    	pnl.add(lblStop);
			    	pnl.add(lblEnd);
			    	pnl.add(pnlVDiv);
			    	pnl.add(pnlHDiv);
				}
				locationY += 16;
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}