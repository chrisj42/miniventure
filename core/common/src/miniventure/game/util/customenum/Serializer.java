package miniventure.game.util.customenum;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.SerialEnum.ClassParser;
import miniventure.game.util.function.MapFunction;

public class Serializer<T> {
	
	public final boolean save;
	public final boolean send;
	private final MapFunction<T, String> valueWriter;
	private final MapFunction<String, T> valueParser;
	
	public Serializer(boolean save, boolean send, MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		this.save = save;
		this.send = send;
		this.valueWriter = valueWriter;
		this.valueParser = valueParser;
	}
	
	public Serializer(boolean save, boolean send, final Class<T> valueClass) {
		this.save = save;
		this.send = send;
		this.valueWriter = defaultValueWriter(valueClass);
		this.valueParser = defaultValueParser(valueClass);
	}
	
	// these two constructors below are essentially the same as the two above, except that they convert the value type to a substitute type which gets serialized instead.
	
	public <U> Serializer(boolean save, boolean send, MapFunction<T, U> substituter, MapFunction<U, T> unsubstituter, MapFunction<U, String> substituteWriter, MapFunction<String, U> substituteParser) {
		this.save = save;
		this.send = send;
		this.valueWriter = val -> substituteWriter.get(substituter.get(val));
		this.valueParser = string -> unsubstituter.get(substituteParser.get(string));
	}
	
	public <U> Serializer(boolean save, boolean send, final Class<U> substituteClass, MapFunction<T, U> substituter, MapFunction<U, T> unsubstituter) {
		this(save, send, substituter, unsubstituter, defaultValueWriter(substituteClass), defaultValueParser(substituteClass));
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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	private static <C, A> A parseArray(Class<C> componentType, MapFunction<String, C> componentParser, String data) {
		String[] dataArray = MyUtils.parseLayeredString(data);
		A ar = (A) Array.newInstance(componentType, dataArray.length);
		for(int i = 0; i < dataArray.length; i++)
			Array.set(ar, i, componentParser.get(dataArray[i]));
		
		return ar;
	}
	
	public String serialize(T value) { return valueWriter.get(value); }
	public T deserialize(String data) { return valueParser.get(data); }
	
	
}
