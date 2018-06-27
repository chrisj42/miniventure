package miniventure.game.util;

import java.util.LinkedList;
import java.util.Stack;

import miniventure.game.GameCore;
import miniventure.game.util.function.ValueMonoFunction;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class MyUtils {
	
	private MyUtils() {} // can't instantiate
	
	
	public static boolean nullablesAreEqual(@Nullable Object o1, @Nullable Object o2) {
		if(o1 != null) return o1.equals(o2);
		return o2 == null;
	}
	
	public static String toTitleCase(String string) {
		return toTitleCase(toTitleCase(string, ""), "_");
	}
	public static String toTitleCase(String string, String delimiter) {
		String[] words = string.split(delimiter);
		for(int i = 0; i < words.length; i++) {
			if(words[i].length() == 0) continue;
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}
		
		return String.join(delimiter, words);
	}
	
	public static String encodeStringArray(String... strings) { return encodeStringArray(strings, '(', ')',','); }
	public static String encodeStringArray(String[] strings, char elementStart, char elementEnd, char delimiter) {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < strings.length; i++) {
			str.append(elementStart);
			str.append(strings[i]);
			str.append(elementEnd);
			if(i < strings.length-1)
				str.append(delimiter);
		}
		
		return str.toString();
	}
	
	public static String[] parseLayeredString(String str) { return parseLayeredString(str, '(', ')', ','); }
	public static String[] parseLayeredString(String str, char layerStart, char layerEnd, char delimiter) {
		if(str.length() == 0) return new String[0];
		
		Array<String> result = new Array<>();
		char[] chars = str.toCharArray();
		int curLayers = 0;
		StringBuilder curStr = new StringBuilder();
		for(int i = 0; i < chars.length; i++) {
			if(chars[i] == delimiter && curLayers == 0) {
				result.add(curStr.toString());
				curStr = new StringBuilder();
			}
			else if(chars[i] == layerStart) {
				if(curLayers > 0) curStr.append(chars[i]);
				curLayers++;
			}
			else if(chars[i] == layerEnd) {
				if(curLayers != 1) curStr.append(chars[i]);
				if(curLayers > 0)
					curLayers--;
			}
			else curStr.append(chars[i]);
		}
		
		result.add(curStr.toString());
		
		return result.toArray(String.class);
	}
	
	
	// this method moves a rectangle *just* enough so that it fits inside another rectangle. In the event that the "outer" rect is smaller than the rect being moved, the rect being moved will be centered onto the outer rect. The padding is only used if the moving rect isn't already inside the outer one.
	public static Rectangle moveRectInside(Rectangle toMove, Rectangle outer, float padding) {
		if(toMove.width+padding*2 >= outer.width)
			toMove.x = outer.x - (toMove.width - outer.width) / 2;
		else {
			if(toMove.x < outer.x)
				toMove.x = outer.x + padding;
			if(toMove.x + toMove.width > outer.x + outer.width)
				toMove.x = outer.x + outer.width - toMove.width - padding;
		}
		
		if(toMove.height+padding*2 >= outer.height)
			toMove.y = outer.y - (toMove.height - outer.height) / 2;
		else {
			if(toMove.y < outer.y)
				toMove.y = outer.y + padding;
			if(toMove.y + toMove.height > outer.y + outer.height)
				toMove.y = outer.y + outer.height - toMove.height - padding;
		}
		
		return toMove;
	}
	
	public static float mapFloat(float num, float prevMin, float prevMax, float newMin, float newMax) {
		return (num-prevMin)/(prevMax-prevMin) * (newMax-newMin) + newMin;
	}
	
	public static void fillRect(Rectangle rect, Color c, Batch batch) {
		fillRect(rect.x, rect.y, rect.width, rect.height, c, batch);
	}
	public static void fillRect(float x, float y, float width, float height, Color c, float alpha, Batch batch) { fillRect(x, y, width, height, c.cpy().mul(1, 1, 1, alpha), batch); }
	public static void fillRect(float x, float y, float width, float height, Color c, Batch batch) {
		Color prev = batch.getColor();
		batch.setColor(c);
		batch.draw(GameCore.icons.get("white").texture, x, y, width, height);
		batch.setColor(prev);
	}
	
	public static <T> void reverseStack(Stack<T> stack) {
		LinkedList<T> list = new LinkedList<>(stack);
		stack.clear();
		while(list.size() > 0)
			stack.push(list.remove(list.size()-1));
	}
	
	public static boolean noException(Action action) {
		try {
			action.act();
		} catch(Throwable t) {
			return false;
		}
		
		return true;
	}
	
	public static <T> boolean notNull(T obj) { return obj != null; }
	
	@SuppressWarnings("unchecked")
	public static <PT, RT> RT[] mapArray(PT[] startValues, Class<RT> returnType, ValueMonoFunction<PT, RT> mapper) {
		RT[] endValues = (RT[]) java.lang.reflect.Array.newInstance(returnType, startValues.length);
		
		for(int i = 0; i < startValues.length; i++)
			endValues[i] = mapper.get(startValues[i]);
		
		return endValues;
	}
	
	public static String arrayToString(int[] array, String start, String end, String delimiter) {
		Integer[] ar = new Integer[array.length];
		for(int i = 0; i < ar.length; i++)
			ar[i] = array[i];
		return arrayToString(ar, start, end, delimiter);
	}
	public static String arrayToString(float[] array, String start, String end, String delimiter) {
		Float[] ar = new Float[array.length];
		for(int i = 0; i < ar.length; i++)
			ar[i] = array[i];
		return arrayToString(ar, start, end, delimiter);
	}
	public static String arrayToString(Object[] array, String start, String end, String delimiter) {
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
		
		T[] joined = (T[]) java.lang.reflect.Array.newInstance(clazz, length);
		int offset = 0;
		for(T[] ar: arrays) {
			System.arraycopy(ar, 0, joined, offset, ar.length);
			offset += ar.length;
		}
		
		return joined;
	}
	
	public static void delay(int milliDelay, Action action) { new DelayedAction(milliDelay, action).start(); }
	
	public static int mod(int num, int mod) { return (num % mod + mod) % mod; }
	public static float mod(float num, float mod) { return (num % mod + mod) % mod; }
	//public static long mod(long num, long mod) { return (num % mod + mod) % mod; }
	//public static double mod(double num, double mod) { return (num % mod + mod) % mod; }
	
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}
}
