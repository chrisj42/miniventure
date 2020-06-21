package miniventure.game.screen.util;

import miniventure.game.core.GdxCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DiscreteViewport extends ScreenViewport {
	
	private final int minWidth;
	private final int minHeight;
	
	public DiscreteViewport() {
		this(new OrthographicCamera());
	}
	
	public DiscreteViewport(Camera camera) {
		super(camera);
		
		this.minWidth = GdxCore.DEFAULT_SCREEN_WIDTH*4/5;
		this.minHeight = GdxCore.DEFAULT_SCREEN_HEIGHT*4/5;
	}
	
	@Override
	public void update(int screenWidth, int screenHeight, boolean centerCamera) {
		// check if the screen size is less than the min size, and scale
		
		float widthRatio = screenWidth / (float)minWidth;
		float heightRatio = screenHeight / (float)minHeight;
		
		float minRatio = Math.min(widthRatio, heightRatio);
		
		// only scale in integer multiples
		if(minRatio >= 1)
			setUnitsPerPixel(1f/(int)minRatio);
		else
			Gdx.graphics.setWindowedMode(Math.max(minWidth, screenWidth), Math.max(minHeight, screenHeight));
		
		super.update(screenWidth, screenHeight, centerCamera);
	}
}
