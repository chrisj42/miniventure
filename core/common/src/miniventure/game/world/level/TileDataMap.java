package miniventure.game.world.level;

import java.util.HashMap;

import miniventure.game.world.tile.Tile;

public class TileDataMap<T> {
	
	private final HashMap<Tile, T> map = new HashMap<>();
	
	TileDataMap() {}
	
	public T get(Tile tile) {
		return map.get(tile);
	}
	
	public void put(Tile tile, T value) {
		map.put(tile, value);
	}
	
	public void clear(Tile tile) {
		map.remove(tile);
	}
	
	public T getOrDefault(Tile tile, T defaultValue) {
		return map.getOrDefault(tile, defaultValue);
	}
	
	public T getOrDefaultAndPut(Tile tile, T defaultValue) {
		T value = get(tile);
		if(value == null) {
			put(tile, defaultValue);
			return defaultValue;
		}
		return value;
	}
}
