package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

// FIXME this class will be used for water and stuff, mainly; except, I'm thinking I'll make a subclass, RandomAnimationProperty, to do that; it will pick from a group of frames randomly, at set intervals. I'm not sure what I'm going to do with this. So I'm just going to leave it alone for now.
public abstract class AnimationProperty implements TileProperty {
	
	// TODO add a way to specify if a tile should draw the tile underneath it, so fully opaque tiles don't try to draw the bottom one when none of it will be seen.
	
	public abstract TextureRegion getSprite(float timeElapsed);
	
	@Override
	public Integer[] getInitData() { return new Integer[0]; }
	
	
	static class Animated extends AnimationProperty {
		
		private boolean initialized = false;
		private final float frameTime;
		private Animation<TextureRegion> animation;
		
		public Animated(float frameTime) {
			this.frameTime = frameTime;
		}
		
		void initialize(Array<AtlasRegion> frames) {
			animation = new Animation<>(frameTime, frames);
			initialized = true;
		}
		
		@Override
		public TextureRegion getSprite(float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			return animation.getKeyFrame(timeElapsed, true);
		}
	}
	
	
	
	static class SingleFrame extends AnimationProperty {
		
		private boolean initialized = false;
		private TextureRegion texture;
		
		void initialize(TextureRegion frame) {
			texture = frame;
			initialized = true;
		}
		
		@Override
		public TextureRegion getSprite(float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			return texture;
		}
	}
}
