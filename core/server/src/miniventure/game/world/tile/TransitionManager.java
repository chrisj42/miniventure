package miniventure.game.world.tile;

import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.tile.TileType.TileTypeEnum;

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
	public void registerDataTags(TileType tileType) {
		if(entranceAnimations.size() > 0 || exitAnimations.size() > 0) {
			tileType.addDataTag(TileDataTag.AnimationStart);
			tileType.addDataTag(TileDataTag.TransitionName);
			tileType.addDataTag(TileDataTag.TransitionMode);
			tileType.addDataTag(TileDataTag.TransitionTile);
		}
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
	public boolean tryStartAnimation(@NotNull Tile.TileContext context, @NotNull TileTypeEnum previous) {
		return tryStartAnimation(context, true, previous, false);
	}
	// exit animation
	public boolean tryStartAnimation(@NotNull Tile.TileContext context, @NotNull TileTypeEnum next, boolean addNext) {
		return tryStartAnimation(context, false, next, addNext);
	}
	// check for transition animation; tiletype is being entered or removed, and given what tile type will be the main one next.
	private boolean tryStartAnimation(@NotNull Tile.TileContext context, boolean isEntering, @NotNull TileTypeEnum nextType, boolean addNext) { // addNext is ignored if isEntering is true
		HashMap<String, ServerTileTransition> animations = isEntering ? entranceAnimations : exitAnimations;
		for(ServerTileTransition animation: animations.values()) {
			if(animation.isTriggerType(nextType)) {
				GameCore.debug("Server starting tile transition for tile "+context+", triggered by tiletype "+nextType+", with enter="+isEntering);
				// TileDataMap dataMap = tile.getDataMap(tileType);
				// TileDataMap cacheMap = tile.getDataMap(tileType);
				context.setData(TileDataTag.TransitionName, animation.name);
				float start = context.getWorld().getGameTime();
				context.setData(TileDataTag.AnimationStart, start);
				context.setData(TileDataTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				if(addNext)
					context.setData(TileDataTag.TransitionTile, nextType);
				else
					context.setData(TileDataTag.TransitionTile, null);
				((ServerLevel)context.getTile().getLevel()).onTileUpdate(((ServerTile)context.getTile()), tileType);
				return true;
			}
		}
		
		return false;
	}
	
	/*void resetAnimation(@NotNull Tile tile) {
		SerialMap map = tile.getDataMap(tileType);
		map.put(TileDataTag.TransitionStart, tile.getWorld().getGameTime());
	}*/
	
	private boolean isTransitionMode(@NotNull Tile.TileContext context, @NotNull TransitionMode mode) {
		// TileDataMap map = tile.getDataMap(tileType);
		TransitionMode curMode = context.getData(TileDataTag.TransitionMode);
		return (curMode == null ? TransitionMode.NONE : curMode) == mode;
	}
	
	public boolean playingAnimation(@NotNull Tile.TileContext context) { return !isTransitionMode(context, TransitionMode.NONE); }
	public boolean playingEntranceAnimation(@NotNull Tile.TileContext context) { return isTransitionMode(context, TransitionMode.ENTERING); }
	public boolean playingExitAnimation(@NotNull Tile.TileContext context) { return isTransitionMode(context, TransitionMode.EXITING); }
	
	public float tryFinishAnimation(@NotNull Tile.TileContext context) {
		// TileDataMap dataMap = context.getDataMap(tileType);
		// TileDataMap cacheMap = tile.getDataMap(tileType);
		
		ServerTileTransition anim = getAnimationStyle(context.getData(TileDataTag.TransitionMode), context.getData(TileDataTag.TransitionName));
		
		if(anim == null)
			return 0;
		
		float now = context.getWorld().getGameTime();
		float prev = context.getData(TileDataTag.AnimationStart);
		float timeElapsed = now - prev;
		
		if(timeElapsed < anim.getDuration())
			return anim.getDuration() - timeElapsed;
		
		GameCore.debug("Server ending tile transition for "+context);
		
		TransitionMode mode = context.clearData(TileDataTag.TransitionMode);
		context.clearData(TileDataTag.AnimationStart);
		context.clearData(TileDataTag.TransitionName);
		TileTypeEnum nextType = context.clearData(TileDataTag.TransitionTile);
		
		// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
		
		ServerTile tile = context.getTile();
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
