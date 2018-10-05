package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.util.Action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MenuScreen extends Stage {
	
	private final boolean clearGdxBackground;
	private MenuScreen parent;
	protected VerticalGroup vGroup;
	
	public MenuScreen(final boolean clearGdxBackground) {
		super(new ExtendViewport(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT), GameCore.getBatch());
		this.clearGdxBackground = clearGdxBackground;
		vGroup = new VerticalGroup();
		vGroup.space(10);
		vGroup.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
	}
	
	// called when the menu is focused, the first time and any subsequent times.
	public void focus() {}
	
	public void setParent(MenuScreen parent) { this.parent = parent; }
	public MenuScreen getParent() { return parent; }
	
	public boolean usesWholeScreen() { return clearGdxBackground; }
	
	@Override
	public void draw() {
		if(clearGdxBackground)
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.draw();
	}
	
	@Override
	public void dispose() { dispose(true); }
	public void dispose(boolean disposeParent) {
		if(disposeParent && parent != null) parent.dispose();
		super.dispose();
	}
	
	protected static VisTextButton makeButton(String text, Action onClick) {
		VisTextButton button = new VisTextButton(text);
		button.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				onClick.act();
			}
		});
		return button;
	}
	
	protected void addCentered(Actor comp) { addCentered(comp, 0); }
	protected void addCentered(Actor comp, int space) {
		Container<Actor> box = addComponent(comp, space);
		box.center();
	}
	/*protected void addSpaced(Actor comp, int space) {
		vGroup.addActor(comp);
		// if(space > 0) add(Box.createVerticalStrut(space));
	}*/
	
	protected static VisLabel makeLabel(String text) {
		VisLabel label = new VisLabel(text, new LabelStyle(GameCore.getFont(), Color.WHITE));
		label.setWrap(true);
		label.setAlignment(Align.center, Align.left);
		return label;
	}
	
	/*protected static VisTextButton makeButton(String text, Action onClick) {
		VisTextButton button = new VisTextButton(text);
		button.addActionListener(e -> onClick.act());
		// button.setFont(ClientCore.DEFAULT_FONT);
		return button;
	}*/
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+Integer.toHexString(hashCode());
	}
	
	/*protected static class LabelPanel extends JPanel {
		
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
	}*/
	
	protected <T extends Actor> Container<T> addComponent(T comp) { return addComponent(0, comp, 0); }
	protected <T extends Actor> Container<T> addComponent(int spacing, T comp) { return addComponent(spacing, comp, 0); }
	protected <T extends Actor> Container<T> addComponent(T comp, int spacing) { return addComponent(0, comp, spacing); }
	protected <T extends Actor> Container<T> addComponent(int preSpacing, T comp, int postSpacing) {
		if(vGroup.getParent() == null)
			addActor(vGroup);
		Container<T> box = new Container<>(comp);
		box.padTop(preSpacing);
		box.padBottom(postSpacing);
		vGroup.addActor(box);
		return box;
	}
}
