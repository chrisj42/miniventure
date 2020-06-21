package miniventure.game.texture.layer;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;

// animation
public class AnimationLayer implements RenderLayer {
	/*
		- time-based sprite changing
		- sprites *have* to base their timing off *some* counter
		- so, provide a way to get a time basis from somewhere
			- as in, allow subclasses to provide their own timing sources
		
	 */
	
	private final Animation<TextureHolder> animation;
	private final TimeSource timeSource;
	
	public AnimationLayer(Animation<TextureHolder> animation, TimeSource timeSource) {
		this.animation = animation;
		this.timeSource = timeSource;
	}
	
	@Override
	public TextureHolder getSprite() {
		return animation.getKeyFrame(timeSource.getTime());
	}
	
	public interface TimeSource {
		/*
			- provides a means for the animation to determine the state time, and thereby the correct frame
		 */
		
		float getTime();
	}
}
