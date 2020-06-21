package miniventure.game.texture.layer;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;

// an animation layer that has functions to fetch the state time externally
public abstract class CachedAnimationLayer extends AnimationLayer {
	
	public CachedAnimationLayer(Animation<TextureHolder> animation) {
		super(animation, null);
	}
	
	protected abstract float fetchTimeCache();
	
	protected abstract void updateTimeCache(float delta);
	
	// @Override
	// protected float getStateTime() {
	// 	return fetchTimeCache();
	// }
}
