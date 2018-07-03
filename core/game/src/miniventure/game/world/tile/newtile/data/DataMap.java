package miniventure.game.world.tile.newtile.data;

import java.util.HashMap;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueMonoFunction;

public class DataMap {
	
	private final HashMap<DataTag<?>, Object> map = new HashMap<>();
	
	public DataMap(DataEntry<?>... entries) { chainAdd(entries); }
	public DataMap(DataMap model) { this(model.getEntries()); }
	
	public DataMap chainAdd(DataEntry<?>... entries) {
		for(DataEntry<?> e: entries)
			put(e);
		return this;
	}
	
	public <T> T put(DataEntry<T> entry) { return put(entry.key, entry.value); }
	public <T> T put(DataTag<T> key, T value) {
		//noinspection unchecked
		return (T) map.put(key, value);
	}
	
	public <T> T remove(DataTag<T> tag) {
		//noinspection unchecked
		return (T) map.remove(tag);
	}
	
	public void clear() { map.clear(); }
	
	public <T> T get(DataTag<T> tag) {
		//noinspection unchecked
		return (T) map.get(tag);
	}
	public <T, U extends T> T getOrDefault(DataTag<T> tag, U defaultValue) {
		if(!map.containsKey(tag))
			return defaultValue;
		else
			return get(tag);
	}
	public <T, U> U computeFrom(DataTag<T> tag, ValueMonoFunction<T, U> mapper, U defaultValue) {
		if(!map.containsKey(tag))
			return defaultValue;
		else
			return mapper.get(get(tag));
	}
	
	// asClass should match the class in DataTag, generally.
	public <T> T get(DataTag<T> tag, Class<? extends T> asClass) {
		return asClass.cast(get(tag));
	}
	
	public DataEntry<?>[] getEntries() {
		DataEntry<?>[] entries = new DataEntry[map.size()];
		int i = 0;
		for(DataTag<?> tag: map.keySet())
			entries[i++] = getEntry(tag);
		
		return entries;
	}
	
	private <T> DataEntry<T> getEntry(DataTag<T> tag) { return new DataEntry<>(tag, get(tag)); }
	
	
	public String serialize() {
		String[] entries = new String[map.size()];
		
		int i = 0;
		for(DataEntry<?> entry: getEntries())
			entries[i++] = entry.serialize();
		
		return MyUtils.encodeStringArray(entries);
	}
	
	public static DataMap deserialize(String alldata) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		DataEntry<?>[] entries = new DataEntry[data.length];
		for(int i = 0; i < entries.length; i++)
			entries[i] = DataEntry.deserialize(data[i]);
		
		return new DataMap(entries);
	}
}
