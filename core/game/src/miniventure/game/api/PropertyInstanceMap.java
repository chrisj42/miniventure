package miniventure.game.api;

import java.util.Collection;
import java.util.Set;

public class PropertyInstanceMap<P extends Property<P>> {
	
	private final PropertyMap<P, P> map;
	
	public PropertyInstanceMap() {
		map = new PropertyMap<>();
	}
	
	public PropertyInstanceMap(PropertyInstanceMap<P> model) {
		map = new PropertyMap<>(model.map);
	}
	
	public PropertyInstanceMap(PropertyFetcher<P> fetcher) {
		this(fetcher.getProperties());
	}
	public PropertyInstanceMap(P[] properties) {
		this();
		for(P prop: properties)
			put(prop);
	}
	
	public <T extends P> void put(T instance) {
		map.put(instance, instance); // this looks weird, I know, but it does actually accomplish something. :P
	}
	
	// can be null; should specify the unique property class
	public <T extends P> T get(Class<T> clazz) {
		//noinspection unchecked
		return (T) map.get(clazz);
	}
	
	public <T extends P> void putFromMap(Class<T> clazz, PropertyInstanceMap<P> map) {
		T prop = map.get(clazz);
		if(prop != null) {
			put(prop);
			this.map.put(clazz, prop);
		}
	}
	
	public Set<Class<? extends P>> getPropertyClasses() { return map.keySet(); }
	public Collection<P> getProperties() { return map.values(); }
}
