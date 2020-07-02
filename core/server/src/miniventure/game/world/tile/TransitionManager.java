package miniventure.game.world.tile;

import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.tile.TileCacheTag.TileDataCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransitionManager {
	
	// note about exit animations: since the server doesn't remember midway transitions, it's important that any and all tile change actions happen *after* the exit animation plays, when the tile is actually changing. Otherwise it could be exploited, if say the items dropped and then the player quit quickly, causing both the items and the tile to be saved.
	// Fortunately, I believe that I have tile animation set up in such a way that tile change actions do actually 
	
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
	
	private void addAnimation(HashMap<String, ServerTileTransition> map, String name, float fps, TileTypeEnum... triggerTypes) {
		int frames = GameCore.tileAtlas.countRegions(tileType.name().toLowerCase()+"/t"+name);
		float duration = frames / fps;
		// GameCore.debug(fps+"fps, "+frames+" frames = "+duration+" seconds duration");
		map.put(name, new ServerTileTransition(name, duration, triggerTypes));
	}
	
	public TransitionManager addEntrance(String name, float fps, TileTypeEnum... triggerTypes) {
		// GameCore.debug("adding enter to "+tileType);
		addAnimation(entranceAnimations, name, fps, triggerTypes);
		return this;
	}
	
	public TransitionManager addExit(String name, float fps, TileTypeEnum... triggerTypes) {
		// GameCore.debug("adding exit to "+tileType);
		addAnimation(exitAnimations, name, fps, triggerTypes);
		return this;
	}
	
	
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
	private boolean tryStartAnimation(@NotNull ServerTile tile, boolean isEntering, @NotNull TileType nextType, boolean addNext) { // addNext is ignored if isEntering is true
		HashMap<String, ServerTileTransition> animations = isEntering ? entranceAnimations : exitAnimations;
		for(ServerTileTransition animation: animations.values()) {
			if(animation.isTriggerType(nextType)) {
				MyUtils.debug("Server starting tile transition for tile "+tile+", triggered by tiletype "+nextType+", with enter="+isEntering);
				// TileDataMap dataMap = tile.getDataMap(tileType);
				// TileDataCache cacheMap = tile.getCacheMap(tileType);
				TransitionData transitionData = new TransitionData(animation.name, tile.getWorld().getGameTime(), isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING, addNext ? nextType.getTypeEnum() : null);
				// dataMap.put(TileDataTag.TransitionName, animation.name);
				// float start = tile.getWorld().getGameTime();
				// cacheMap.put(TileCacheTag.AnimationStart, start);
				// cacheMap.put(TileCacheTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				// if(addNext)
				// 	cacheMap.put(TileCacheTag.TransitionTile, nextType);
				// else
				// 	cacheMap.remove(TileCacheTag.TransitionTile);
				tile.getLevel().tileTransitions.put(tile, transitionData);
				// tile.getLevel().onTileUpdate(tile, tileType);
				return true;
			}
		}
		
		return false;
	}
	
	/*void resetAnimation(@NotNull Tile tile) {
		SerialMap map = tile.getDataMap(tileType);
		map.put(TileCacheTag.TransitionStart, tile.getWorld().getGameTime());
	}*/
	
	private boolean isTransitionMode(@NotNull ServerTile tile, TransitionMode mode) {
		// TileDataCache map = tile.getCacheMap(tileType);
		TransitionData data = tile.getLevel().tileTransitions.get(tile);
		return (data == null ? TransitionMode.NONE : data.mode) == mode;
	}
	
	public boolean playingAnimation(@NotNull ServerTile tile) { return !isTransitionMode(tile, TransitionMode.NONE); }
	public boolean playingEntranceAnimation(@NotNull ServerTile tile) { return isTransitionMode(tile, TransitionMode.ENTERING); }
	public boolean playingExitAnimation(@NotNull ServerTile tile) { return isTransitionMode(tile, TransitionMode.EXITING); }
	
	public float tryFinishAnimation(@NotNull ServerTile tile) {
		// TileDataMap dataMap = tile.getDataMap(tileType);
		// TileDataCache cacheMap = tile.getCacheMap(tileType);
		ServerLevel level = tile.getLevel();
		
		TransitionData data = level.tileTransitions.get(tile);
		if(data == null)
			return 0;
		
		ServerTileTransition anim = getAnimationStyle(data.mode, data.name);
		
		if(anim == null)
			return 0;
		
		float now = tile.getWorld().getGameTime();
		float prev = data.startTime;
		float timeElapsed = now - prev;
		
		if(timeElapsed < anim.getDuration())
			return anim.getDuration() - timeElapsed;
		
		MyUtils.debug("Server ending tile transition for "+tile);
		
		level.tileTransitions.clear(tile);
		// TransitionMode mode = cacheMap.remove(TileCacheTag.TransitionMode);
		// cacheMap.remove(TileCacheTag.AnimationStart);
		// dataMap.remove(TileDataTag.TransitionName);
		// TileTypeInfo nextType = cacheMap.remove(TileCacheTag.TransitionTile);
		
		// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
		
		boolean update = true;
		if(data.mode == TransitionMode.EXITING) {
			update = !tile.breakTile(level.getWorld().getTileType(data.nextType)); // successful breakage will handle the update
		}
		
		if(update) // entering, or exit where tile could not be removed
			level.onTileUpdate(tile);
		
		return 0;
	}
	
	private static class ServerTileTransition {
		
		private final String name;
		private final float duration;
		private final EnumSet<TileTypeEnum> triggerTypes;
		
		ServerTileTransition(String name, float duration, TileTypeEnum... triggerTypes) {
			this.name = name;
			this.duration = duration;
			this.triggerTypes = MyUtils.enumSet(triggerTypes);
			// if triggertypes is empty, then anything triggers it
		}
		
		boolean isTriggerType(TileType type) { return isTriggerType(type.getTypeEnum()); }
		boolean isTriggerType(TileTypeEnum type) {
			return triggerTypes.size() == 0 || triggerTypes.contains(type);
		}
		
		float getDuration() { return duration; }
	}
}
