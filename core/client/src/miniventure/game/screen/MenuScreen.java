package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class MenuScreen extends Stage {
	
	/*
		So! This class uses Scene2D. I think I'll have GameCore hold instances of each type of screen... or, perhaps, of each stage. When a menu is created, the stage is fetched, and the new instance configures the labels to its needs.
		
		/// IDEA How about I have MenuScreen be an interface; or make another interface that MenuScreen implements. The idea is that I can have displays that don't use Scene2D (like the the loading screen, or level transitions if that's a thing), since they don't have options.
	 */
	
	private MenuScreen parent;
	protected VerticalGroup vGroup;
	protected Table table;
	
	public MenuScreen() {
		super(new ExtendViewport(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT), GameCore.getBatch());
		vGroup = new VerticalGroup() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				drawTable(batch, parentAlpha);
				super.draw(batch, parentAlpha);
			}
		};
		
		table = new Table() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				drawTable(batch, parentAlpha);
				super.draw(batch, parentAlpha);
			}
		};
		
		vGroup.space(10);
		vGroup.setOrigin(Align.topLeft);
		vGroup.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
		
		table.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
		addActor(vGroup);
	}
	
	public void setParent(MenuScreen parent) { this.parent = parent; }
	
	public boolean usesWholeScreen() { return true; }
	
	protected void drawTable(Batch batch, float parentAlpha) {}
	
	@Override
	public void draw() {
		if(usesWholeScreen())
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.draw();
	}
	
	@Override
	public void dispose() {
		if(parent != null) parent.dispose();
		super.dispose();
	}
}
