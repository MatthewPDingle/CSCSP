package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class StockToolTip extends JWindow {

	private final int WIDTH = 60;
	private final int HEIGHT = 18;
	
	private JPanel pnl = null;
	private JLabel lblText = null;
	private JLabel lblText2 = null;
	
	private static StockToolTip instance = null;
	
	public static StockToolTip getInstance() {
		if (instance == null) {
			instance = new StockToolTip();
		}
		return instance;
	}
	
	protected StockToolTip() {
		super();

    	this.setLayout(null);
    	this.setBounds(0, 0, WIDTH, HEIGHT);	
    	this.setBackground(new Color(0, 0, 0, 0));

    	pnl = new JPanel();
    	pnl.setName("panel");
    	pnl.setLayout(null);
    	pnl.setBounds(new Rectangle(0, 0, WIDTH, HEIGHT));
    	pnl.setOpaque(false);

    	lblText = new JLabel();
    	lblText.setBounds(new Rectangle(0, 0, WIDTH, HEIGHT));
    	lblText.setFont(new Font("Dialog", Font.BOLD, 17));
    	lblText.setForeground(new Color(187, 84, 255)); // Purple
    	lblText.setOpaque(false);
    	
    	lblText2 = new JLabel();
    	lblText2.setBounds(new Rectangle(1, 1, WIDTH, HEIGHT));
    	lblText2.setFont(new Font("Dialog", Font.BOLD, 17));
    	lblText2.setForeground(Color.BLACK);
    	lblText2.setOpaque(false);
    	
    	pnl.add(lblText);
    	pnl.add(lblText2);
    	
    	this.add(pnl);
	}
	
	public void update(MapSymbol mapSymbol, Point2D screenLocation) {
		lblText.setText(mapSymbol.getSymbol());
		lblText2.setText(mapSymbol.getSymbol());
		
		this.setBounds(new Rectangle((int)screenLocation.getX() + 8, (int)screenLocation.getY() - 11, WIDTH, HEIGHT));
		this.setVisible(true);
	}
}