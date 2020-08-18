package miniventure.game.world.entity;

import java.util.HashMap;

import miniventure.game.util.SerialHashMap;
import miniventure.game.util.function.MapFunction;

public class EntityDataSet extends SerialHashMap {
	
	private String prefix = "";
	
	public EntityDataSet() { super(); }
	public EntityDataSet(String encodedData) { super(encodedData); }
	
	@Override
	public String add(String key, Object value) {
		return super.add(prefix+key, value);
	}
	
	@Override
	public String remove(Object key) {
		return super.remove(prefix+key);
	}
	
	@Override
	public String get(Object key) {
		return super.get(prefix+key);
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
