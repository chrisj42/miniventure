package miniventure.game.world.tile.newtile.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.tile.newtile.TileType.TileTypeEnum;
import miniventure.game.world.tile.newtile.TransitionManager.TransitionMode;

import org.jetbrains.annotations.NotNull;

public final class DataTag<T> implements Comparable<DataTag<?>> {
	
	/* Some notes:
		
		- it is expected that all given value types can be converted to a String with toString, and have a constructor that takes a single String.
		
		- This class is used only for Tile-specific data.
	 */
	
	private static int counter = 0;
	private static synchronized int nextOrdinal() { return counter++; }
	
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final DataTag<Integer> Health = new DataTag<>(Integer.class);
	
	// Rendering
	public static final DataTag<String> TransitionName = new DataTag<>(String.class);
	public static final DataTag<Float> TransitionStart = new DataTag<>(Float.class);
	public static final DataTag<TransitionMode> TransitionMode = new DataTag<>(TransitionMode.class);
	public static final DataTag<TileTypeEnum> TransitionTile = new DataTag<>(TileTypeEnum.class);
	
	// Update
	public static final DataTag<Float> LastUpdate = new DataTag<>(Float.class);
	
	
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	private static final HashMap<String, DataTag<?>> nameToValue = new HashMap<>();
	private static final TreeMap<DataTag<?>, String> valueToName = new TreeMap<>();
	static {
		ArrayList<Field> enumTypes = new ArrayList<>(8);
		
		// WARNING: getDeclaredFields makes no guarantee that the fields are returned in order of declaration.
		// So, I'll store the values in a TreeMap.
		for(Field field: DataTag.class.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers()) && DataTag.class.isAssignableFrom(field.getType()))
				enumTypes.add(field);
		}
		
		try {
			for(Field type: enumTypes)
				nameToValue.put(type.getName(), (DataTag<?>) type.get(null));
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		
		nameToValue.forEach((name, value) -> valueToName.put(value, name));
	}
	
	@SuppressWarnings("unchecked")
	public static DataTag<?>[] values() { return valueToName.keySet().toArray(new DataTag[valueToName.size()]); }
	public static DataTag<?> valueOf(String str) { return nameToValue.get(str); }
	
	public static final DataTag<?>[] values = values();
	
	private final int ordinal;
	public String name() { return valueToName.get(this); }
	public int ordinal() { return ordinal; }
	
	
	
	/* --- INSTANCE DEFINITION --- */
	
	
	private final ValueMonoFunction<T, String> valueWriter;
	private final ValueMonoFunction<String, T> valueParser;
	
	private DataTag(ValueMonoFunction<T, String> valueWriter, ValueMonoFunction<String, T> valueParser) {
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
		ordinal = nextOrdinal();
	}
	
	private DataTag(final Class<T> valueClass) {
		this(String::valueOf, data -> {
			if(Enum.class.isAssignableFrom(valueClass))
				return Enum.valueOf(Enum.class.asSubclass(valueClass), data);
			
			try {
				return valueClass.getConstructor(String.class).newInstance(data);
			} catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new TypeNotPresentException(valueClass.getTypeName(), e);
			}
		});
	}
	
	public DataEntry<T> as(T value) { return new DataEntry<>(this, value); }
	public DataEntry<T> asSerial(String value) { return new DataEntry<>(this, deserialize(value)); }
	
	public String serialize(T value) { return valueWriter.get(value); }
	public T deserialize(String data) { return valueParser.get(data); }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof DataTag)) return false;
		return ordinal == ((DataTag)other).ordinal;
	}
	
	@Override
	public int hashCode() { return ordinal; }
	
	@Override
	public int compareTo(@NotNull DataTag<?> o) {
		return Integer.compare(ordinal, o.ordinal);
	}
}
