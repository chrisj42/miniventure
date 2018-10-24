package miniventure.game.util;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class ClassInstanceMap<T> {
	
	private final HashMap<Class<? extends T>, T> classMap;
	
	public ClassInstanceMap() { classMap = new HashMap<>(); }
	
	public ClassInstanceMap(ClassInstanceMap<T> map) {
		this.classMap = new HashMap<>(map.classMap);
	}
	
	public <U extends T> U put(Class<U> clazz, U instance) {
		return (U) classMap.put(clazz, instance);
	}
	
	public <U extends T> U get(Class<U> clazz) {
		return (U) classMap.get(clazz);
	}
	
	public <U extends T> U remove(Class<U> clazz) {
		return (U) classMap.remove(clazz);
	}
	
	public void putAll(ClassInstanceMap<T> map) {
		classMap.putAll(map.classMap);
	}
	
	public void removeAll(ClassInstanceMap<T> map) {
		for(Class<? extends T> clazz: map.classMap.keySet())
			remove(clazz);
	}
	
	public boolean containsClass(Class<? extends T> clazz) {
		return classMap.containsKey(clazz);
	}
	
	public boolean containsInstance(T instance) {
		return classMap.containsValue(instance);
	}
	
	public void clear() { classMap.clear(); }
}
