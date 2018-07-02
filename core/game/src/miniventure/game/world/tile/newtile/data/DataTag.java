package miniventure.game.world.tile.newtile.data;

import java.lang.reflect.InvocationTargetException;

import miniventure.game.util.function.ValueMonoFunction;

public final class DataTag<T> {
	
	/* Some notes:
		
		- it is expected that all given value types can be converted to a String with toString, and have a constructor that takes a single String.
		
		- This class is used for both TileLayer ID data, and Tile-specific data. They will be separate DataMaps, but they will be DataMaps nonetheless.
	 */
	
	private static int counter = 0;
	
	// Tile-specific
	
	public static final DataTag<Integer> Health = new DataTag<>(Integer.class);
	
	// Rendering
	public static final DataTag<String> AnimationName = new DataTag<>(String.class);
	public static final DataTag<Float> AnimationStart = new DataTag<>(Float.class);
	
	// Update
	public static final DataTag<Float> LastUpdate = new DataTag<>(Float.class);
	
	
	
	
	private final int ordinal;
	private final ValueMonoFunction<T, String> valueWriter;
	private final ValueMonoFunction<String, T> valueParser;
	
	private DataTag(ValueMonoFunction<T, String> valueWriter, ValueMonoFunction<String, T> valueParser) {
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
		ordinal = counter++;
	}
	
	private DataTag(Class<T> valueClass) {
		this(String::valueOf, data -> {
			try {
				return valueClass.getConstructor(String.class).newInstance(data);
			} catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new TypeNotPresentException(valueClass.getTypeName(), e);
			}
		});
	}
	
	public DataEntry<T> as(T value) { return new DataEntry<>(this, value); }
	
	public String serialize(T value) { return valueWriter.get(value); }
	public T deserialize(String data) { return valueParser.get(data); }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof DataTag)) return false;
		return ordinal == ((DataTag)other).ordinal;
	}
	
	@Override
	public int hashCode() { return ordinal; }
}
