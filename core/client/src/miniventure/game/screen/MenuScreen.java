package miniventure.game.screen;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import miniventure.game.client.ClientCore;
import miniventure.game.util.Action;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public abstract class MenuScreen extends JPanel {
	
	private MenuScreen parent;
	
	private final boolean clearGdxBackground;
	
	protected LabelPanel labelPanel = null; // only instantiated if used
	
	/**
	 * @param clearGdxBackground it the menu takes up the entire screen; note that this does not size the menu, but rather paints its background color around it on the libGDX canvas.
	 */
	public MenuScreen(boolean clearGdxBackground) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.clearGdxBackground = clearGdxBackground;
		setFocusable(false);
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 0));
	}
	
	// called when the menu is focused, the first time and any subsequent times.
	public void focus() {
		revalidate();
		if(isFocusable())
			requestFocus();
		repaint();
	}
	
	public void setParentScreen(MenuScreen parent) { this.parent = parent; }
	public MenuScreen getParentScreen() { return parent; }
	
	protected void setupAnchorLayout(AnchorPanel parent) {
		parent.addToAnchorLayout(this, RelPos.CENTER);
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		if(!isOpaque() && !clearGdxBackground) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		super.paintComponent(g);
	}
	
	private final com.badlogic.gdx.graphics.Color background = new com.badlogic.gdx.graphics.Color();
	public void glDraw() {
		if(clearGdxBackground) {
			com.badlogic.gdx.graphics.Color.argb8888ToColor(background, getBackground().getRGB());
			Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
	}
	
	protected void addCentered(JComponent comp) { addCentered(comp, 0); }
	protected void addCentered(JComponent comp, int space) {
		comp.setAlignmentX(CENTER_ALIGNMENT);
		addSpaced(comp, space);
	}
	protected void addSpaced(Component comp, int space) {
		add(comp);
		if(space > 0) add(Box.createVerticalStrut(space));
	}
	
	protected static JLabel makeLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(ClientCore.DEFAULT_FONT);
		return label;
	}
	
	protected static JButton makeButton(String text, Action onClick) {
		JButton button = new JButton(text);
		button.addActionListener(e -> onClick.act());
		button.setFont(ClientCore.DEFAULT_FONT);
		return button;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+Integer.toHexString(hashCode());
	}
	
	protected static class LabelPanel extends JPanel {
		
		public LabelPanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setOpaque(false);
			setBackground(new Color(163, 227, 232, 200));
			setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.CYAN, Color.GRAY),
			  BorderFactory.createEmptyBorder(20, 30, 20, 20)
			));
		}
		
		@Override
		protected void paintComponent(final Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}
	
	protected <T extends JComponent> T addComponent(T comp) { return addComponent(0, comp, 0); }
	protected <T extends JComponent> T addComponent(int spacing, T comp) { return addComponent(spacing, comp, 0); }
	protected <T extends JComponent> T addComponent(T comp, int spacing) { return addComponent(0, comp, spacing); }
	protected <T extends JComponent> T addComponent(int preSpacing, T comp, int postSpacing) {
		if(labelPanel == null) {
			labelPanel = new LabelPanel();
			add(labelPanel);
		}
		if(preSpacing > 0) labelPanel.add(Box.createVerticalStrut(preSpacing));
		labelPanel.add(comp);
		if(postSpacing > 0) labelPanel.add(Box.createVerticalStrut(postSpacing));
		return comp;
	}
}
