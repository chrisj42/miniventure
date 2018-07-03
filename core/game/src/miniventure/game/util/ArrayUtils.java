package miniventure.game.util;

import java.lang.reflect.Array;
import java.util.Arrays;

import miniventure.game.util.function.ValueMonoFunction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ArrayUtils {
	private ArrayUtils() {}
	
	public static <T> T[] revCopyArray(T[] array) {
		T[] newArray = Arrays.copyOf(array, array.length);
		return revArray(newArray);
	}
	
	public static <T> T[] revArray(T[] array) {
		if(array.length < 2) return array;
		
		for(int i = 0; i < array.length/2; i++) {
			T temp = array[i];
			array[i] = array[array.length-1-i];
			array[array.length-1-i] = temp;
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public static <PT, RT> RT[] mapArray(PT[] startValues, Class<RT> returnType, ValueMonoFunction<PT, RT> mapper) {
		RT[] endValues = (RT[]) java.lang.reflect.Array.newInstance(returnType, startValues.length);
		
		for(int i = 0; i < startValues.length; i++)
			endValues[i] = mapper.get(startValues[i]);
		
		return endValues;
	}
	
	@NotNull
	public static String arrayToString(@NotNull Object[] array, String start, String end, String delimiter) {
		StringBuilder str = new StringBuilder(start);
		for(int i = 0; i < array.length; i++) {
			str.append(array[i]);
			if(i < array.length-1)
				str.append(delimiter);
		}
		str.append(end);
		return str.toString();
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayJoin(Class<T> clazz, T[]... arrays) {
		int length = 0;
		for(T[] ar: arrays)
			length += ar.length;
		
		T[] joined = (T[]) Array.newInstance(clazz, length);
		int offset = 0;
		for(T[] ar: arrays) {
			System.arraycopy(ar, 0, joined, offset, ar.length);
			offset += ar.length;
		}
		
		return joined;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] boxPrimitiveArray(Class<T> clazz, int length, ValueMonoFunction<Integer, T> valueFetcher) {
		T[] ar = (T[]) Array.newInstance(clazz, length);
		for(int i = 0; i < ar.length; i++)
			ar[i] = valueFetcher.get(i);
		return ar;
	}
	
	@Contract("null -> fail")
	public static Object[] boxArray(Object ar) {
		Object[] objAr;
		if (ar instanceof Object[])
			objAr = (Object[]) ar;
		else if (ar instanceof byte[])
			objAr = boxPrimitiveArray(Byte.class, ((byte[]) ar).length, i -> ((byte[]) ar)[i]);
		else if (ar instanceof short[])
			objAr = boxPrimitiveArray(Short.class, ((short[]) ar).length, i -> ((short[]) ar)[i]);
		else if (ar instanceof int[])
			objAr = boxPrimitiveArray(Integer.class, ((int[]) ar).length, i -> ((int[]) ar)[i]);
		else if (ar instanceof long[])
			objAr = boxPrimitiveArray(Long.class, ((long[]) ar).length, i -> ((long[]) ar)[i]);
		else if (ar instanceof char[])
			objAr = boxPrimitiveArray(Character.class, ((char[]) ar).length, i -> ((char[]) ar)[i]);
		else if (ar instanceof float[])
			objAr = boxPrimitiveArray(Float.class, ((float[]) ar).length, i -> ((float[]) ar)[i]);
		else if (ar instanceof double[])
			objAr = boxPrimitiveArray(Double.class, ((double[]) ar).length, i -> ((double[]) ar)[i]);
		else if (ar instanceof boolean[])
			objAr = boxPrimitiveArray(Boolean.class, ((boolean[]) ar).length, i -> ((boolean[]) ar)[i]);
		else
			throw new IllegalArgumentException("argument is not an array: "+ar);
		
		return objAr;
	}
	
	public static String deepToString(Object obj, ValueMonoFunction<String[], String> stringArrayJoiner, ValueMonoFunction<Object, String> stringifier) {
		if(!obj.getClass().isArray())
			return stringifier.get(obj);
		
		return stringArrayJoiner.get(mapArray(boxArray(obj), String.class, elem -> deepToString(elem, stringArrayJoiner, stringifier)));
	}
}
