package miniventure.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DiscreteScreenViewport extends ScreenViewport {
	
	private final int minWidth;
	private final int minHeight;
	
	public DiscreteScreenViewport(int minWidth, int minHeight) {
		this(minWidth, minHeight, new OrthographicCamera());
	}
	
	public DiscreteScreenViewport(int minWidth, int minHeight, Camera camera) {
		super(camera);
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}
	
	@Override
	public void update(int screenWidth, int screenHeight, boolean centerCamera) {
		// check if the screen size is less than the min size, and scale
		
		float widthRatio = screenWidth / (float)minWidth;
		float heightRatio = screenHeight / (float)minHeight;
		
		float minRatio = Math.min(widthRatio, heightRatio);
		
		// only scale in integer multiples
		
		if(minRatio >= 1)
			setUnitsPerPixel(1f/((int)minRatio));
		else
			Gdx.graphics.setWindowedMode(Math.max(screenWidth, minWidth), Math.max(screenHeight, minHeight));
		
		super.update(screenWidth, screenHeight, centerCamera);
	}
}
