package miniventure.game.screen;

import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import miniventure.game.client.ClientCore;

public class MultilineLabel extends JTextArea {
	
	public MultilineLabel(String... text) { this(String.join(System.lineSeparator(), text)); }
	public MultilineLabel(String text) {
		super(text);
		setOpaque(false);
		setEditable(false);
		setFocusable(false);
		setLineWrap(true);
		setWrapStyleWord(true);
		setBorder(null);
		setFont(ClientCore.DEFAULT_FONT);
		setBackground(new Color(0, 0, 0, 0));
	}
	
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponent(g);
	}
}
