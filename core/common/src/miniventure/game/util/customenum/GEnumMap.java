package miniventure.game.util.customenum;

import java.util.Arrays;

/** @noinspection rawtypes*/
@SuppressWarnings("unchecked")
public class GEnumMap<ET extends GenericEnum> {
	
	// final HashMap<ET, Object> map = new HashMap<>();
	final Class<ET> dataClass;
	final Object[] data;
	
	public GEnumMap(Class<ET> dataClass) {
		data = new Object[GenericEnum.values(dataClass).length];
		this.dataClass = dataClass;
	}
	public GEnumMap(Class<ET> dataClass, DataEntry<?, ? extends ET>... entries) {
		this(dataClass);
		addAll(entries);
	}
	public GEnumMap(DataEntry<?, ? extends ET> firstEntry, DataEntry<?, ? extends ET>... entries) {
		this(firstEntry.key.getEnumClass());
		add(firstEntry).addAll(entries);
	}
	// public GEnumMap(DataEntry<?, ? extends ET>[] entries) { addAll(entries); }
	/*public GEnumMap(GEnumMap<ET> model) {
		this(model.dataClass);
		// map.putAll(model.map);
	}*/
	
	public GEnumMap<ET> add(DataEntry<?, ? extends ET> entry) {
		add(entry.key, entry.value);
		return this;
	}
	public GEnumMap<ET> addAll(DataEntry<?, ? extends ET>... entries) {
		for(DataEntry<?, ? extends ET> e: entries)
			add(e.key, e.value);
		return this;
	}
	
	/*public <T> T set(DataEntry<T, ? extends ET> entry) {
		return set(entry.key, entry.value);
	}*/
	public <T, CET extends GenericEnum<T, ? extends ET>> void add(CET key, T value) {
		data[key.ordinal()] = value;
	}
	
	public <T, CET extends GenericEnum<T, ? extends ET>> T remove(CET tag) {
		T prev = get(tag);
		add(tag, null);
		return prev;
	}
	
	public void clear() {
		Arrays.fill(data, null);
	}
	
	// public boolean contains(ET tag) { return map.containsKey(tag); }
	
	public <T, CET extends GenericEnum<T, ? extends ET>> T get(CET tag) {
		return (T) data[tag.ordinal()];
	}
	
	public <T, CET extends GenericEnum<T, ? extends ET>> T getOrDefault(CET tag, T defaultValue) {
		T value = get(tag);
		return value == null ? defaultValue : value;
	}
	
	// fetches the value for the given key. If there is no key, the default value is added for it and returned.
	public <T, CET extends GenericEnum<T, ? extends ET>> T getOrDefaultAndPut(DataEntry<T, ? extends ET> tagWithDefault) {
		return getOrDefaultAndPut((CET) tagWithDefault.key, tagWithDefault.value);
	}
	public <T, CET extends GenericEnum<T, ? extends ET>> T getOrDefaultAndPut(CET tag, T defaultValue) {
		// return (T) map.computeIfAbsent(tag, t -> defaultValue);
		T val = get(tag);
		if(val == null) {
			add(tag, defaultValue);
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
