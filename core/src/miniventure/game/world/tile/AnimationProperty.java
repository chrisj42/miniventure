package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class AnimationProperty implements TileProperty {
	
	private static HashMap<String, HashMap<String, Array<AtlasRegion>>> tileConnectionAnimations = new HashMap<>();
	private static HashMap<String, HashMap<String, Array<AtlasRegion>>> tileOverlapAnimations = new HashMap<>();
	static {
		Array<AtlasRegion> regions = GameCore.tileAtlas.getRegions();
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
			MathUtils.random.setSeed((long)(frameIdx) + tile.getCenterX() * tile.getCenterX() + tile.getCenterY());
			return frames.get(MathUtils.random(frames.size-1));
		}),
		
		SINGLE_FRAME((tile, frames, frameIdx) -> frames.get(0));
		
		private final SpriteFetcher fetcher;
		
		AnimationType(SpriteFetcher fetcher) { this.fetcher = fetcher; }
		
		public AtlasRegion getSprite(Tile tile, Array<AtlasRegion> frames, int frameIdx) { return fetcher.getSprite(tile, frames, frameIdx); }
	}
	
	//private final AnimationType main, overlay;
	//private final float mainFrameTime, overlayFrameTime;
	private final boolean isOpaque;
	private final TileAnimation main, overlay;
	private TileType tileType;
	
	AnimationProperty(boolean isOpaque, AnimationType main) { this(isOpaque, main, 0); }
	//AnimationProperty(boolean isTransparent, AnimationType main) { this(isTransparent, main, 0); }
	AnimationProperty(boolean isOpaque, AnimationType main, float mainFrameTime) { this(isOpaque, main, mainFrameTime, main, mainFrameTime); }
	//AnimationProperty(boolean isTransparent, AnimationType main, float mainFrameTime) { this(isTransparent, main, mainFrameTime, main, mainFrameTime); }
	AnimationProperty(boolean isOpaque, AnimationType main, float mainFrameTime, AnimationType overlay, float overlayFrameTime) {
		this.main = new TileAnimation(main, mainFrameTime, tileConnectionAnimations);
		this.overlay = new TileAnimation(overlay, overlayFrameTime, tileOverlapAnimations);
		this.isOpaque = isOpaque;
	}
	
	@Override
	public void init(TileType type) { this.tileType = type; }
	
	public boolean isOpaque() { return isOpaque; }
	
	/*@Nullable
	public TileType getRenderedBehind() {
		if(!isTransparent) return null;
		return tileType.getProp(CoveredTileProperty.class).getCoveredTile();
	}*/
	
	/*AtlasRegion getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile) {
		return getSprite(spriteIndex, isOverlapSprite, tile, tile.getType());
	}*/
	AtlasRegion getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile) {
		return getSprite(spriteIndex, isOverlapSprite, tile, GameCore.getElapsedProgramTime());
	}
	AtlasRegion getSprite(int spriteIndex, boolean isOverlapSprite, Tile tile, float timeElapsed) {
		if(!isOverlapSprite && !tile.hasType(tileType))
			System.err.println("Warning: fetching sprite for tile "+tile+" that doesn't have the intended type " + tileType);
		// it is intended 
		//if(isOverlapSprite && !tileType.getProp(OverlapProperty.class).overlaps)
		//	System.err.println("Warning: fetching overlap sprite on tile "+tile+" for tile type " + tileType + " which does not overlap other tiles");
		
		TileAnimation animation = isOverlapSprite ? overlay : main;
		
		return animation.getSprite(tile, spriteIndex, timeElapsed);
		
		//Array<AtlasRegion> frames;
		//String typeName = tileType.name().toLowerCase();
		//String indexString = (spriteIndex < 10 ? "0" : "") + spriteIndex;
		/*if(isOverlapSprite) {
			frames = tileOverlapAnimations.get(typeName).get(indexString);
			return overlay.getSprite(tile, frames, (int)(timeElapsed/overlayFrameTime));
		} else {
			frames = tileConnectionAnimations.get(typeName).get(indexString);
			//if(!indexString.equals("00"))
			//	System.out.println("fetching sprites for " + typeName + " at " + indexString + " as a connection sprite, from the main animation type " + main + "; frames="+frames);
			return main.getSprite(tile, frames, (int)(timeElapsed/mainFrameTime));
		}*/
	}
}
