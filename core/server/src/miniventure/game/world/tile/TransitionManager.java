package miniventure.game.world.tile;

import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
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
				GameCore.debug("Server starting tile transition for tile "+tile+", triggered by tiletype "+nextType+", with enter="+isEntering);
				SerialMap dataMap = tile.getDataMap(tile.getType().getTypeEnum());
				dataMap.put(TileCacheTag.TransitionName, animation.name);
				float start = tile.getWorld().getGameTime();
				dataMap.put(TileCacheTag.TransitionStart, start);
				dataMap.put(TileCacheTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				if(addNext)
					dataMap.put(TileCacheTag.TransitionTile, nextType.getTypeEnum());
				else
					dataMap.remove(TileCacheTag.TransitionTile);
				tile.getLevel().onTileUpdate(tile);
				return true;
			}
		}
		
		return false;
	}
	
	/*void resetAnimation(@NotNull Tile tile) {
		SerialMap map = tile.getDataMap(tileType);
		map.put(TileCacheTag.TransitionStart, tile.getWorld().getGameTime());
	}*/
	
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
		
		GameCore.debug("Server ending tile transition for "+tile);
		
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
