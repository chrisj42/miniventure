package miniventure.game.screen.util;

import miniventure.game.core.ClientCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BackgroundInheritor extends BackgroundProvider {
	
	private BackgroundProvider gdxBackground;
	
	public BackgroundInheritor() {
		super(false);
		// the background provider will clear the background if needed
	}
	public BackgroundInheritor(Viewport viewport) {
		super(false, viewport);
		// the background provider will clear the background if needed
	}
	
	@Override
	public boolean usesWholeScreen() {
		return gdxBackground != null && gdxBackground.usesWholeScreen();
	}
	
	public void setBackground(final BackgroundProvider gdxBackground) {
		if(gdxBackground instanceof BackgroundInheritor)
			setBackground(((BackgroundInheritor)gdxBackground).gdxBackground);
		else if(gdxBackground != this) // don't want an infinite loop going... but that's impossible!
			this.gdxBackground = gdxBackground;
	}
	
	@Override
	public void renderBackground() {
		if(gdxBackground != null)
			gdxBackground.renderBackground();
		else if(ClientCore.getWorld() == null || !ClientCore.getWorld().hasRenderableLevel()) // there is no background when we expect one to have been given to us; clear the background to make sure it actually happens.
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// if there is a renderable level, that means that it will be drawn and therefore the screen need not be cleared.
	}
	
	@Override
	public void resizeBackground(int width, int height) {
		if(gdxBackground != null)
			gdxBackground.resize(width, height);
	}
}
