package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class TileTypeToAnimationSetMap<T> {
	
	// maps tile types to animation sets separated by name
	
	// ease-of-use class for the map of sprite id to animation frames
	private class IdToAnimationMap extends HashMap<T, Array<TextureHolder>> {}
	
	// the main map
	private final EnumMap<TileType, IdToAnimationMap> setMap;
	
	// constructor
	TileTypeToAnimationSetMap() {
		setMap = new EnumMap<>(TileType.class);
	}
	
	public abstract T fromString(String name);
	
	/*Map<T, Array<TextureHolder>> getMap(@NotNull TileType tileType) {
		return map.get(tileType);
	}*/
	
	public int getAnimationCount(@NotNull TileType tileType) {
		IdToAnimationMap animMap = setMap.get(tileType);
		if(animMap == null)
			return -1;
		return animMap.size();
	}
	
	public boolean hasSprites(@NotNull TileType tileType) {
		return getAnimationCount(tileType) > 0;
	}
	
	void addFrame(@NotNull TileType tileType, String name, TextureHolder frame) {
		T id = fromString(name);
		setMap.computeIfAbsent(tileType, k -> new IdToAnimationMap())
			.computeIfAbsent(id, k -> new Array<>(TextureHolder.class))
			.add(frame);
	}
	
	Array<TextureHolder> getAnimationFrames(@NotNull TileType tileType, T name, String mapNameForErrorLog) {
		IdToAnimationMap tileAnims = setMap.get(tileType);
		if(tileAnims == null)
			throw new SpriteNotFoundException("tile type "+tileType+" does not have any registered sprites in map "+mapNameForErrorLog+". (discovered when attempting to fetch sprite \""+name+"\")");
		
		Array<TextureHolder> anim = tileAnims.get(name);
		if(anim == null)
			throw new SpriteNotFoundException("tile type "+tileType+" does not have sprite with id \""+name+"\" in map "+mapNameForErrorLog+'.');
		
		return anim;
	}
	
	public static class IndexedTileTypeToAnimationMap extends TileTypeToAnimationSetMap<Integer> {
		@Override
		public Integer fromString(String name) {
			return Integer.parseInt(name);
		}
	}
	public static class StringTileTypeToAnimationMap extends TileTypeToAnimationSetMap<String> {
		@Override
		public String fromString(String name) {
			return name;
		}
	}
}
