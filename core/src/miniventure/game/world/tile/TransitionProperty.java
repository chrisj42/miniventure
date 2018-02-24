package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TransitionProperty implements TileProperty {
	
	private enum Data {
		ANIM, TILE, TIME;
		private final int idx;
		Data() { idx = ordinal(); }
	}
	
	/*
		Can have transitions upon placing the tile, or upon removing the tile.
		There can also be checks to only display the transition if there's a certain tile that it is transitioning to/from.
		
		So, the TransitionProperty involves a variable count of animations, which have:
			- if this is a transition entrance, or exit
			- a list of tiles that one is req to be transitioning to/from
			- a frame rate
	 */
	
	private static final HashMap<TileType, HashMap<String, Array<AtlasRegion>>> allAnimations = new HashMap<>();
	
	private TileType tileType;
	private final @NotNull TransitionAnimation[] animations;
	
	/// note: the sprite name key for the animations is the name of the animation set for that animation; aka, it's what the name of all the images are before the _## at the end.
	public TransitionProperty(@NotNull TransitionAnimation... animations) {
		this.animations = animations;
	}
	
	@Override
	public void init(@NotNull TileType type) {
		this.tileType = type;
		if(animations.length == 0) return;
		
		allAnimations.putIfAbsent(tileType, new HashMap<>());
		HashMap<String, Array<AtlasRegion>> animationMap = allAnimations.get(tileType);
		for(TransitionAnimation anim: animations) {
			if (!animationMap.containsKey(anim.getName()))
				animationMap.put(anim.getName(), GameCore.tileAtlas.findRegions(tileType.name().toLowerCase() + "/" + anim.getName()));
			
			anim.setAnimationTime(animationMap.get(anim.getName()).size);
		}
	}
	
	// enter animation
	public boolean tryStartAnimation(@NotNull Tile tile, @NotNull TileType previous) {
		return tryStartAnimation(tile, true, previous, false);
	}
	// exit animation
	public boolean tryStartAnimation(@NotNull Tile tile, @NotNull TileType next, boolean addNext) {
		return tryStartAnimation(tile, false, next, addNext);
	}
	// check for transition animation; tiletype is being entered or removed, and given what tile type will be the main one next.
	private boolean tryStartAnimation(@NotNull Tile tile, boolean isEntering, @NotNull TileType other, boolean addNext) { // addNext is ignored if isEntering is true
		boolean found = false;
		for(int i = 0; i < animations.length; i++) {
			TransitionAnimation anim = animations[i];
			if(anim.isEntrance() != isEntering) continue;
			if(anim.getReqTiles().length == 0) found = true;
			else for(TileType type: anim.getReqTiles())
				if(type == other)
					found = true;
			
			if(found) {
				tile.setData(getClass(), tileType, Data.ANIM.idx, i+"");
				tile.setData(getClass(), tileType, Data.TILE.idx, (!isEntering && addNext ? other.name() : "")); // if removing, this specifies the tile to add after removal
				tile.setData(getClass(), tileType, Data.TIME.idx, GameCore.getElapsedProgramTime()+"");
				return true;
			}
		}
		
		return false;
	}
	
	public boolean playingAnimation(@NotNull Tile tile) {
		return animations.length > 0 && tile.getData(getClass(), tileType, Data.ANIM.idx).length() > 0;
	}
	
	// throws error if no animation is playing
	public AtlasRegion getAnimationFrame(@NotNull Tile tile) {
		if(!playingAnimation(tile)) throw new IllegalStateException("Not allowed to fetch animation frame for transition when no animation is playing. invoking tile: "+tile);
		
		float timeStarted = Float.parseFloat(tile.getData(getClass(), tileType, Data.TIME.idx));
		float timeElapsed = GameCore.getElapsedProgramTime() - timeStarted;
		
		TransitionAnimation anim = animations[Integer.parseInt(tile.getData(getClass(), tileType, Data.ANIM.idx))];
		
		tryFinishAnimation(tile, timeElapsed, anim); // updates it for next render
		
		Array<AtlasRegion> frames = allAnimations.get(tileType).get(anim.getName());
		int curFrameIdx = Math.min(frames.size-1, (int) (timeElapsed / anim.getFrameRate()));
		// TO-DO perhaps later I could make it so the animation can loop?
		return frames.get(curFrameIdx);
	}
	
	// TODO later, I should make a system so tiles can request constant updates, so they don't have to rely on the render loop. This will be used for this, probably, as well as maybe water..? Actually, water might just need updates whenever an adjacent tile is updated. That would probably be a worthwhile system as well.
	private void tryFinishAnimation(@NotNull Tile tile, float timeElapsed, @NotNull TransitionAnimation anim) {
		if(timeElapsed > anim.getAnimationTime()) {
			tile.setData(getClass(), tileType, Data.TIME.idx, "");
			tile.setData(getClass(), tileType, Data.ANIM.idx, "");
			 
			// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
			String nextTileTypeData = tile.getData(getClass(), tileType, Data.TILE.idx);
			tile.setData(getClass(), tileType, Data.TILE.idx, "");
			
			if(!anim.isEntrance()) {
				tile.breakTile(false);
				if(nextTileTypeData.length() > 0)
					tile.addTile(Enum.valueOf(TileType.class, nextTileTypeData));
			}
		}
	}
	
	@Override
	// first string is index of animation being played, second is name of tiletype that will be placed (assuming this is an exit animation), and third is the duration that the animation has been going so far.
	public String[] getInitData() {
		String[] data = new String[animations.length == 0?0:3];
		Arrays.fill(data, "");
		return data;
	}
}
