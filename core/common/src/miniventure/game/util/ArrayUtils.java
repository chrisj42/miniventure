package miniventure.game.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import miniventure.game.util.function.MapFunction;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** @noinspection WeakerAccess*/
@SuppressWarnings("unchecked")
public final class ArrayUtils {
	private ArrayUtils() {}
	
	public static <T> T pickRandom(T[] array) { return pickRandom(array, MathUtils.random); }
	public static <T> T pickRandom(T[] array, Random random) {
		return array[random.nextInt(array.length)];
	}
	public static <T> T pickRandom(Object array) { return pickRandom(array, MathUtils.random); }
	public static <T> T pickRandom(Object array, Random random) {
		return (T) Array.get(array, random.nextInt(Array.getLength(array)));
	}
	
	public static void revArray(Object array) {
		final int len = Array.getLength(array);
		if(len < 2) return;
		
		for(int i = 0; i < len/2; i++) {
			Object temp = Array.get(array, i);
			Array.set(array, i, Array.get(array, len-1-i));
			Array.set(array, len-1-i, temp);
		}
	}
	
	public static Object flattenArray(Object multiDimArray) {
		Class<?> compType = multiDimArray.getClass();
		while(compType.isArray())
			compType = compType.getComponentType();
		
		// compile into 1D list
		List<Object> list = new LinkedList<>();
		flattenHelper(list, multiDimArray);
		// make new array and copy contents to it
		Object flatArray = Array.newInstance(compType, list.size());
		int i = 0;
		for(Object o: list)
			Array.set(flatArray, i++, o);
		
		return flatArray;
	}
	
	private static void flattenHelper(List<Object> stack, Object array) {
		if(!array.getClass().isArray())
			stack.add(array);
		else for(int i = 0; i < Array.getLength(array); i++)
			flattenHelper(stack, Array.get(array, i));
	}
	
	/** @noinspection SuspiciousSystemArraycopy*/
	public static <T> T joinArrays(Class<T> arrayClass, Object... arrays) {
		int length = 0;
		for(Object ar: arrays)
			length += Array.getLength(ar);
		
		T joined = (T) Array.newInstance(arrayClass.getComponentType(), length);
		int offset = 0;
		for(Object ar: arrays) {
			System.arraycopy(ar, 0, joined, offset, Array.getLength(ar));
			offset += Array.getLength(ar);
		}
		
		return joined;
	}
	
	// this method is not really safe since it assumes the the array type is the generic type; ie it assumes there has been no casting.
	/*public static <T> T[] mapArray(T[] startArray, MapFunction<T, T> mapper) {
		return mapArray(startArray, (Class<T>) startArray.getClass().getComponentType(), mapper);
	}*/
	// useful to convert between object arrays
	public static <PT, RT> RT[] mapArray(PT[] startArray, Class<RT> resultComponentType, MapFunction<PT, RT> mapper) {
		return (RT[]) mapArray((Object)startArray, resultComponentType, mapper);
	}
	// useful to convert from an object array to a primitive array
	public static <PT, RT, RAT> RAT mapArray(PT[] startArray, Class<RT> resultComponentType, Class<RAT> resultArrayType, MapFunction<PT, RT> mapper) {
		return (RAT) mapArray((Object)startArray, resultComponentType, mapper);
	}
	// useful to convert from a primitive array to an object array
	public static <PT, RT> RT[] mapArray(Class<PT> startComponentType, Object startArray, Class<RT> resultComponentType, MapFunction<PT, RT> mapper) {
		return (RT[]) mapArray(startArray, resultComponentType, mapper);
	}
	// useful to convert between primitive arrays.
	public static <PT, RT, RAT> RAT mapArray(Class<PT> startComponentType, Object startArray, Class<RT> resultComponentType, Class<RAT> resultArrayType, MapFunction<PT, RT> mapper) {
		return (RAT) mapArray(startArray, resultComponentType, mapper);
	}
	// base method that does the real mapping work
	private static <PT, RT> Object mapArray(Object startArray, Class<RT> resultComponentType, MapFunction<PT, RT> mapper) {
		Object endValues = Array.newInstance(resultComponentType, Array.getLength(startArray));
		
		for(int i = 0; i < Array.getLength(startArray); i++)
			Array.set(endValues, i, mapper.get((PT)Array.get(startArray, i)));
		
		return endValues;
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
	
	public static void deepFill(Object multiDimArray, MapFunction<List<Integer>, ?> valueFetcher) {
		int dimensions = 0;
		Class<?> clazz = multiDimArray.getClass();
		while(clazz.isArray()) {
			++dimensions;
			clazz = clazz.getComponentType();
		}
		
		if(dimensions > 0) // 0 dims means the given object is not an array at all
			deepFillHelper(multiDimArray, valueFetcher, new ArrayList<>(dimensions));
	}
	
	private static void deepFillHelper(Object array, MapFunction<List<Integer>, ?> valueFetcher, List<Integer> indexStack) {
		final Class<?> componentType = array.getClass().getComponentType();
		final boolean multidim = componentType.isArray();
		for(int i = 0; i < Array.getLength(array); i++) {
			indexStack.add(i);
			if(multidim) {
				// contains arrays
				Object obj = Array.get(array, i);
				if(obj != null)
					deepFillHelper(obj, valueFetcher, indexStack);
			}
			else // contains values
				Array.set(array, i, valueFetcher.get(indexStack));
			
			indexStack.remove(indexStack.size()-1);
		}
	}
	
	@NotNull
	public static String arrayToString(@NotNull Object array, String start, String end, String delimiter) {
		return arrayToString(array, Object::toString, start, end, delimiter);
	}
	@NotNull
	public static String arrayToString(@NotNull Object array, @NotNull MapFunction<Object, String> stringifier, String start, String end, String delimiter) {
		StringBuilder str = new StringBuilder(start);
		
		final int len = Array.getLength(array);
		for(int i = 0; i < len; i++) {
			str.append(stringifier.get(Array.get(array, i)));
			if(i < len-1)
				str.append(delimiter);
		}
		str.append(end);
		return str.toString();
	}
	
	public static String deepToString(Object arrayValue, MapFunction<String[], String> stringArrayJoiner, MapFunction<Object, String> stringifier) {
		if(arrayValue == null)
			return "null";
		
		if(!arrayValue.getClass().isArray())
			return stringifier.get(arrayValue);
		
		return stringArrayJoiner.get(fetchArray(String.class, Array.getLength(arrayValue), i -> deepToString(Array.get(arrayValue, i), stringArrayJoiner, stringifier)));
	}
	
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
		if(ar == null) return null;
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
	
	public static void deepMapFloatArray(Object array, final float newmin, final float newmax) {
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
