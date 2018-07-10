package miniventure.game.world.levelgen.util;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class MyPanel extends JPanel {
	
	protected boolean ready = false;
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.RED);
		g.drawRect(0, 0, getWidth(), getHeight());
	}
	
	public void ready() {
		ready = true;
		for(Component c: getComponents()) {
			if(c instanceof MyPanel)
				((MyPanel)c).ready();
		}
	}
}
