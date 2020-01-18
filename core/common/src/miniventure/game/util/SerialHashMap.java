package miniventure.game.util;

import java.util.HashMap;
import java.util.LinkedList;

import miniventure.game.util.function.MapFunction;

public class SerialHashMap extends HashMap<String, String> {
	
	public SerialHashMap() {}
	public SerialHashMap(String encodedData) {
		String[] entries = MyUtils.parseLayeredString(encodedData);
		for(String entry: entries) {
			String[] parts = entry.split("=", 2);
			put(parts[0], parts[1]);
		}
	}
	
	public String serialize() {
		String[] entries = new String[size()];
		int i = 0;
		for(Entry<String, String> entry: entrySet())
			entries[i++] = entry.getKey()+'='+entry.getValue();
		return MyUtils.encodeStringArray(entries);
	}
	
	public String add(String key, Object value) {
		return put(key, value == null ? "null" : value.toString());
	}
	
	public <T> T get(String key, MapFunction<String, T> mapper) {
		return mapper.get(get(key));
	}
}
