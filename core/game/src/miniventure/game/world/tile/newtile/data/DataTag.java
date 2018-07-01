package miniventure.game.world.tile.newtile.data;

import java.lang.reflect.InvocationTargetException;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.tile.newtile.TileType;

public final class DataTag<T> {
	
	/* Some notes:
		
		- it is expected that all given value types can be converted to a String with toString, and have a constructor that takes a single String.
		
		- This class is used for both TileLayer ID data, and Tile-specific data. They will be separate DataMaps, but they will but be DataMaps nonetheless.
	 */
	
	// Booleans
	public static final DataTag<Boolean> Permeable = new DataTag<>(Boolean.class);
	public static final DataTag<Boolean> Overlap = new DataTag<>(Boolean.class);
	public static final DataTag<Boolean> Opaque = new DataTag<>(Boolean.class);
	
	public static final DataTag<Float> LightRadius = new DataTag<>(Float.class);
	
	public static final DataTag<TileType[]> Connections = new DataTag<>(
		types -> MyUtils.encodeStringArray(MyUtils.<TileType, String>mapArray(types, String.class, Enum::name)), 
		data -> MyUtils.mapArray(MyUtils.parseLayeredString(data), TileType.class, TileType::valueOf)
	);
	
	
	// Tile-specific
	
	public static final DataTag<Integer> Health = new DataTag<>(Integer.class);
	
	// Rendering
	public static final DataTag<String> AnimationName = new DataTag<>(String.class);
	public static final DataTag<Float> AnimationStart = new DataTag<>(Float.class);
	
	// Update
	public static final DataTag<Float> LastUpdate = new DataTag<>(Float.class);
	
	
	
	
	
	private final ValueMonoFunction<T, String> valueWriter;
	private final ValueMonoFunction<String, T> valueParser;
	
	private DataTag(ValueMonoFunction<T, String> valueWriter, ValueMonoFunction<String, T> valueParser) {
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
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
	
}
