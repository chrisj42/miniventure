package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.widget.VisTable;

public class MenuScreen extends Stage {
	
	/*
		So! This class uses Scene2D. I think I'll have GameCore hold instances of each type of screen... or, perhaps, of each stage. When a menu is created, the stage is fetched, and the new instance configures the labels to its needs.
		
		
	 */
	
	private MenuScreen parent;
	protected VisTable table;
	
	public MenuScreen() {
		super(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), GameCore.getBatch());
		table = new VisTable(true);
		table.setPosition(getWidth()/2, getHeight()*2/3, Align.center);
		addActor(table);
	}
	
	public void setParent(MenuScreen parent) { this.parent = parent; }
	
	public boolean usesWholeScreen() { return true; }
	
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
