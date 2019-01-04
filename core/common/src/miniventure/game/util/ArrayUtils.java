package miniventure.game.util;

import java.lang.reflect.Array;
import java.util.LinkedList;

import miniventure.game.util.function.MapFunction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ArrayUtils {
	private ArrayUtils() {}
	
	public static void revArray(Object array) {
		final int len = Array.getLength(array);
		if(len < 2) return;
		
		for(int i = 0; i < len/2; i++) {
			Object temp = Array.get(array, i);
			Array.set(array, i, Array.get(array, len-1-i));
			Array.set(array, len-1-i, temp);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <PT, RT> RT[] mapArray(PT[] startValues, Class<RT> returnType, MapFunction<PT, RT> mapper) {
		RT[] endValues = (RT[]) java.lang.reflect.Array.newInstance(returnType, startValues.length);
		
		for(int i = 0; i < startValues.length; i++)
			endValues[i] = mapper.get(startValues[i]);
		
		return endValues;
	}
	
	@NotNull
	public static String arrayToString(@NotNull Object array, String start, String end, String delimiter) {
		StringBuilder str = new StringBuilder(start);
		
		final int len = Array.getLength(array);
		for(int i = 0; i < len; i++) {
			str.append(Array.get(array, i));
			if(i < len-1)
				str.append(delimiter);
		}
		str.append(end);
		return str.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayJoin(Class<T> clazz, Object... arrays) {
		int length = 0;
		for(Object ar: arrays)
			length += Array.getLength(ar);
		
		T[] joined = (T[]) Array.newInstance(clazz, length);
		int offset = 0;
		for(Object ar: arrays) {
			System.arraycopy(ar, 0, joined, offset, Array.getLength(ar));
			offset += Array.getLength(ar);
		}
		
		return joined;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] fetchArray(Class<T> clazz, int length, MapFunction<Integer, T> valueFetcher) {
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
			objAr = fetchArray(Byte.class, ((byte[]) ar).length, i -> ((byte[]) ar)[i]);
		else if (ar instanceof short[])
			objAr = fetchArray(Short.class, ((short[]) ar).length, i -> ((short[]) ar)[i]);
		else if (ar instanceof int[])
			objAr = fetchArray(Integer.class, ((int[]) ar).length, i -> ((int[]) ar)[i]);
		else if (ar instanceof long[])
			objAr = fetchArray(Long.class, ((long[]) ar).length, i -> ((long[]) ar)[i]);
		else if (ar instanceof char[])
			objAr = fetchArray(Character.class, ((char[]) ar).length, i -> ((char[]) ar)[i]);
		else if (ar instanceof float[])
			objAr = fetchArray(Float.class, ((float[]) ar).length, i -> ((float[]) ar)[i]);
		else if (ar instanceof double[])
			objAr = fetchArray(Double.class, ((double[]) ar).length, i -> ((double[]) ar)[i]);
		else if (ar instanceof boolean[])
			objAr = fetchArray(Boolean.class, ((boolean[]) ar).length, i -> ((boolean[]) ar)[i]);
		else
			throw new IllegalArgumentException("argument is not an array: "+ar);
		
		return objAr;
	}
	
	public static Object unbox(Object[] ar) {
		try {
			Class<?> clazz = (Class<?>) ar.getClass().getComponentType().getDeclaredField("TYPE").get(null);
			Object primAr = Array.newInstance(clazz, ar.length);
			for(int i = 0; i < ar.length; i++)
				Array.set(primAr, i, ar[i]);
			return primAr;
		} catch(IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalArgumentException("array component type "+ar.getClass().getComponentType()+" is not a primitive wrapper class.", e);
		}
	}
	
	public static String deepToString(Object obj, MapFunction<String[], String> stringArrayJoiner, MapFunction<Object, String> stringifier) {
		if(obj == null)
			return "null";
		
		if(!obj.getClass().isArray())
			return stringifier.get(obj);
		
		return stringArrayJoiner.get(mapArray(boxArray(obj), String.class, elem -> deepToString(elem, stringArrayJoiner, stringifier)));
	}
	
	public static void deepFill(Object array, Object value) {
		for(int i = 0; i < Array.getLength(array); i++) {
			Object stored = Array.get(array, i);
			if(stored != null && stored.getClass().isArray())
				deepFill(stored, value);
			else
				Array.set(array, i, value);
		}
	}
	
	// i realize that I don't need to track every array if I'm assuming the component type is a number; but I already did it.
	public static void deepMapArray(Object array, float newmin, float newmax) {
		if(Array.getLength(array) == 0) return;
		
		float[] extrema = getDeepMinMax(array);
		final float min = extrema[0], max = extrema[1];
		if(min == newmin && max == newmax) return; // goal already achieved
		
		LinkedList<Object> subarrays = new LinkedList<>();
		subarrays.add(array);
		
		while(subarrays.size() > 0) {
			Object curArray = subarrays.removeFirst();
			final int len = Array.getLength(curArray);
			for(int i = 0; i < len; i++) {
				Object val = Array.get(curArray, i);
				if(val == null) continue;
				if(val.getClass().isArray())
					subarrays.add(val);
				else
					Array.set(curArray, i, MyUtils.mapFloat((float)val, min, max, newmin, newmax));
			}
		}
	}
	
	public static float[] getDeepMinMax(Object array) {
		if(array == null) return null;
		if(!array.getClass().isArray())
			return new float[] {(float)array, (float)array};
		
		boolean set = false;
		float min = 0, max = 0;
		for(int i = 0; i < Array.getLength(array); i++) {
			float[] extrema = getDeepMinMax(Array.get(array, i));
			if(extrema == null) continue;
			if(!set) {
				set = true;
				min = extrema[0];
				max = extrema[1];
			}
			else {
				min = Math.min(min, extrema[0]);
				max = Math.max(max, extrema[1]);
			}
		}
		
		if(!set) return null;
		return new float[] {min, max};
	}
}
