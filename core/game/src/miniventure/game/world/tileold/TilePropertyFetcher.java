package miniventure.game.world.tileold;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.util.function.MapFunction;

public class TilePropertyFetcher {
	
	private EnumMap<TileType, HashMap<TilePropertyType, TileProperty>> propertyInstances = new EnumMap<>(TileType.class);
	
	public TilePropertyFetcher(MapFunction<TileProperty> instanceFetcher) {
		for(TileType tileType: TileType.values) {
			HashMap<TilePropertyType, TileProperty> propertyMap = new HashMap<>();
			propertyInstances.put(tileType, propertyMap);
			for(TilePropertyType<? extends TileProperty> propertyType: TilePropertyType.values) {
				propertyMap.put(propertyType, tileType.getProp(propertyType, instanceFetcher));
			}
		}
	}
	
	TileProperty[] getProperties(TileType type) {
		HashMap<TilePropertyType, TileProperty> instances = propertyInstances.get(type);
		return instances.values().toArray(new TileProperty[instances.size()]);
	}
	
	// given a TilePropertyInstance (and TileType?), returns another instance of the same class that should be used in place of the given instance.
	// in the server, this will return a subclass that is suited for the server. The client will do likewise.
	// for tiles, we will go through each property type for each tile type, and use this to fetch the instances later. After all, this is used to create new instances given the "template" instance, not to return an instance with an ongoing state.
	public <T extends TileProperty> T getProp(TilePropertyType<T> propertyType, TileType tileType) {
		//noinspection unchecked
		return (T) propertyInstances.get(tileType).get(propertyType);
	}
	
	public <T extends TileProperty> T getProp(TilePropertyType<? super T> propertyType, TileType tileType, Class<T> asClass) {
		//noinspection unchecked
		return (T) propertyInstances.get(tileType).get(propertyType);
	}
	
}
