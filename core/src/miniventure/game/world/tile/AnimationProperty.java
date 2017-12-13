package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class AnimationProperty implements TileProperty {
	
	private static HashMap<String, HashMap<String, Array<AtlasRegion>>> tileConnectAnimations = new HashMap<>();
	static {
		Array<AtlasRegion> regions = GameCore.tileAtlas.getRegions();
		for(AtlasRegion region: regions) {
			String tilename = region.name.substring(0, region.name.indexOf("/"));
			String connectIdx = region.name.substring(region.name.indexOf("/")+1);
			tileConnectAnimations.computeIfAbsent(tilename, k -> new HashMap<>());
			tileConnectAnimations.get(tilename).computeIfAbsent(connectIdx, k -> new Array<>());
			tileConnectAnimations.get(tilename).get(connectIdx).add(region);
		}
	}
	
	@FunctionalInterface
	private interface SpriteFetcher {
		AtlasRegion getSprite(Tile tile, Array<AtlasRegion> frames, int frameIdx);
	}
	
	enum AnimationType {
		SEQUENCE((tile, frames, frameIdx) -> frames.get(frameIdx % frames.size)),
		
		RANDOM((tile, frames, frameIdx) -> {
			MathUtils.random.setSeed((long)(frameIdx) + tile.getCenterX() * tile.getCenterX() + tile.getCenterY());
			return frames.get(MathUtils.random(frames.size-1));
		}),
		
		SINGLE_FRAME((tile, frames, frameIdx) -> frames.get(0));
		
		private SpriteFetcher fetcher;
		
		AnimationType(SpriteFetcher fetcher) { this.fetcher = fetcher; }
		
		public AtlasRegion getSprite(Tile tile, Array<AtlasRegion> frames, int frameIdx) { return fetcher.getSprite(tile, frames, frameIdx); }
	}
	
	// TODO add a way to specify if a tile should draw the tile underneath it, so fully opaque tiles don't try to draw the bottom one when none of it will be seen.
	
	private final AnimationType main, overlay;
	private final float mainFrameTime, overlayFrameTime;
	
	AnimationProperty(AnimationType main) { this(main, 0); }
	AnimationProperty(AnimationType main, float mainFrameTime) { this(main, mainFrameTime, main, mainFrameTime); }
	AnimationProperty(AnimationType main, float mainFrameTime, AnimationType overlay, float overlayFrameTime) {
		this.main = main;
		this.overlay = overlay;
		this.mainFrameTime = mainFrameTime;
		this.overlayFrameTime = overlayFrameTime;
	}
	
	AtlasRegion getSprite(String connectIndex, Tile tile) {
		return getSprite(connectIndex, tile, tile.getType());
	}
	AtlasRegion getSprite(String connectIndex, Tile tile, TileType type) {
		return getSprite(connectIndex, tile, type, GameCore.getElapsedProgramTime());
	}
	AtlasRegion getSprite(String connectIndex, Tile tile, float timeElapsed) {
		return getSprite(connectIndex, tile, tile.getType(), timeElapsed);
	}
	AtlasRegion getSprite(String connectIndex, Tile tile, TileType type, float timeElapsed) {
		Array<AtlasRegion> frames = tileConnectAnimations.get(type.name().toLowerCase()).get(connectIndex);
		
		if(connectIndex.equals("00"))
			return main.getSprite(tile, frames, (int)(timeElapsed/mainFrameTime));
		
		return overlay.getSprite(tile, frames, (int)(timeElapsed/overlayFrameTime));
	}
	
	@Override
	public Integer[] getInitData() { return new Integer[0]; }
	
	/*static class Animated extends AnimationType {
		
		private boolean initialized = false;
		private final float frameTime;
		private Animation<TextureRegion> animation;
		
		public Animated(float frameTime) {
			super(false);
			this.frameTime = frameTime;
			//GameCore.tileConnectionAtlas.getRegions().get(0).name;
		}
		
		@Override
		void initialize(Array<AtlasRegion> frames) {
			animation = new Animation<>(frameTime, frames);
			initialized = true;
		}
		
		@Override
		protected TextureRegion getSprite(Tile tile, float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			return animation.getKeyFrame(timeElapsed, true);
		}
	}
	
	static class RandomFrame extends AnimationType {
		private boolean initialized = false;
		private final float frameTime;
		private Array<? extends TextureRegion> frames;
		
		public RandomFrame(float frameTime) {
			this(frameTime, frameTime);
		}
		public RandomFrame(float frameTime, float overlayFrameTime) {
			super(false);
			this.frameTime = frameTime;
		}
		
		@Override
		void initialize(Array<AtlasRegion> frames) {
			this.frames = frames;
			initialized = true;
		}
		
		@Override
		protected TextureRegion getSprite(Tile tile, float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			MathUtils.random.setSeed((long)(1/frameTime*timeElapsed) + tile.getCenterX() * tile.getCenterX() + tile.getCenterY());
			return frames.get(MathUtils.random(frames.size-1));
		}
	}
	
	
	
	static class SingleFrame extends AnimationType {
		
		private boolean initialized = false;
		private TextureRegion texture;
		
		public SingleFrame() { super(false); }
		public SingleFrame(boolean connects) { super(connects); }
		
		@Override
		void initialize(Array<AtlasRegion> frames) {
			texture = frames.size > 0 ? frames.get(0) : null;
			initialized = frames.size > 0;
		}
		
		@Override
		protected TextureRegion getSprite(Tile tile, float timeElapsed) {
			if(!initialized)
				throw new IllegalStateException("Attempted to access sprite from uninitialized AnimationProperty.");
			
			return texture;
		}
	}*/
}
