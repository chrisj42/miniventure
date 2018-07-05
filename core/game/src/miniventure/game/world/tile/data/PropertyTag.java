package miniventure.game.world.tile.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.tile.SwimAnimation;

public class PropertyTag<T> extends DataTag<T> {
	
	private static int counter = 0;
	private static synchronized int nextOrdinal() { return counter++; }
	
	private static final PropertyTag<?>[] values = new PropertyTag[8];
	public static PropertyTag<?>[] values() { return Arrays.copyOf(values, values.length); }
	private static final String[] names = new String[values.length];
	
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final PropertyTag<Float> LightRadius = new PropertyTag<>(Float.class);
	public static final PropertyTag<Float> SpeedRatio = new PropertyTag<>(Float.class);
	public static final PropertyTag<Float> ZOffset = new PropertyTag<>(Float.class);
	public static final PropertyTag<SwimAnimation> Swim = new PropertyTag<>(SwimAnimation::serialize, SwimAnimation::deserialize);
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	private static final HashMap<String, PropertyTag<?>> nameToValue = new HashMap<>(values.length);
	static {
		ArrayList<Field> enumTypes = new ArrayList<>(values.length);
		
		// WARNING: getDeclaredFields makes no guarantee that the fields are returned in order of declaration.
		// So, I'll store the values in a TreeMap.
		for(Field field: PropertyTag.class.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers()) && PropertyTag.class.isAssignableFrom(field.getType()))
				enumTypes.add(field);
		}
		
		try {
			for(Field type: enumTypes)
				nameToValue.put(type.getName(), (PropertyTag<?>) type.get(null));
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		
		nameToValue.forEach((name, value) -> names[value.ordinal] = name);
	}
	
	public static PropertyTag<?> valueOf(String str) { return nameToValue.get(str); }
	
	private final int ordinal;
	public String name() { return names[ordinal]; }
	public int ordinal() { return ordinal; }
	
	
	PropertyTag(ValueMonoFunction<T, String> valueWriter, ValueMonoFunction<String, T> valueParser) {
		super(valueWriter, valueParser);
		ordinal = nextOrdinal();
		values[ordinal] = this;
	}
	
	PropertyTag(Class<T> valueClass) {
		super(valueClass);
		ordinal = nextOrdinal();
		values[ordinal] = this;
	}
}
