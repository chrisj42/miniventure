package miniventure.game.screen;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import miniventure.game.client.ClientCore;
import miniventure.game.util.Action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class MenuScreen extends JPanel {
	
	private MenuScreen parent;
	// protected VerticalGroup vGroup;
	
	public MenuScreen(boolean focusable) { this(focusable, false); }
	public MenuScreen(boolean focusable, boolean opaque) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		if(!opaque)
			ClientCore.setupTransparentSwingContainer(this, focusable);
		else
			setFocusable(focusable);
		
		setOpaque(opaque);
		// super(new ExtendViewport(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT), GameCore.getBatch());
		// vGroup = new VerticalGroup();
		// add(Box.createVerticalStrut(10));
		// vGroup.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
	}
	
	// called when the menu is focused, the first time and any subsequent times.
	public void focus() {
		revalidate();
		if(isFocusable())
			requestFocus();
		repaint();
	}
	
	public void setParent(MenuScreen parent) { this.parent = parent; }
	public MenuScreen getParent() { return parent; }
	
	public boolean usesWholeScreen() { return true; }
	
	// @Override
	public void glDraw() {
		if(usesWholeScreen())
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// super.draw();
	}
	
	/*@Override
	public void dispose() { dispose(true); }
	public void dispose(boolean disposeParent) {
		if(disposeParent && parent != null) parent.dispose();
		super.dispose();
	}*/
	
	protected static JButton makeButton(String text, Action onClick) {
		JButton button = new JButton(text);
		button.addActionListener(e -> onClick.act());
		return button;
	}
}
