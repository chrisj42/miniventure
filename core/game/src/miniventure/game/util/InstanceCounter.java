package miniventure.game.util;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

public class InstanceCounter<T> extends HashMap<T, Integer> {
	
	public InstanceCounter() {
		
	}
	
	public Integer add(T instance) {
		Integer newVal = get(instance)+1;
		put(instance, newVal);
		return newVal;
	}
	
	public Integer removeInstance(T instance) {
		Integer newVal = Math.max(0, get(instance)-1);
		if(newVal == 0)
			remove(instance);
		else
			put(instance, newVal);
		return newVal;
	}
	
	@Override @NotNull
	public Integer get(Object instance) {
		Integer val = super.get(instance);
		return val == null ? 0 : val;
	}
	
	@Override @NotNull
	public Integer remove(Object instance) {
		Integer cur = super.remove(instance);
		return cur == null ? 0 : cur;
	}
	
	@Override @NotNull
	public Integer put(T instance, Integer count) {
		Integer prevVal = super.put(instance, count);
		return prevVal == null ? 0 : prevVal;
	}
}
