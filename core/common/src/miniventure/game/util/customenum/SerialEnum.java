package miniventure.game.util.customenum;

import miniventure.game.util.Version;

import org.jetbrains.annotations.Nullable;

public abstract class SerialEnum<T, ET extends SerialEnum<T, ET>> extends GenericEnum<Serializer<T>, ET> {
	
	/* Some notes:
		
		- serial enum classes are not required to have every constant be serializable
		
		- This class is used for both Tile-specific data, and TileType Property data.
	 */
	
	@FunctionalInterface
	interface ClassParser<T> {
		T get(String data, Class<T> clazz);
	}
	
	@Nullable
	private final Serializer<T> serializer;
	
	protected SerialEnum(@Nullable Serializer<T> serializer) {
		this.serializer = serializer;
	}
	
	public boolean serializable() { return serializer != null; }
	
	private void ensureSerializable() {
		if(!serializable())
			throw new IllegalStateException(getClass().getSimpleName()+' '+this+" is not serializable.");
	}
	
	/** @noinspection ConstantConditions*/
	public boolean savable() { return serializable() && serializer.save; }
	/** @noinspection ConstantConditions*/
	public boolean sendable() { return serializable() && serializer.send; }
	
	@SuppressWarnings("unchecked")
	public String serializeCast(Object value) { return serialize((T) value); }
	public String serialize(T value) {
		ensureSerializable();
		//noinspection ConstantConditions
		return serializer.serialize(value);
	}
	
	// assume serial
	public T deserialize(String data) { return deserialize(data, null); }
	public T deserialize(String data, @Nullable Version dataVersionIfFile) {
		ensureSerializable();
		//noinspection ConstantConditions
		return serializer.deserialize(data, dataVersionIfFile);
	}
	
	// the generics on this one are a little finnicky, but its package-private and it works here so oh well
	String serializeEntry(SerialEnumMap<SerialEnum<T, ET>> map) {
		ensureSerializable();
		//noinspection ConstantConditions
		return name()+'='+serializer.serialize(map.get(this));
	}
	
	static <T, ET extends SerialEnum<T, ET>> void deserializeEntry(String data, @Nullable Version dataVersionIfFile, Class<ET> tagClass, SerialEnumMap<ET> map) {
		String[] parts = data.split("=", 2);
		ET tag = GenericEnum.valueOf(tagClass, parts[0]);
		((SerialEnum<T, ET>)tag).ensureSerializable();
		//noinspection ConstantConditions
		map.put(tag, ((SerialEnum<T, ET>)tag).serializer.deserialize(parts[1], dataVersionIfFile));
	}
	
	public SerialEntry<T, ET> asSerial(T value) {
		return new SerialEntry<>(getValue(), value);
	}
	@SuppressWarnings("unchecked")
	public SerialEntry<T, ET> castSerial(Object value) {
		return asSerial((T) value);
	}
}
