package miniventure.game.world.tile;

import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransitionManager implements TileProperty {
	
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
	
	@Override
	public void registerDataTypes(TileType tileType) {
		if(hasTransitions()) {
			tileType.registerData(TileDataTag.LastUpdate);
			tileType.registerData(TileDataTag.TransitionMode);
			tileType.registerData(TileDataTag.TransitionName);
			tileType.registerData(TileDataTag.TransitionTile);
			tileType.registerData(TileDataTag.AnimationStart);
		}
	}
	
	private boolean hasTransitions() {
		return entranceAnimations.size() > 0 || exitAnimations.size() > 0;
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
		return tryStartAnimation(tile, true, new TileTypeInfo(previous), false);
	}
	// exit animation
	public boolean tryStartAnimation(@NotNull ServerTile tile, @NotNull TileTypeInfo next, boolean addNext) {
		return tryStartAnimation(tile, false, next, addNext);
	}
	// check for transition animation; tiletype is being entered or removed, and given what tile type will be the main one next.
	private boolean tryStartAnimation(@NotNull ServerTile tile, boolean isEntering, @NotNull TileTypeInfo nextType, boolean addNext) { // addNext is ignored if isEntering is true
		if(!hasTransitions())
			return false;
		
		HashMap<String, ServerTileTransition> animations = isEntering ? entranceAnimations : exitAnimations;
		for(ServerTileTransition animation: animations.values()) {
			if(animation.isTriggerType(nextType.typeEnum)) {
				MyUtils.debug("Server starting tile transition for tile "+tile+", triggered by tiletype "+nextType.typeEnum +", with enter="+isEntering);
				TileTypeDataMap dataMap = tile.getDataMap(tileType);
				// TileTypeDataMap cacheMap = tile.getDataMap(tileType);
				dataMap.put(TileDataTag.TransitionName, animation.name);
				float start = tile.getWorld().getGameTime();
				dataMap.put(TileDataTag.AnimationStart, start);
				dataMap.put(TileDataTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				if(addNext)
					dataMap.put(TileDataTag.TransitionTile, nextType);
				else
					dataMap.remove(TileDataTag.TransitionTile);
				tile.getLevel().onTileUpdate(tile, tileType);
				return true;
			}
		}
		
		return false;
	}
	
	/*void resetAnimation(@NotNull Tile tile) {
		SerialMap map = tile.getDataMap(tileType);
		map.put(TileDataTag.TransitionStart, tile.getWorld().getGameTime());
	}*/
	
	private boolean isTransitionMode(@NotNull Tile tile, TransitionMode mode) {
		TransitionMode curMode;
		if(hasTransitions()) {
			TileTypeDataMap map = tile.getDataMap(tileType);
			curMode = map.getOrDefault(TileDataTag.TransitionMode, TransitionMode.NONE);
		}
		else
			curMode = TransitionMode.NONE;
		
		return curMode == mode;
	}
	
	public boolean playingAnimation(@NotNull Tile tile) { return !isTransitionMode(tile, TransitionMode.NONE); }
	public boolean playingEntranceAnimation(@NotNull Tile tile) { return isTransitionMode(tile, TransitionMode.ENTERING); }
	public boolean playingExitAnimation(@NotNull Tile tile) { return isTransitionMode(tile, TransitionMode.EXITING); }
	
	public float tryFinishAnimation(@NotNull ServerTile tile) {
		if(!hasTransitions())
			return 0;
		
		// TileTypeDataMap dataMap = tile.getDataMap(tileType);
		TileTypeDataMap dataMap = tile.getDataMap(tileType);
		
		ServerTileTransition anim = getAnimationStyle(dataMap.get(TileDataTag.TransitionMode), dataMap.get(TileDataTag.TransitionName));
		
		if(anim == null)
			return 0;
		
		float now = tile.getWorld().getGameTime();
		float prev = dataMap.get(TileDataTag.AnimationStart);
		float timeElapsed = now - prev;
		
		if(timeElapsed < anim.getDuration())
			return anim.getDuration() - timeElapsed;
		
		MyUtils.debug("Server ending tile transition for "+tile);
		
		TransitionMode mode = dataMap.remove(TileDataTag.TransitionMode);
		dataMap.remove(TileDataTag.AnimationStart);
		dataMap.remove(TileDataTag.TransitionName);
		TileTypeInfo nextType = dataMap.remove(TileDataTag.TransitionTile);
		
		// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
		
		boolean update = true;
		if(mode == TransitionMode.EXITING) {
			update = !tile.breakTile(nextType); // successful breakage will handle the update
		}
		
		if(update) // entering, or exit where tile could not be removed
			tile.getLevel().onTileUpdate(tile, tileType);
		
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
