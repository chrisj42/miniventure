package miniventure.game.world.tilenew;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.util.property.PropertyInstanceFetcher;

public class TilePropertyInstanceFetcher {
	
	private EnumMap<TileType, HashMap<TilePropertyType, TilePropertyInstance>> propertyInstances = new EnumMap<>(TileType.class);
	
	public TilePropertyInstanceFetcher(PropertyInstanceFetcher<TilePropertyInstance> instanceFetcher) {
		for(TileType tileType: TileType.values) {
			HashMap<TilePropertyType, TilePropertyInstance> propertyMap = new HashMap<>();
			propertyInstances.put(tileType, propertyMap);
			for(TilePropertyType<? extends TilePropertyInstance> propertyType: TilePropertyType.values) {
				propertyMap.put(propertyType, instanceFetcher.getPropertyInstance(tileType.getProp(propertyType)));
			}
		}
	}
	
	// given a TilePropertyInstance (and TileType?), returns another instance of the same class that should be used in place of the given instance.
	// in the server, this will return a subclass that is suited for the server. The client will do likewise.
	// for tiles, we will go through each property type for each tile type, and use this to fetch the instances later. After all, this is used to create new instances given the "template" instance, not to return an instance with an ongoing state.
	public <T extends TilePropertyInstance> T getPropertyInstance(TilePropertyType<T> propertyType, TileType tileType) {
		//noinspection unchecked
		return (T) propertyInstances.get(tileType).get(propertyType);
	}
	
	
}
