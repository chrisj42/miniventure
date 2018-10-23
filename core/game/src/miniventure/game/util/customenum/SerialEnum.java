package miniventure.game.util.customenum;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.FetchBiFunction;
import miniventure.game.util.function.MapFunction;

@SuppressWarnings("unchecked")
public abstract class SerialEnum<T> extends GenericEnum<SerialEnum<T>> {
	
	/* Some notes:
		
		- it is expected that all given value types can be converted to a String with toString, and have a constructor that takes a single String.
		
		- This class is used for both Tile-specific data, and TileType Property data.
		
		- None of these values will ever be saved to file. TileType properties will be regenerated, and tile data-caches don't hold important data.
	 */
	
	
	private final MapFunction<T, String> valueWriter;
	private final MapFunction<String, T> valueParser;
	
	protected SerialEnum(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
	}
	
	protected SerialEnum(final Class<T> valueClass) {
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
	interface ClassParser<T> extends FetchBiFunction<String, Class<T>, T> {}
	
	private static <T> MapFunction<T, String> getValueWriter(Class<T> valueClass, MapFunction<Object, String> baseParser) {
		if(valueClass.isArray())
			return ar -> ArrayUtils.deepToString(ar, MyUtils::encodeStringArray, baseParser);
		return (MapFunction<T, String>) baseParser;
	}
	
	private static <T, B> MapFunction<String, T> getValueParser(Class<T> valueClass, ClassParser<B> baseValueParser) {
		if(valueClass.isArray())
			return (MapFunction<String, T>) getArrayParser(valueClass.getComponentType(), baseValueParser);
		// T == B
		return data -> ((ClassParser<T>)baseValueParser).get(data, valueClass);
	}
	
	private static <C, B> MapFunction<String, B> getArrayParser(Class<C> componentType, ClassParser<B> baseValueParser) {
		MapFunction<String, C> componentParser = getValueParser(componentType, baseValueParser);
		return data -> parseArray(componentType, componentParser, data);
	}
	
	private static <C, A> A parseArray(Class<C> componentType, MapFunction<String, C> componentParser, String data) {
		String[] dataArray = MyUtils.parseLayeredString(data);
		A ar = (A) Array.newInstance(componentType, dataArray.length);
		for(int i = 0; i < dataArray.length; i++)
			Array.set(ar, i, componentParser.get(dataArray[i]));
		
		return ar;
	}
	
	public SerialEntry<T, ?> as(T value) { return new SerialEntry<>(this, value); }
	SerialEntry<T, ?> serialEntry(String value) { return new SerialEntry<>(this, deserialize(value)); }
	
	public String serialize(T value) { return valueWriter.get(value); }
	public T deserialize(String data) { return valueParser.get(data); }
	
	/** @noinspection NoopMethodInAbstractClass*/
	public static void init() {}
}
