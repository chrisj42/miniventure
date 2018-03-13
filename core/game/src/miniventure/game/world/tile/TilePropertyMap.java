package miniventure.game.world.tile;

import java.util.HashMap;
import java.util.HashSet;

public class TilePropertyMap {
	
	private HashMap<Class<? extends TileProperty>, TileProperty> map;
	
	private HashSet<TileProperty> instances;
	private boolean set = false;
	
	public TilePropertyMap() { map = new HashMap<>(); }
	public TilePropertyMap(TilePropertyMap model) { map = new HashMap<>(model.map); }
	
	public void putAll(TileProperty... properties) {
		for(TileProperty prop: properties)
			put(prop);
	}
	
	public void put(TileProperty property) {
		// put it in the lowest class, then look up for any classes that are already put in, but as higher levels.
		Class<? extends TileProperty> clazz = property.getClass();
		Class<? extends TileProperty> topClass = property.getUniquePropertyClass();
		
		HashSet<Class<? extends TileProperty>> matchingClasses = new HashSet<>();
		getAllClassesBetween(clazz, topClass, matchingClasses);
		matchingClasses.add(topClass);
		
		for(Class<? extends TileProperty> eachClass: matchingClasses)
			map.put(eachClass, property);
		
		set = false;
	}
	
	public <T extends TileProperty> T get(Class<T> clazz) {
		//noinspection unchecked
		return (T) map.get(clazz);
	}
	
	public TileProperty[] values() {
		if(!set) {
			instances = new HashSet<>(map.values());
			set = true;
		}
		
		return instances.toArray(new TileProperty[instances.size()]);
	}
	
	
	private static void getAllClassesBetween(Class<? extends TileProperty> bottomClass, Class<? extends TileProperty> topClass, HashSet<Class<? extends TileProperty>> foundClasses) {
		
		Class<?> superClass = bottomClass.getSuperclass();
		if(topClass.isAssignableFrom(superClass)) {
			foundClasses.add(bottomClass);
			if(!topClass.equals(superClass))
				//noinspection unchecked
				getAllClassesBetween((Class<? extends TileProperty>)superClass, topClass, foundClasses);
		}
		
		for(Class<?> interfaceClass: bottomClass.getInterfaces()) {
			if(topClass.isAssignableFrom(interfaceClass)) {
				foundClasses.add(bottomClass);
				if(!topClass.equals(interfaceClass))
					//noinspection unchecked
					getAllClassesBetween((Class<? extends TileProperty>)interfaceClass, topClass, foundClasses);
			}
		}
	}
	
}
