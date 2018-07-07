package miniventure.game.world.tile.data;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueBiFunction;
import miniventure.game.util.function.ValueMonoFunction;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class DataTag<T> implements Comparable<DataTag<?>> {
	
	/* Some notes:
		
		- it is expected that all given value types can be converted to a String with toString, and have a constructor that takes a single String.
		
		- This class is used for both Tile-specific data, and TileType Property data.
		
		- None of these values will ever be saved to file. TileType properties will be regenerated, and tile data-caches don't hold important data.
	 */
	
	
	public abstract String name();
	public abstract int ordinal();
	
	
	/* --- INSTANCE DEFINITION --- */
	
	
	private final ValueMonoFunction<T, String> valueWriter;
	private final ValueMonoFunction<String, T> valueParser;
	
	DataTag(ValueMonoFunction<T, String> valueWriter, ValueMonoFunction<String, T> valueParser) {
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
	}
	
	DataTag(final Class<T> valueClass) {
		this.valueWriter = getValueWriter(valueClass, String::valueOf);
		this.valueParser = getValueParser(valueClass, (data, baseClass) -> {
			if(Enum.class.isAssignableFrom(baseClass))
				return Enum.valueOf(baseClass.asSubclass(Enum.class), data);
			
			if(baseClass.isPrimitive()) {
				// primitive type...
				if(baseClass.equals(char.class)) {
					// this one is a little different
					return data.charAt(0);
				}
				
				Object obj = Array.newInstance(baseClass, 0);
				Object[] ar = ArrayUtils.boxArray(obj);
				Class<?> boxedClass = ar.getClass().getComponentType();
				try {
					return boxedClass.getMethod("parse"+MyUtils.toTitleCase(baseClass.toString()), String.class).invoke(null, data);
				} catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException("primitive type "+baseClass+" could not be instantiated; boxed "+boxedClass+" does not contain a parse"+boxedClass.getSimpleName()+'('+String.class.getName()+") method.", e);
				}
			}
			
			try {
				return baseClass.getConstructor(String.class).newInstance(data);
			} catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new TypeNotPresentException(baseClass.getTypeName(), e);
			}
		});
	}
	
	@FunctionalInterface
	interface ClassParser<T> extends ValueBiFunction<String, Class<T>, T> {}
	
	private static <T> ValueMonoFunction<T, String> getValueWriter(Class<T> valueClass, ValueMonoFunction<Object, String> baseParser) {
		if(valueClass.isArray())
			return ar -> ArrayUtils.deepToString(ar, MyUtils::encodeStringArray, baseParser);
		return (ValueMonoFunction<T, String>) baseParser;
	}
	
	private static <T, B> ValueMonoFunction<String, T> getValueParser(Class<T> valueClass, ClassParser<B> baseValueParser) {
		if(valueClass.isArray())
			return (ValueMonoFunction<String, T>) getArrayParser(valueClass.getComponentType(), baseValueParser);
		// T == B
		return data -> ((ClassParser<T>)baseValueParser).get(data, valueClass);
	}
	
	private static <C, B> ValueMonoFunction<String, B> getArrayParser(Class<C> componentType, ClassParser<B> baseValueParser) {
		ValueMonoFunction<String, C> componentParser = getValueParser(componentType, baseValueParser);
		return data -> parseArray(componentType, componentParser, data);
	}
	
	private static <C, A> A parseArray(Class<C> componentType, ValueMonoFunction<String, C> componentParser, String data) {
		String[] dataArray = MyUtils.parseLayeredString(data);
		A ar = (A) Array.newInstance(componentType, dataArray.length);
		for(int i = 0; i < dataArray.length; i++)
			Array.set(ar, i, componentParser.get(dataArray[i]));
		
		return ar;
	}
	
	public DataEntry<T, ?> as(T value) { return new DataEntry<>(this, value); }
	DataEntry<T, ?> serialEntry(String value) { return new DataEntry<>(this, deserialize(value)); }
	
	public String serialize(T value) { return valueWriter.get(value); }
	public T deserialize(String data) { return valueParser.get(data); }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof DataTag)) return false;
		return ordinal() == ((DataTag)other).ordinal();
	}
	
	@Override
	public final int hashCode() { return ordinal(); }
	
	@Override
	public final int compareTo(@NotNull DataTag<?> o) {
		return Integer.compare(ordinal(), o.ordinal());
	}
	
	@Override
	public final String toString() { return name()==null?String.valueOf(ordinal()):name(); }
	
	public static void init() {}
}
