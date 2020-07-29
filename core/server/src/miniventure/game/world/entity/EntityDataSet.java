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
	public <T> T get(String key, MapFunction<String, T> mapper) {
		return super.get(prefix+key, mapper);
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
