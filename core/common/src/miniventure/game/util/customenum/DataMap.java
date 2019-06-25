package miniventure.game.util.customenum;

import java.util.HashMap;

import miniventure.game.util.function.MapFunction;

public class DataMap {
	
	private final HashMap<DataEnum<?>, Object> map = new HashMap<>();
	
	public DataMap() {}
	public DataMap(DataEntry<?> entry) { add(entry); }
	public DataMap(DataEntry<?>[] entries) { addAll(entries); }
	public DataMap(DataMap model) { this(model.getEntries()); }
	
	public DataMap add(DataEntry<?> entry) {
		put(entry);
		return this;
	}
	public DataMap addAll(DataEntry<?>[] entries) {
		for(DataEntry<?> e: entries)
			add(e);
		return this;
	}
	
	public <T> T put(DataEntry<T> entry) { return put(entry.key, entry.value); }
	@SuppressWarnings("unchecked")
	public <T> T put(DataEnum<T> key, T value) {
		return (T) map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T remove(DataEnum<T> tag) {
		return (T) map.remove(tag);
	}
	
	public void clear() { map.clear(); }
	
	public boolean contains(DataEnum<?> tag) { return map.containsKey(tag); }
	
	@SuppressWarnings("unchecked")
	public <T> T get(DataEnum<T> tag) {
		return (T) map.get(tag);
	}
	
	public <T, U extends T> T getOrDefault(DataEnum<T> tag, U defaultValue) {
		T val;
		return (val = get(tag)) == null ? defaultValue : val;
	}
	// fetches the value for the given key. If there is no key, the default value is added for it and returned.
	public <T, U extends T> T getOrDefaultAndPut(DataEnum<T> tag, U defaultValue) {
		T val = get(tag);
		if(val == null) {
			put(tag, defaultValue);
			return defaultValue;
		}
		else
			return val;
	}
	public <T, U> U computeFrom(DataEnum<T> tag, MapFunction<T, U> mapper, U defaultValue) {
		if(!map.containsKey(tag))
			return defaultValue;
		else
			return mapper.get(get(tag));
	}
	
	// asClass should match the class in DataTag, generally.
	// public <T> T get(DataEnum<?, T> tag, Class<? extends T> asClass) {
	// 	return asClass.cast(get(tag));
	// }
	
	public DataEntry<?>[] getEntries() {
		DataEntry<?>[] entries = new DataEntry[map.size()];
		int i = 0;
		for(DataEnum<?> tag: map.keySet())
			entries[i++] = getEntry(tag);
		
		return entries;
	}
	
	private <T, D extends DataEnum<T>> DataEntry<T> getEntry(D tag) { return new DataEntry<>(tag, get(tag)); }
}
