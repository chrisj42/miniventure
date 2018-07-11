package miniventure.game.world.levelgen.util;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class MyPanel extends JPanel {
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// g.setColor(Color.RED);
		// g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}
}
