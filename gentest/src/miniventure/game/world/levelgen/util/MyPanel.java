package miniventure.game.world.levelgen.util;

import javax.swing.JPanel;

import java.awt.LayoutManager;

public class MyPanel extends JPanel {
	
	public MyPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		setBackground(null);
	}
	
	public MyPanel(LayoutManager layout) {
		super(layout);
		setBackground(null);
	}
	
	public MyPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		setBackground(null);
	}
	
	public MyPanel() {
		super();
		setBackground(null);
	}
	
	/*@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.RED);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}*/
}
