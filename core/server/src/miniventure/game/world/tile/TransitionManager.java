package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.util.customenum.SerialMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransitionManager {
	
	private final TileTypeEnum tileType;
	private final HashMap<String, ServerTileTransition> entranceAnimations = new HashMap<>();
	private final HashMap<String, ServerTileTransition> exitAnimations = new HashMap<>();
	
	public TransitionManager(@NotNull TileTypeEnum tileType) {
		this.tileType = tileType;
	}
	public TransitionManager(@NotNull TransitionManager manager) {
		this(manager.tileType);
		entranceAnimations.putAll(manager.entranceAnimations);
		exitAnimations.putAll(manager.exitAnimations);
	}
	
	public TransitionManager addEntranceAnimations(@NotNull ServerTileTransition... animations) {
		for(ServerTileTransition transition: animations)
			entranceAnimations.put(transition.name, transition);
		
		return this;
	}
	
	public TransitionManager addExitAnimations(@NotNull ServerTileTransition... animations) {
		for(ServerTileTransition transition: animations)
			exitAnimations.put(transition.name, transition);
		
		return this;
	}
	
	
	/*@Nullable
	private TransitionAnimation getAnimationStyle(DataMap dataMap) {
		TransitionMode mode = dataMap.getOrDefault(CacheTag.TransitionMode, TransitionMode.NONE);
		String name = dataMap.get(CacheTag.TransitionName);
		return getAnimationStyle(mode, name);
	}*/
	@Nullable
	private ServerTileTransition getAnimationStyle(TransitionMode mode, String name) {
		ServerTileTransition animation = null;
		if(mode == TransitionMode.ENTERING)
			animation = entranceAnimations.get(name);
		else if(mode == TransitionMode.EXITING)
			animation = exitAnimations.get(name);
		
		return animation;
	}
	
	
	// enter animation
	public boolean tryStartAnimation(@NotNull ServerTile tile, @NotNull TileType previous) {
		return tryStartAnimation(tile, true, previous, false);
	}
	// exit animation
	public boolean tryStartAnimation(@NotNull ServerTile tile, @NotNull TileType next, boolean addNext) {
		return tryStartAnimation(tile, false, next, addNext);
	}
	// check for transition animation; tiletype is being entered or removed, and given what tile type will be the main one next.
	private boolean tryStartAnimation(@NotNull ServerTile tile, boolean isEntering, @NotNull TileType other, boolean addNext) { // addNext is ignored if isEntering is true
		HashMap<String, ServerTileTransition> animations = isEntering ? entranceAnimations : exitAnimations;
		for(ServerTileTransition animation: animations.values()) {
			if(animation.isTriggerType(other)) {
				SerialMap dataMap = tile.getDataMap();
				dataMap.put(TileCacheTag.TransitionName, animation.name);
				float start = tile.getWorld().getGameTime();
				dataMap.put(TileCacheTag.TransitionStart, start);
				dataMap.put(TileCacheTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				if(addNext)
					dataMap.put(TileCacheTag.TransitionTile, other.getTypeEnum());
				else
					dataMap.remove(TileCacheTag.TransitionTile);
				tile.getLevel().onTileUpdate(tile);
				return true;
			}
		}
		
		return false;
	}
	
	/*TileAnimation<TextureHolder> getTransitionSprite(@NotNull Tile tile) {
		SerialMap dataMap = tile.getDataMap(tileType);
		
		TransitionMode mode = dataMap.get(TileCacheTag.TransitionMode);
		String name = dataMap.get(TileCacheTag.TransitionName);
		
		TransitionAnimation animation = getAnimationStyle(mode, name);
		if(animation == null)
			throw new IllegalStateException("Cannot get transition sprite when not transitioning.");
		
		return animation.getAnimation(tileType, name, tileAnimations);
	}*/
	
	/*public float getTimeRemaining(@NotNull Tile tile) {
		if(!playingAnimation(tile)) return 0;
		TransitionAnimation curTransition = getAnimationStyle(tile.getDataMap(tileType));
		if(curTransition == null)
			return 0;
		
		float start = tile.getDataMap(tileType).get(CacheTag.TransitionStart);
		float now = tile.getWorld().getGameTime();
		float duration = curTransition.time;
		return duration - (now - start);
	}*/
	
	void resetAnimation(@NotNull Tile tile) {
		SerialMap map = tile.getDataMap(tileType);
		map.put(TileCacheTag.TransitionStart, tile.getWorld().getGameTime());
	}
	
	private boolean isTransitionMode(@NotNull Tile tile, TransitionMode mode) {
		SerialMap map = tile.getDataMap(tileType);
		return map.getOrDefault(TileCacheTag.TransitionMode, TransitionMode.NONE) == mode;
	}
	
	public boolean playingAnimation(@NotNull Tile tile) { return !isTransitionMode(tile, TransitionMode.NONE); }
	public boolean playingEntranceAnimation(@NotNull Tile tile) { return isTransitionMode(tile, TransitionMode.ENTERING); }
	public boolean playingExitAnimation(@NotNull Tile tile) { return isTransitionMode(tile, TransitionMode.EXITING); }
	
	public float tryFinishAnimation(@NotNull ServerTile tile) {
		SerialMap dataMap = tile.getDataMap(tileType);
		
		ServerTileTransition anim = getAnimationStyle(dataMap.get(TileCacheTag.TransitionMode), dataMap.get(TileCacheTag.TransitionName));
		
		if(anim == null)
			return 0;
		
		float now = tile.getWorld().getGameTime();
		float prev = dataMap.get(TileCacheTag.TransitionStart);
		float timeElapsed = now - prev;
		
		if(timeElapsed < anim.getDuration())
			return anim.getDuration() - timeElapsed;
		
		
		TransitionMode mode = dataMap.remove(TileCacheTag.TransitionMode);
		dataMap.remove(TileCacheTag.TransitionStart);
		dataMap.remove(TileCacheTag.TransitionName);
		TileTypeEnum nextType = dataMap.remove(TileCacheTag.TransitionTile);
		
		// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
		
		if(mode == TransitionMode.EXITING) {
			tile.breakTile(false);
			if(nextType != null)
				tile.addTile(ServerTileType.get(nextType));
		} else
			tile.getLevel().onTileUpdate(tile);
		
		return 0;
	}
}
