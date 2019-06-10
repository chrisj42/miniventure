package miniventure.game.util;

import miniventure.game.util.function.MapFunction;
import miniventure.game.util.function.ValueAction;

public class SyncObj<T> {
	
	private final T obj;
	
	public SyncObj(T obj) {
		this.obj = obj;
	}
	
	public <RT> RT get(MapFunction<T, RT> function) {
		synchronized (obj) {
			return function.get(obj);
		}
	}
	
	public void act(ValueAction<T> action) {
		synchronized (obj) {
			action.act(obj);
		}
	}
	
}
