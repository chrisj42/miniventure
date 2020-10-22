package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class TileTypeToAnimationMap<T> {
	
	// ease-of-use class for the map of sprite id to animation frames
	private class AnimationMap extends HashMap<T, Array<TextureHolder>> {}
	
	// the main map
	private final EnumMap<TileTypeEnum, AnimationMap> map;
	
	// constructor
	TileTypeToAnimationMap() {
		map = new EnumMap<>(TileTypeEnum.class);
	}
	
	public abstract T fromString(String name);
	
	Map<T, Array<TextureHolder>> getMap(@NotNull TileTypeEnum tileType) {
		return map.get(tileType);
	}
	
	public int getAnimationCount(@NotNull TileTypeEnum tileType) {
		AnimationMap animMap = map.get(tileType);
		if(animMap == null)
			return -1;
		return animMap.size();
	}
	
	public boolean hasAnimations(@NotNull TileTypeEnum tileType) {
		return getAnimationCount(tileType) > 0;
	}
	
	void addFrame(@NotNull TileTypeEnum tileType, String name, @NotNull TextureHolder frame) {
		T id = fromString(name);
		map.computeIfAbsent(tileType, k -> new AnimationMap())
			.computeIfAbsent(id, k -> new Array<>(TextureHolder.class))
			.add(frame);
	}
	
	Array<TextureHolder> getAnimationFrames(@NotNull TileTypeEnum tileType, T name, String mapNameForErrorLog) {
		AnimationMap tileAnims = map.get(tileType);
		if(tileAnims == null)
			throw new SpriteNotFoundException("tile type "+tileType+" does not have any registered sprites in map "+mapNameForErrorLog+". (discovered when attempting to fetch sprite \""+name+"\")");
		
		Array<TextureHolder> anim = tileAnims.get(name);
		if(anim == null)
			throw new SpriteNotFoundException("tile type "+tileType+" does not have sprite with id \""+name+"\" in map "+mapNameForErrorLog+'.');
		
		return anim;
	}
	
	// TODO this ought to be a checked exception
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}
	
	public static class IndexedTileTypeToAnimationMap extends TileTypeToAnimationMap<Integer> {
		@Override
		public Integer fromString(String name) {
			return Integer.parseInt(name);
		}
	}
	public static class StringTileTypeToAnimationMap extends TileTypeToAnimationMap<String> {
		@Override
		public String fromString(String name) {
			return name;
		}
	}
}
