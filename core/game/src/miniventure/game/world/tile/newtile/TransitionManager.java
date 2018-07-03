package miniventure.game.world.tile.newtile;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.tile.newtile.TileType.TileTypeEnum;
import miniventure.game.world.tile.newtile.data.DataMap;
import miniventure.game.world.tile.newtile.data.DataTag;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransitionManager {
	
	static EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	private final TileTypeEnum tileType;
	private final HashMap<String, TransitionAnimation> entranceAnimations = new HashMap<>();
	private final HashMap<String, TransitionAnimation> exitAnimations = new HashMap<>();
	
	// store this in DataMap, along with trans start time?
	public enum TransitionMode {
		ENTERING, EXITING, NONE
	}
	
	public TransitionManager(@NotNull TileTypeEnum tileType) {
		this.tileType = tileType;
	}
	
	public TransitionManager addEntranceAnimations(@NotNull TransitionAnimation... animations) {
		for(TransitionAnimation transition: animations)
			entranceAnimations.put(transition.name, transition);
		
		return this;
	}
	
	public TransitionManager addExitAnimations(@NotNull TransitionAnimation... animations) {
		for(TransitionAnimation transition: animations)
			exitAnimations.put(transition.name, transition);
		
		return this;
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
		HashMap<String, TransitionAnimation> animations = isEntering ? entranceAnimations : exitAnimations;
		for(TransitionAnimation animation: animations.values()) {
			if(animation.isTriggerType(other)) {
				DataMap dataMap = tile.getDataMap();
				dataMap.put(DataTag.TransitionName, animation.name);
				dataMap.put(DataTag.TransitionStart, tile.getWorld().getGameTime());
				dataMap.put(DataTag.TransitionMode, isEntering ? TransitionMode.ENTERING : TransitionMode.EXITING);
				if(!isEntering && addNext)
					dataMap.put(DataTag.TransitionTile, other.getEnumType());
				else
					dataMap.remove(DataTag.TransitionTile);
				return true;
			}
		}
		
		return false;
	}
	
	Animation<TextureHolder> getTransitionSprite(@NotNull Tile tile) {
		DataMap dataMap = tile.getDataMap(tileType);
		
		TransitionMode mode = dataMap.get(DataTag.TransitionMode);
		String name = dataMap.get(DataTag.TransitionName);
		
		TransitionAnimation animation;
		if(mode == TransitionMode.ENTERING)
			animation = entranceAnimations.get(name);
		else if(mode == TransitionMode.EXITING)
			animation = exitAnimations.get(name);
		else
			throw new IllegalStateException("Cannot get transition sprite when not transitioning.");
		
		return animation.getAnimation(tileAnimations.get(tileType).get(name));
	}
	
	private boolean isTransitionMode(Tile tile, TransitionMode mode) { return tile.getDataMap(tileType).getOrDefault(DataTag.TransitionMode, TransitionMode.NONE) == mode; }
	
	public boolean playingAnimation(Tile tile) { return !isTransitionMode(tile, TransitionMode.NONE); }
	public boolean playingEntranceAnimation(Tile tile) { return isTransitionMode(tile, TransitionMode.ENTERING); }
	public boolean playingExitAnimation(Tile tile) { return isTransitionMode(tile, TransitionMode.EXITING); }
	
	public void tryFinishAnimation(Tile tile, @NotNull TransitionAnimation anim) {
		if(!playingAnimation(tile)) return;
		
		DataMap dataMap = tile.getDataMap(tileType);
		
		float now = tile.getWorld().getGameTime();
		float prev = dataMap.get(DataTag.TransitionStart);
		float timeElapsed = now - prev;
		
		if(timeElapsed > anim.getDuration()) {
			dataMap.remove(DataTag.TransitionMode);
			dataMap.remove(DataTag.TransitionStart);
			dataMap.remove(DataTag.TransitionName);
			
			// if entering, no action required. if removing, remove the current tile from the stack, specifying not to check for an exit animation. If removing, and there is data for a tile type, then add that tile type.
			TransitionMode mode = dataMap.get(DataTag.TransitionMode);
			
			if(mode == TransitionMode.EXITING) {
				tile.breakTile(false);
				TileTypeEnum nextType = dataMap.getOrDefault(DataTag.TransitionTile, null);
				if(nextType != null)
					tile.addTile(nextType.tileType);
			}
			else {
				// TODO tell tile to update sprites
			}
			
			dataMap.remove(DataTag.TransitionTile);
		}
	}
}
