package miniventure.game.screen;

import miniventure.game.GameCore;

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
		 
		this.minWidth = GameCore.DEFAULT_SCREEN_WIDTH*4/5;
		this.minHeight = GameCore.DEFAULT_SCREEN_HEIGHT*4/5;
	}
	
	@Override
	public void update(int screenWidth, int screenHeight, boolean centerCamera) {
		// check if the screen size is less than the min size, and scale
		
		float widthRatio = screenWidth / (float)minWidth;
		float heightRatio = screenHeight / (float)minHeight;
		
		float minRatio = Math.min(widthRatio, heightRatio);
		
		// only scale in integer multiples
		setUnitsPerPixel(1f/(int)minRatio);
		
		super.update(screenWidth, screenHeight, centerCamera);
	}
}
