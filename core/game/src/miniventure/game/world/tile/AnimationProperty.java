package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class AnimationProperty extends TileProperty {
	
	private static HashMap<String, HashMap<String, Array<TextureHolder>>> tileConnectionAnimations = new HashMap<>();
	private static HashMap<String, HashMap<String, Array<TextureHolder>>> tileOverlapAnimations = new HashMap<>();
	static {
		Array<TextureHolder> regions = GameCore.tileAtlas.getRegions();
		for(TextureHolder region: regions) {
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
		private final HashMap<String, HashMap<String, Array<TextureHolder>>> tileAnimationFrames;
		
		TileAnimation(AnimationType animationType, float frameTime, HashMap<String, HashMap<String, Array<TextureHolder>>> tileAnimationFrames) {
			this.animationType = animationType;
			this.frameTime = frameTime;
			this.tileAnimationFrames = tileAnimationFrames;
		}
		
		TextureHolder getSprite(Tile tile, int spriteIndex, float timeElapsed) {
			String typeName = tileType.name().toLowerCase();
			String indexString = (spriteIndex < 10 ? "0" : "") + spriteIndex;
			return animationType.getSprite(tile, tileAnimationFrames.get(typeName).get(indexString), (int)(timeElapsed/frameTime));
		}
	}
	
	@FunctionalInterface
	private interface SpriteFetcher {
		TextureHolder getSprite(Tile tile, Array<TextureHolder> frames, int frameIdx);
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
		
		public TextureHolder getSprite(Tile tile, Array<TextureHolder> frames, int frameIdx) { return fetcher.getSprite(tile, frames, frameIdx); }
	}
	
	private final boolean isOpaque;
	private final TileAnimation main, overlay;
	
	AnimationProperty(@NotNull TileType tileType, boolean isOpaque, AnimationType main) { this(tileType, isOpaque, main, 0); }
	AnimationProperty(@NotNull TileType tileType, boolean isOpaque, AnimationType main, float mainFrameTime) { this(tileType, isOpaque, main, mainFrameTime, main, mainFrameTime); }
	AnimationProperty(@NotNull TileType tileType, boolean isOpaque, AnimationType main, float mainFrameTime, AnimationType overlay, float overlayFrameTime) {
		super(tileType);
		this.main = new TileAnimation(main, mainFrameTime, tileConnectionAnimations);
		this.overlay = new TileAnimation(overlay, overlayFrameTime, tileOverlapAnimations);
		this.isOpaque = isOpaque;
	}
	
	public boolean isOpaque() { return isOpaque; }
	
	TextureHolder getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile) {
		return getSprite(spriteIndex, isOverlapSprite, tile, GameCore.getElapsedProgramTime());
	}
	TextureHolder getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile, float timeElapsed) {
		if(!isOverlapSprite && !tile.hasType(tileType))
			System.err.println("Warning: fetching sprite for tile "+tile+" that doesn't have the intended type " + tileType);
		
		TileAnimation animation = isOverlapSprite ? overlay : main;
		
		return animation.getSprite(tile, spriteIndex, timeElapsed);
	}
}
