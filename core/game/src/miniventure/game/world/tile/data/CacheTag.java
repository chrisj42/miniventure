package miniventure.game.world.tile.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.util.function.MapFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.TransitionManager.TransitionMode;

public class CacheTag<T> extends DataTag<T> {
	
	
	private static int counter = 0;
	private static synchronized int nextOrdinal() { return counter++; }
	
	private static final CacheTag<?>[] values = new CacheTag[8];
	public static CacheTag<?>[] values() { return Arrays.copyOf(values, values.length); }
	private static final String[] names = new String[values.length];
	
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final CacheTag<Integer> Health = new CacheTag<>(Integer.class);
	
	// Rendering
	public static final CacheTag<String> TransitionName = new CacheTag<>(String.class);
	public static final CacheTag<Float> TransitionStart = new CacheTag<>(Float.class);
	public static final CacheTag<TransitionMode> TransitionMode = new CacheTag<>(TransitionMode.class);
	public static final CacheTag<TileTypeEnum> TransitionTile = new CacheTag<>(TileTypeEnum.class);
	
	// Update
	public static final CacheTag<Float> LastUpdate = new CacheTag<>(Float.class);
	public static final CacheTag<float[]> UpdateTimers = new CacheTag<>(float[].class);
	public static final CacheTag<String[]> UpdateActionCaches = new CacheTag<>(String[].class);
	
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	private static final HashMap<String, CacheTag<?>> nameToValue = new HashMap<>(values.length);
	static {
		ArrayList<Field> enumTypes = new ArrayList<>(values.length);
		
		// WARNING: getDeclaredFields makes no guarantee that the fields are returned in order of declaration.
		// So, I'll store the values in a TreeMap.
		for(Field field: CacheTag.class.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers()) && CacheTag.class.isAssignableFrom(field.getType()))
				enumTypes.add(field);
		}
		
		try {
			for(Field type: enumTypes)
				nameToValue.put(type.getName(), (CacheTag<?>) type.get(null));
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		
		nameToValue.forEach((name, value) -> names[value.ordinal()] = name);
	}
	
	public static CacheTag<?> valueOf(String str) { return nameToValue.get(str); }
	
	private final int ordinal;
	public String name() { return names[ordinal]; }
	public int ordinal() { return ordinal; }
	
	
	
	private CacheTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(valueWriter, valueParser);
		ordinal = nextOrdinal();
		values[ordinal] = this;
	}
	
	private CacheTag(Class<T> valueClass) {
		super(valueClass);
		ordinal = nextOrdinal();
		values[ordinal] = this;
	}
	
}
