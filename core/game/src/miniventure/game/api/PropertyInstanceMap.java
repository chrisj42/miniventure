package miniventure.game.api;

import java.util.HashMap;

public class PropertyInstanceMap<P extends Property<P>> {
	
	private final PropertyMap<P, P> map;
	
	public PropertyInstanceMap() {
		map = new PropertyMap<>();
	}
	
	public PropertyInstanceMap(PropertyInstanceMap<P> model) {
		map = new PropertyMap<>(model.map);
	}
	
	public PropertyInstanceMap(PropertyFetcher<P> fetcher) {
		this();
		for(P prop: fetcher.getProperties())
			put(prop);
	}
	
	public <T extends P> void put(T instance) {
		//noinspection unchecked
		map.put((Class<? extends P>)instance.getClass(), instance);
		map.put(instance.getUniquePropertyClass(), instance);
	}
	
	// can be null; should specify the unique property class
	public <T extends P> T get(Class<T> clazz) {
		//noinspection unchecked
		return (T) map.get(clazz);
	}
}
