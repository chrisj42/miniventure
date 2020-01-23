package miniventure.game.util.customenum;

import java.util.HashMap;

/** @noinspection rawtypes*/
@SuppressWarnings("unchecked")
public class GEnumMap<ET extends GenericEnum> {
	
	final HashMap<ET, Object> map = new HashMap<>();
	
	public GEnumMap() {}
	public GEnumMap(DataEntry<?, ? extends ET>... entries) { addAll(entries); }
	// public GEnumMap(DataEntry<?, ? extends ET>[] entries) { addAll(entries); }
	public GEnumMap(GEnumMap<ET> model) {
		map.putAll(model.map);
	}
	
	public GEnumMap<ET> add(DataEntry<?, ? extends ET> entry) {
		put(entry);
		return this;
	}
	public GEnumMap<ET> addAll(DataEntry<?, ? extends ET>... entries) {
		for(DataEntry<?, ? extends ET> e: entries)
			put(e);
		return this;
	}
	
	public <T> T put(DataEntry<T, ? extends ET> entry) {
		return (T) map.put(entry.key, entry.value);
	}
	public <T, CET extends GenericEnum<T, ? extends ET>> T put(CET key, T value) {
		return (T) map.put((ET) key, value);
	}
	
	public <T, CET extends GenericEnum<T, ? extends ET>> T remove(CET tag) {
		return (T) map.remove(tag);
	}
	
	public void clear() { map.clear(); }
	
	// public boolean contains(ET tag) { return map.containsKey(tag); }
	
	public <T, CET extends GenericEnum<T, ? extends ET>> T get(CET tag) {
		return (T) map.get(tag);
	}
	
	public <T, CET extends GenericEnum<T, ? extends ET>> T getOrDefault(CET tag, T defaultValue) {
		return (T) map.getOrDefault(tag, defaultValue);
	}
	
	// fetches the value for the given key. If there is no key, the default value is added for it and returned.
	public <T, CET extends GenericEnum<T, ? extends ET>> T getOrDefaultAndPut(DataEntry<T, ? extends ET> tagWithDefault) {
		return getOrDefaultAndPut((CET) tagWithDefault.key, tagWithDefault.value);
	}
	public <T, CET extends GenericEnum<T, ? extends ET>> T getOrDefaultAndPut(CET tag, T defaultValue) {
		// return (T) map.computeIfAbsent(tag, t -> defaultValue);
		T val = get(tag);
		if(val == null) {
			put(tag, defaultValue);
			return defaultValue;
		}
		else
			return val;
	}
	/*public <T, U> U computeFrom(ET tag, MapFunction<T, U> mapper, U defaultValue) {
		return (U) map.compute(tag, (t, val) -> val == null ? defaultValue : mapper.get((T) val));
		// if(!map.containsKey(tag))
		// 	return defaultValue;
		// else
		// 	return mapper.get(get(tag));
	}*/
	
	// asClass should match the class in DataTag, generally.
	// public <T> T get(DataEnum<?, T> tag, Class<? extends T> asClass) {
	// 	return asClass.cast(get(tag));
	// }
	
	/*public DataEntry<?, ? extends ET>[] getEntries() {
		DataEntry<?, ? extends ET>[] entries = new DataEntry[map.size()];
		int i = 0;
		for(ET tag: map.keySet())
			entries[i++] = getEntry(tag);
		
		return entries;
	}*/
	
	/*<T, CET extends GenericEnum<T, ? extends ET>> DataEntry<T, ? extends ET> getEntry(Object tag) {
		// return tag.as((T) map.get(tag));
		return *//*(DataEntry<T, ? extends ET>) *//*((CET)tag).as((T) map.get(tag));
	}*/
}
