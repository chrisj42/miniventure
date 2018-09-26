package miniventure.game.screen;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Container;

import miniventure.game.util.Action;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

public abstract class MenuScreen extends JPanel {
	
	private MenuScreen parent;
	private final boolean fillScreen;
	// protected VerticalGroup vGroup;
	
	/**
	 * @param transparent if the panel should be transparent; if true, then a call will be made to allow the libGDX canvas to render on top of it.
	 * @param fillScreen it the menu takes up the entire screen; note that this does not size the menu, but rather paints its background color around it on the libGDX canvas.
	 */
	public MenuScreen(boolean transparent, boolean fillScreen) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.fillScreen = fillScreen;
		setFocusable(false);
		setOpaque(!transparent);
		if(transparent)
			ClientUtils.setupTransparentAWTContainer(this);
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
	
	// protected abstract boolean usesWholeScreen();
	
	/**
	 * Because the UI Panel will not have a layout, MenuScreens must provide a way to adapt to changes in screen size.
	 * This will be done through a ComponentListener, which will be added to the ui panel, and removed when this screen
	 * is removed from the panel.
	 */
	public void doLayoutBehavior(Container parent) {
		ClientUtils.addToAnchorLayout(this, parent, RelPos.CENTER);
	}
	
	/*@Override
	public Dimension getPreferredSize() {
		if(fillScreen)
			return ClientCore.getUiPanel().getSize();
		return super.getPreferredSize();
	}*/
	
	private final Color background = new Color();
	public void glDraw() {
		if(fillScreen) {
			Color.argb8888ToColor(background, getBackground().getRGB());
			Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		// super.draw();
	}
	
	/*@Override
	public void dispose() { dispose(true); }
	public void dispose(boolean disposeParent) {
		if(disposeParent && parent != null) parent.dispose();
		super.dispose();
	}*/
	
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if(comp instanceof Box.Filler)
			ClientUtils.setupTransparentAWTContainer(comp);
		
		super.addImpl(comp, constraints, index);
	}
	
	protected void addCentered(JComponent comp) {
		comp.setAlignmentX(CENTER_ALIGNMENT);
		super.add(comp);
	}
	protected void addSpaced(Component comp, int space) {
		add(comp);
		add(Box.createVerticalStrut(space));
	}
	
	protected static JButton makeButton(String text, Action onClick) {
		JButton button = new JButton(text);
		button.addActionListener(e -> onClick.act());
		return button;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+Integer.toHexString(hashCode());
	}
}
