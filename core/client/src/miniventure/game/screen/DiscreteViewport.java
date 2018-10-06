package miniventure.game.screen;

import miniventure.game.GameCore;

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
		 
		this.minWidth = GameCore.DEFAULT_SCREEN_WIDTH;
		this.minHeight = GameCore.DEFAULT_SCREEN_HEIGHT;
	}
	
	@Override
	public void update(int screenWidth, int screenHeight, boolean centerCamera) {
		// check if the screen size is less than the min size, and scale
		
		float widthRatio = screenWidth / (float)minWidth;
		float heightRatio = screenHeight / (float)minHeight;
		
		float minRatio = Math.min(widthRatio, heightRatio);
		
		// only scale in integer multiples
		minRatio = (int)(4*minRatio)/4f;
		
		if(minRatio >= 1)
			setUnitsPerPixel(1f/minRatio);
		else
			Gdx.graphics.setWindowedMode(Math.max(screenWidth, minWidth), Math.max(screenHeight, minHeight));
		
		super.update(screenWidth, screenHeight, centerCamera);
	}
}
