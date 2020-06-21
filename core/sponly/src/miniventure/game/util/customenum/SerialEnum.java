package miniventure.game.util.customenum;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class SerialEnum<T, ET extends SerialEnum<T, ET>> extends GenericEnum<T, ET> {
	
	/* Some notes:
		- this is for generic enums who represent at least some types that can be serialized
			- if all enum types of a subclass should be serializable, then the subclass can simply not include a constructor without serialization methods.
		- it is designed so that the types can be serialized to or from a map of the data
	 */
	
	@FunctionalInterface
	interface ClassParser<T> {
		T get(String data, Class<T> clazz);
	}
	
	private class SerialInterface {
		@NotNull final MapFunction<T, String> valueWriter;
		@NotNull final MapFunction<String, T> valueParser;
		
		SerialInterface(@NotNull MapFunction<T, String> valueWriter, @NotNull MapFunction<String, T> valueParser) {
			this.valueWriter = valueWriter;
			this.valueParser = valueParser;
		}
	}
	
	// public final boolean save; // save to map?
	// public final boolean send;
	
	// @Nullable
	// private final SerialInterface serialInterface;
	
	private final MapFunction<T, String> valueWriter;
	private final MapFunction<String, T> valueParser;
	
	// no serialization
	protected SerialEnum() { this(null, null); }
	
	protected SerialEnum(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
	}
	
	protected SerialEnum(final Class<T> valueClass) {
		this(defaultValueWriter(valueClass), defaultValueParser(valueClass));
	}
	
	// these two constructors below are essentially the same as the two above, except that they convert the value type to a substitute type which gets serialized instead.
	
	protected <U> SerialEnum(MapFunction<T, U> substituter, MapFunction<U, T> unsubstituter, MapFunction<U, String> substituteWriter, MapFunction<String, U> substituteParser) {
		this(
				val -> substituteWriter.get(substituter.get(val)),
				string -> unsubstituter.get(substituteParser.get(string))
		);
	}
	
	protected <U> SerialEnum(final Class<U> substituteClass, MapFunction<T, U> substituter, MapFunction<U, T> unsubstituter) {
		this(substituter, unsubstituter, defaultValueWriter(substituteClass), defaultValueParser(substituteClass));
	}
	
	private static <T> MapFunction<T, String> defaultValueWriter(final Class<T> valueClass) {
		if(valueClass.isArray())
			return ar -> ArrayUtils.deepToString(ar, MyUtils::encodeStringArray, String::valueOf);
		return String::valueOf;
	}
	
	private static <T> MapFunction<String, T> defaultValueParser(final Class<T> valueClass) {
		return getValueParser(valueClass, (data, baseClass) -> {
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
	
	public String serialize(T value) {
		// return valueWriter == null ? null : valueWriter.get(value);
		if(valueWriter == null)
			throw new SerializableException(this, true);
		return valueWriter.get(value);
	}
	public T deserialize(String data) {
		// return valueParser == null ? null : valueParser.get(data);
		if(valueParser == null)
			throw new SerializableException(this, false);
		return valueParser.get(data);
	}
	
	String serializeEntry(SerialEnumMap<? super ET> map) {
		return name()+'='+serialize(map.get(this));
	}
	
	static <T, ET extends SerialEnum<T, ET>> void deserializeEntry(String data, Class<ET> tagClass, SerialEnumMap<ET> map) {
		String[] parts = data.split("=", 2);
		ET tag = GenericEnum.valueOf(tagClass, parts[0]);
		map.add(tag, tag.deserialize(parts[1]));
	}
	
	private static class SerializableException extends RuntimeException {
		public SerializableException(SerialEnum<?, ?> enumValue, boolean missingSerial) {
			super(enumValue.getEnumClass().getSimpleName()+' '+enumValue+" has no value"+(missingSerial?"writer":"parser")+" and cannot be "+(missingSerial?"":"de")+"serialized.");
		}
	}
}
