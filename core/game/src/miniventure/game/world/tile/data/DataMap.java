package miniventure.game.world.tile.data;

import java.util.HashMap;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MonoValueFunction;

public class DataMap {
	
	private final HashMap<DataTag<?>, Object> map = new HashMap<>();
	
	public DataMap() {}
	public DataMap(DataEntry<?, ?> entry) { add(entry); }
	public DataMap(DataEntry<?, ?>[] entries) { addAll(entries); }
	public DataMap(DataMap model) { this(model.getEntries()); }
	
	public DataMap add(DataEntry<?, ?> entry) {
		put(entry);
		return this;
	}
	public DataMap addAll(DataEntry<?, ?>[] entries) {
		for(DataEntry<?, ?> e: entries)
			add(e);
		return this;
	}
	
	public <T> T put(DataEntry<T, ?> entry) { return put(entry.key, entry.value); }
	@SuppressWarnings("unchecked")
	public <T> T put(DataTag<T> key, T value) {
		return (T) map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T remove(DataTag<T> tag) {
		return (T) map.remove(tag);
	}
	
	public void clear() { map.clear(); }
	
	public boolean contains(DataTag<?> tag) { return map.containsKey(tag); }
	
	@SuppressWarnings("unchecked")
	public <T> T get(DataTag<T> tag) {
		return (T) map.get(tag);
	}
	
	public <T, U extends T> T getOrDefault(DataTag<T> tag, U defaultValue) {
		T val;
		return (val = get(tag)) == null ? defaultValue : val;
	}
	// fetches the value for the given key. If there is no key, the default value is added for it and returned.
	public <T, U extends T> T getOrDefaultAndPut(DataTag<T> tag, U defaultValue) {
		T val = get(tag);
		if(val == null) {
			put(tag, defaultValue);
			return defaultValue;
		}
		else
			return val;
	}
	public <T, U> U computeFrom(DataTag<T> tag, MonoValueFunction<T, U> mapper, U defaultValue) {
		if(!map.containsKey(tag))
			return defaultValue;
		else
			return mapper.get(get(tag));
	}
	
	// asClass should match the class in DataTag, generally.
	public <T> T get(DataTag<T> tag, Class<? extends T> asClass) {
		return asClass.cast(get(tag));
	}
	
	public DataEntry<?, ?>[] getEntries() {
		DataEntry<?, ?>[] entries = new DataEntry[map.size()];
		int i = 0;
		for(DataTag<?> tag: map.keySet())
			entries[i++] = getEntry(tag);
		
		return entries;
	}
	
	private <T, D extends DataTag<T>> DataEntry<T, D> getEntry(D tag) { return new DataEntry<>(tag, get(tag)); }
	
	
	public String serialize() {
		String[] entries = new String[map.size()];
		
		int i = 0;
		for(DataEntry<?, ?> entry: getEntries())
			entries[i++] = entry.serialize();
		
		return MyUtils.encodeStringArray(entries);
	}
	
	public static <D extends DataTag<?>> DataMap deserialize(String alldata, Class<D> tagClass) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		DataEntry<?, ?>[] entries = new DataEntry[data.length];
		for(int i = 0; i < entries.length; i++)
			entries[i] = DataEntry.deserialize(data[i], tagClass);
		
		return new DataMap(entries);
	}
}
