package miniventure.game.util.customenum;

import java.util.HashMap;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;

public class SerialMap {
	
	private final HashMap<SerialEnum<?>, Object> map = new HashMap<>();
	
	public SerialMap() {}
	public SerialMap(SerialEntry<?, ?> entry) { add(entry); }
	public SerialMap(SerialEntry<?, ?>[] entries) { addAll(entries); }
	public SerialMap(SerialMap model) { this(model.getEntries()); }
	
	public SerialMap add(SerialEntry<?, ?> entry) {
		put(entry);
		return this;
	}
	public SerialMap addAll(SerialEntry<?, ?>[] entries) {
		for(SerialEntry<?, ?> e: entries)
			add(e);
		return this;
	}
	
	public <T> T put(SerialEntry<T, ?> entry) { return put(entry.key, entry.value); }
	@SuppressWarnings("unchecked")
	public <T> T put(SerialEnum<T> key, T value) {
		return (T) map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T remove(SerialEnum<T> tag) {
		return (T) map.remove(tag);
	}
	
	public void clear() { map.clear(); }
	
	public boolean contains(SerialEnum<?> tag) { return map.containsKey(tag); }
	
	@SuppressWarnings("unchecked")
	public <T> T get(SerialEnum<T> tag) {
		return (T) map.get(tag);
	}
	
	public <T, U extends T> T getOrDefault(SerialEnum<T> tag, U defaultValue) {
		T val;
		return (val = get(tag)) == null ? defaultValue : val;
	}
	// fetches the value for the given key. If there is no key, the default value is added for it and returned.
	public <T, U extends T> T getOrDefaultAndPut(SerialEnum<T> tag, U defaultValue) {
		T val = get(tag);
		if(val == null) {
			put(tag, defaultValue);
			return defaultValue;
		}
		else
			return val;
	}
	public <T, U> U computeFrom(SerialEnum<T> tag, MapFunction<T, U> mapper, U defaultValue) {
		if(!map.containsKey(tag))
			return defaultValue;
		else
			return mapper.get(get(tag));
	}
	
	// asClass should match the class in DataTag, generally.
	public <T> T get(SerialEnum<T> tag, Class<? extends T> asClass) {
		return asClass.cast(get(tag));
	}
	
	public SerialEntry<?, ?>[] getEntries() {
		SerialEntry<?, ?>[] entries = new SerialEntry[map.size()];
		int i = 0;
		for(SerialEnum<?> tag: map.keySet())
			entries[i++] = getEntry(tag);
		
		return entries;
	}
	
	private <T, D extends SerialEnum<T>> SerialEntry<T, D> getEntry(D tag) { return new SerialEntry<>(tag, get(tag)); }
	
	
	public String serialize() {
		String[] entries = new String[map.size()];
		
		int i = 0;
		for(SerialEntry<?, ?> entry: getEntries())
			entries[i++] = entry.serialize();
		
		return MyUtils.encodeStringArray(entries);
	}
	
	public static <D extends SerialEnum<?>> SerialMap deserialize(String alldata, Class<D> tagClass) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		SerialEntry<?, ?>[] entries = new SerialEntry[data.length];
		for(int i = 0; i < entries.length; i++)
			entries[i] = SerialEntry.deserialize(data[i], tagClass);
		
		return new SerialMap(entries);
	}
}
