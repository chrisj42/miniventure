package miniventure.game.world.tilenew;

import java.util.HashMap;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class AnimationProperty implements TilePropertyInstance {
	
	private static HashMap<String, HashMap<String, Array<AtlasRegion>>> tileConnectionAnimations = new HashMap<>();
	private static HashMap<String, HashMap<String, Array<AtlasRegion>>> tileOverlapAnimations = new HashMap<>();
	static {
		Array<AtlasRegion> regions = GameCore.tileAtlas != null ? GameCore.tileAtlas.getRegions() : new Array<>();
		for(AtlasRegion region: regions) {
			String tilename = region.name.substring(0, region.name.indexOf("/"));
			String spriteIdx = region.name.substring(region.name.indexOf("/")+1);
			boolean isOverlap = spriteIdx.startsWith("o");
			if(isOverlap) {
				spriteIdx = spriteIdx.substring(1);
				tileOverlapAnimations.computeIfAbsent(tilename, k -> new HashMap<>());
				tileOverlapAnimations.get(tilename).computeIfAbsent(spriteIdx, k -> new Array<>());
				tileOverlapAnimations.get(tilename).get(spriteIdx).add(region);
			} else {
				tileConnectionAnimations.computeIfAbsent(tilename, k -> new HashMap<>());
				tileConnectionAnimations.get(tilename).computeIfAbsent(spriteIdx, k -> new Array<>());
				tileConnectionAnimations.get(tilename).get(spriteIdx).add(region);
			}
		}
	}
	
	private class TileAnimation {
		private final AnimationType animationType;
		private final float frameTime;
		private final HashMap<String, HashMap<String, Array<AtlasRegion>>> tileAnimationFrames;
		
		TileAnimation(AnimationType animationType, float frameTime, HashMap<String, HashMap<String, Array<AtlasRegion>>> tileAnimationFrames) {
			this.animationType = animationType;
			this.frameTime = frameTime;
			this.tileAnimationFrames = tileAnimationFrames;
		}
		
		AtlasRegion getSprite(Tile tile, int spriteIndex, float timeElapsed) {
			String typeName = tileType.name().toLowerCase();
			String indexString = (spriteIndex < 10 ? "0" : "") + spriteIndex;
			return animationType.getSprite(tile, tileAnimationFrames.get(typeName).get(indexString), (int)(timeElapsed/frameTime));
		}
	}
	
	@FunctionalInterface
	private interface SpriteFetcher {
		AtlasRegion getSprite(Tile tile, Array<AtlasRegion> frames, int frameIdx);
	}
	
	enum AnimationType {
		SEQUENCE((tile, frames, frameIdx) -> frames.get(frameIdx % frames.size)),
		
		RANDOM((tile, frames, frameIdx) -> {
			MathUtils.random.setSeed((long)(frameIdx) + tile.getPosition().hashCode() * 17);
			return frames.get(MathUtils.random(frames.size-1));
		}),
		
		SINGLE_FRAME((tile, frames, frameIdx) -> frames.get(0));
		
		private final SpriteFetcher fetcher;
		
		AnimationType(SpriteFetcher fetcher) { this.fetcher = fetcher; }
		
		public AtlasRegion getSprite(Tile tile, Array<AtlasRegion> frames, int frameIdx) { return fetcher.getSprite(tile, frames, frameIdx); }
	}
	
	private final boolean isOpaque;
	private final TileAnimation main, overlay;
	private final TileType tileType;
	
	AnimationProperty(@NotNull TileType tileType, boolean isOpaque, AnimationType main) { this(tileType, isOpaque, main, 0); }
	AnimationProperty(@NotNull TileType tileType, boolean isOpaque, AnimationType main, float mainFrameTime) { this(tileType, isOpaque, main, mainFrameTime, main, mainFrameTime); }
	AnimationProperty(@NotNull TileType tileType, boolean isOpaque, AnimationType main, float mainFrameTime, AnimationType overlay, float overlayFrameTime) {
		this.tileType = tileType;
		this.main = new TileAnimation(main, mainFrameTime, tileConnectionAnimations);
		this.overlay = new TileAnimation(overlay, overlayFrameTime, tileOverlapAnimations);
		this.isOpaque = isOpaque;
	}
	
	public boolean isOpaque() { return isOpaque; }
	
	AtlasRegion getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile) {
		return getSprite(spriteIndex, isOverlapSprite, tile, GameCore.getElapsedProgramTime());
	}
	AtlasRegion getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile, float timeElapsed) {
		if(!isOverlapSprite && !tile.hasType(tileType))
			System.err.println("Warning: fetching sprite for tile "+tile+" that doesn't have the intended type " + tileType);
		
		TileAnimation animation = isOverlapSprite ? overlay : main;
		
		return animation.getSprite(tile, spriteIndex, timeElapsed);
	}
}
