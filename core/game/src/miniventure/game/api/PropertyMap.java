package miniventure.game.api;

import java.util.HashMap;

public class PropertyMap<P extends Property<P>, V> extends HashMap<Class<? extends P>, V> {
	
	public PropertyMap() {
		super();
	}
	
	public PropertyMap(PropertyMap<P, V> map) {
		super(map);
	}
	
	public void put(P keyInstance, V value) {
		//noinspection unchecked
		Class<? extends P> actual = (Class<? extends P>) keyInstance.getClass();
		Class<? extends P> unique = keyInstance.getUniquePropertyClass();
		put(actual, value);
		put(unique, value);
	}
	
}
