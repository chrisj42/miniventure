package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.world.management.Level;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TransitionManager implements TileProperty {
	
	public static final TransitionManager NONE = new TransitionManager(new TransitionMap(), new TransitionMap());
	
	// note about exit animations: since the server doesn't remember midway transitions, it's important that any and all tile change actions happen *after* the exit animation plays, when the tile is actually changing. Otherwise it could be exploited, if say the items dropped and then the player quit quickly, causing both the items and the tile to be saved.
	// Fortunately, I believe that I have tile animation set up in such a way that tile change actions do actually 
	
	private static class TransitionMap extends HashMap<String, TileTransition> {}
	
	public static class TransitionBuilder {
		private final TransitionMap entranceAnimations = new TransitionMap();
		private final TransitionMap exitAnimations = new TransitionMap();
		private final TileType tileType;
		
		TransitionBuilder(TileType tileType) {
			this.tileType = tileType;
		}
		
		private void addAnimation(TransitionMap map, String name, RenderStyle renderStyle, TileType... triggerTypes) {
			Array<TextureHolder> frames = TileAnimType.Transition.fetchMap(tileType).getFrames(name);
			if(frames == null)
				throw new SpriteNotFoundException("transition animation '"+name+"' was not found for tile type "+tileType);
			// GameCore.debug(fps+"fps, "+frames+" frames = "+duration+" seconds duration");
			map.put(name, new TileTransition(renderStyle.getAnimation(frames), triggerTypes));
		}
		
		public TransitionBuilder addEntrance(String name, RenderStyle renderStyle, TileType... triggerTypes) {
			// GameCore.debug("adding enter to "+tileType);
			addAnimation(entranceAnimations, name, renderStyle, triggerTypes);
			return this;
		}
		public TransitionBuilder addEntrance(String name, RenderStyle renderStyle, TileTypeSource... triggerTypes) {
			return addEntrance(name, renderStyle, ArrayUtils.mapArray(triggerTypes, TileType.class, TileTypeSource::getType));
		}
		
		public TransitionBuilder addExit(String name, RenderStyle renderStyle, TileType... triggerTypes) {
			// GameCore.debug("adding exit to "+tileType);
			addAnimation(exitAnimations, name, renderStyle, triggerTypes);
			return this;
		}
		public TransitionBuilder addExit(String name, RenderStyle renderStyle, TileTypeSource... triggerTypes) {
			return addExit(name, renderStyle, ArrayUtils.mapArray(triggerTypes, TileType.class, TileTypeSource::getType));
		}
		
		TransitionManager build() {
			return new TransitionManager(
				(TransitionMap) entranceAnimations.clone(), (TransitionMap) exitAnimations.clone()
			);
		}
	}
	
	// private final TileType tileType;
	private final TransitionMap entranceAnimations;
	private final TransitionMap exitAnimations;
	
	// public TransitionManager(@NotNull TransitionManager manager) {
		// 	this(manager.tileType);
		// 	entranceAnimations.putAll(manager.entranceAnimations);
		// 	exitAnimations.putAll(manager.exitAnimations);
	public TransitionManager(TransitionMap entranceAnimations, TransitionMap exitAnimations) {
		this.entranceAnimations = entranceAnimations;
		this.exitAnimations = exitAnimations;
	}
	
	@Override
	public void registerDataTags(TileType tileType) {
		if(entranceAnimations.size() > 0 || exitAnimations.size() > 0) {
			// tileType.addDataTag(TileDataTag.AnimationStart);
			// tileType.addDataTag(TileDataTag.TransitionName);
			// tileType.addDataTag(TileDataTag.TransitionMode);
			// tileType.addDataTag(TileDataTag.TransitionTile);
			tileType.addDataTag(TileDataTag.Transition);
		}
	}
	
	
	/*@Nullable
	private TileTransition getAnimationStyle(TransitionMode mode, String name) {
		TileTransition animation = null;
		if(mode == TransitionMode.ENTERING)
			animation = entranceAnimations.get(name);
		else if(mode == TransitionMode.EXITING)
			animation = exitAnimations.get(name);
		
		return animation;
	}*/
	
	
	// enter animation
	public boolean tryStartAnimation(@NotNull Tile.TileContext context, @NotNull TileType previous) {
		return tryStartAnimation(context, true, previous, false);
	}
	// exit animation
	public boolean tryStartAnimation(@NotNull Tile.TileContext context, @NotNull TileType next, boolean addNext) {
		return tryStartAnimation(context, false, next, addNext);
	}
	// check for transition animation; tiletype is being entered or removed, and given what tile type will be the main one next.
	// otherType is the tile type that came before entrance, or will come after exit
	private boolean tryStartAnimation(@NotNull Tile.TileContext context, boolean isEntering, @NotNull TileType otherType, boolean addNext) { // addNext is ignored if isEntering is true
		TransitionMap animations = isEntering ? entranceAnimations : exitAnimations;
		for(TileTransition animation: animations.values()) {
			if(animation.isTriggerType(otherType)) {
				MyUtils.debug("Server starting tile transition for tile "+context+", triggered by tiletype "+otherType+", with enter="+isEntering);
				// TileDataMap dataMap = tile.getDataMap(tileType);
				// TileDataMap cacheMap = tile.getDataMap(tileType);
				// context.setData(TileDataTag.TransitionName, animation.name);
				float startTime = context.getWorld().getGameTime();
				// context.setData(TileDataTag.AnimationStart, start);
				// context.setData(TileDataTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				// if(addNext)
				// 	context.setData(TileDataTag.TransitionTile, otherType);
				// else
				// 	context.setData(TileDataTag.TransitionTile, null);
				
				context.setData(TileDataTag.Transition, new ActiveTileTransition(
					animation, isEntering, startTime, addNext ? otherType : null
				));
				
				((Level)context.getLevel()).onTileUpdate(context.getTile());
				return true;
			}
		}
		
		return false;
	}
	
	/*void resetAnimation(@NotNull Tile tile) {
		SerialMap map = tile.getDataMap(tileType);
		map.put(TileDataTag.TransitionStart, tile.getWorld().getGameTime());
	}*/
	
	/*private boolean isTransitionMode(@NotNull Tile.TileContext context, @NotNull TransitionMode mode) {
		// TileDataMap map = tile.getDataMap(tileType);
		TransitionMode curMode = context.getData(TileDataTag.TransitionMode);
		return (curMode == null ? TransitionMode.NONE : curMode) == mode;
	}*/
	
	public boolean playingAnimation(@NotNull Tile.TileContext context) {
		return context.getData(TileDataTag.Transition) != null;
	}
	 public boolean playingEntranceAnimation(@NotNull Tile.TileContext context) {
		ActiveTileTransition transition = context.getData(TileDataTag.Transition);
		return transition != null && transition.entering;
	 }
	 public boolean playingExitAnimation(@NotNull Tile.TileContext context) {
		 ActiveTileTransition transition = context.getData(TileDataTag.Transition);
		 return transition != null && !transition.entering;
	 }
	
	public float tryFinishAnimation(@NotNull Tile.TileContext context) {
		// TileDataMap dataMap = context.getDataMap(tileType);
		// TileDataMap cacheMap = tile.getDataMap(tileType);
		
		ActiveTileTransition anim = context.getData(TileDataTag.Transition);
		
		if(anim == null)
			return 0;
		
		float now = context.getWorld().getGameTime();
		float prev = anim.startTime;
		float timeElapsed = now - prev;
		
		if(timeElapsed < anim.transition.getDuration())
			return anim.transition.getDuration() - timeElapsed;
		
		MyUtils.debug("Server ending tile transition for "+context);
		
		// TransitionMode mode = context.clearData(TileDataTag.TransitionMode);
		// context.clearData(TileDataTag.AnimationStart);
		// context.clearData(TileDataTag.TransitionName);
		// TileType nextType = context.clearData(TileDataTag.TransitionTile);
		context.clearData(TileDataTag.Transition);
		
		// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
		
		Tile tile = context.getTile();
		boolean update = true;
		if(!anim.entering) {
			update = !tile.breakTile(anim.nextType); // successful breakage will handle the update
		}
		
		if(update) // entering, or exit where tile could not be removed
			tile.getLevel().onTileUpdate(tile);
		
		return 0;
	}
	
	// store this in DataMap, along with trans start time?
	/*public enum TransitionMode {
		ENTERING, EXITING, NONE
	}*/
}
