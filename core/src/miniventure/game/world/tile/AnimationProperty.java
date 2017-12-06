package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public abstract class AnimationProperty implements TileProperty {
	
	// TODO add a way to specify if a tile should draw the tile underneath it, so fully opaque tiles don't try to draw the bottom one when none of it will be seen.
	
	// maybe have an animationProperty for each overlap state, thing..?
	
	public TextureRegion getSprite(float timeElapsed, Array<Tile> adjacentTiles) {
		return getSprite(timeElapsed);
	}
	protected abstract TextureRegion getSprite(float timeElapsed);
	
	@Override
	public Integer[] getInitData() { return new Integer[0]; }
	
	abstract void initialize(Array<AtlasRegion> frames);
	
	static class Animated extends AnimationProperty {
		
		private boolean initialized = false;
		private final float frameTime;
		private Animation<TextureRegion> animation;
		
		public Animated(float frameTime) {
			this.frameTime = frameTime;
		}
		
		@Override
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
	
	static class RandomFrame extends AnimationProperty {
		private boolean initialized = false;
		private final float frameTime;
		private Array<? extends TextureRegion> frames;
		
		public RandomFrame(float frameTime) {
			this.frameTime = frameTime;
		}
		
		@Override
		void initialize(Array<AtlasRegion> frames) {
			this.frames = frames;
			initialized = true;
		}
		
		@Override
		public TextureRegion getSprite(float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			MathUtils.random.setSeed((long)(timeElapsed/frameTime));
			return frames.get(MathUtils.random(frames.size-1));
		}
	}
	
	
	
	static class SingleFrame extends AnimationProperty {
		
		private boolean initialized = false;
		private TextureRegion texture;
		
		@Override
		void initialize(Array<AtlasRegion> frames) {
			texture = frames.size > 0 ? frames.get(0) : null;
			initialized = frames.size > 0;
		}
		
		@Override
		public TextureRegion getSprite(float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			return texture;
		}
	}
}
