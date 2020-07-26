package miniventure.game.util.customenum;

import java.util.ArrayList;
import java.util.HashMap;

import miniventure.game.util.MyUtils;

/** @noinspection rawtypes*/
@SuppressWarnings("unchecked")
public class SerialEnumMap<ET extends SerialEnum> {
	
	final HashMap<ET, Object> map = new HashMap<>();
	
	public SerialEnumMap() {}
	public SerialEnumMap(DataEntry<?, ? extends ET>... entries) { addAll(entries); }
	public SerialEnumMap(SerialEnumMap<ET> model) {
		map.putAll(model.map);
	}
	
	public SerialEnumMap(String alldata, Class<ET> tagClass) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		for(String item: data)
			SerialEnum.deserializeEntry(item, tagClass, this);
	}
	
	public String serialize(boolean save) {
		ArrayList<String> entries = new ArrayList<>(map.size());
		
		for(SerialEnum key: map.keySet()) {
			if(key.serialSave() && save || key.serialSend() && !save)
				entries.add(key.serializeEntry(this));
		}
		
		return MyUtils.encodeStringArray(entries);
	}
	
	public SerialEnumMap<ET> add(DataEntry<?, ? extends ET> entry) {
		put(entry);
		return this;
	}
	public SerialEnumMap<ET> addAll(DataEntry<?, ? extends ET>... entries) {
		for(DataEntry<?, ? extends ET> e: entries)
			put(e);
		return this;
	}
	
	public <T> T put(DataEntry<T, ? extends ET> entry) {
		return (T) map.put(entry.key, entry.value);
	}
	public <T, CET extends SerialEnum<T, ? extends ET>> T put(CET key, T value) {
		return (T) map.put((ET) key, value);
	}
	
	public <T, CET extends SerialEnum<T, ? extends ET>> T remove(CET tag) {
		return (T) map.remove((ET) tag);
	}
	
	public void clear() { map.clear(); }
	
	// public boolean contains(ET tag) { return map.containsKey(tag); }
	
	public <T, CET extends SerialEnum<T, ? extends ET>> T get(CET tag) {
		return (T) map.get((ET) tag);
	}
	
	public <T, CET extends SerialEnum<T, ? extends ET>> T getOrDefault(CET tag, T defaultValue) {
		return (T) map.getOrDefault((ET) tag, defaultValue);
	}
	
	// fetches the value for the given key. If there is no key, the default value is added for it and returned.
	public <T, CET extends SerialEnum<T, ? extends ET>> T getOrDefaultAndPut(DataEntry<T, ? extends ET> tagWithDefault) {
		return getOrDefaultAndPut((CET) tagWithDefault.key, tagWithDefault.value);
	}
	public <T, CET extends SerialEnum<T, ? extends ET>> T getOrDefaultAndPut(CET tag, T defaultValue) {
		// return (T) map.computeIfAbsent(tag, t -> defaultValue);
		T val = get(tag);
		if(val == null) {
			put(tag, defaultValue);
			return defaultValue;
		}
		else
			return val;
	}
}
