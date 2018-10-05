package miniventure.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public abstract class BackgroundProvider extends MenuScreen {
	
	private final boolean clearGdxBackground;
	private final boolean externallyCleared;
	
	public BackgroundProvider(boolean clearGdxBackground) { this(clearGdxBackground, false); }
	public BackgroundProvider(boolean clearGdxBackground, boolean externallyCleared) {
		super(false);
		this.clearGdxBackground = clearGdxBackground;
		this.externallyCleared = externallyCleared;
	}
	
	public abstract void renderBackground();
	
	@Override
	public boolean usesWholeScreen() { return clearGdxBackground || externallyCleared; }
	
	@Override
	public void draw() {
		if(clearGdxBackground)
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderBackground();
		super.draw();
	}
}
