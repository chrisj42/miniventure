package miniventure.game.util.customenum;

public abstract class SerialEnum<T, ET extends SerialEnum<T, ET>> extends GenericEnum<Serializer<T>, SerialEnum<T, ET>> {
	
	/* Some notes:
		
		- it is expected that all given value types can be converted to a String with toString, and have a constructor that takes a single String.
		
		- This class is used for both Tile-specific data, and TileType Property data.
		
		- None of these values will ever be saved to file. TileType properties will be regenerated, and tile data-caches don't hold important data.
	 */
	
	@FunctionalInterface
	interface ClassParser<T> {
		T get(String data, Class<T> clazz);
	}
	
	private final Serializer<T> serializer;
	
	protected SerialEnum(Serializer<T> serializer) {
		this.serializer = serializer;
	}
	
	boolean serialSave() { return serializer.save; }
	boolean serialSend() { return serializer.send; }
	
	// the generics on this one are a little finnicky, but its package-private and it works here so oh well
	String serializeEntry(SerialEnumMap<SerialEnum<T, ET>> map) {
		return name()+'='+serializer.serialize(map.get(this));
	}
	
	static <T, ET extends SerialEnum<T, ET>> void deserializeEntry(String data, Class<ET> tagClass, SerialEnumMap<ET> map) {
		String[] parts = data.split("=", 2);
		ET tag = GenericEnum.valueOf(tagClass, parts[0]);
		map.put(tag, ((SerialEnum<T, ET>)tag).serializer.deserialize(parts[1]));
	}
}
