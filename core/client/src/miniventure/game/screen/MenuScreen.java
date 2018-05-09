package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.util.Action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MenuScreen extends Stage {
	
	private MenuScreen parent;
	protected VerticalGroup vGroup;
	
	public MenuScreen() {
		super(new ExtendViewport(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT), GameCore.getBatch());
		vGroup = new VerticalGroup();
		vGroup.space(10);
		vGroup.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
	}
	
	// called when the menu is focused, the first time and any subsequent times.
	public void focus() {}
	
	public void setParent(MenuScreen parent) { this.parent = parent; }
	public MenuScreen getParent() { return parent; }
	
	public boolean usesWholeScreen() { return true; }
	
	@Override
	public void draw() {
		if(usesWholeScreen())
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
}
