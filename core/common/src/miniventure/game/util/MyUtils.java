package miniventure.game.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import miniventure.game.GameCore;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public final class MyUtils {
	
	private MyUtils() {} // can't instantiate
	
	
	/// STRING UTILS
	
	
	public static String toTitleFormat(String string) {
		return toTitleCase(string.replaceAll("_", " "));
	}
	
	public static String toTitleCase(String string) {
		return toTitleCase(string, " ");
	}
	public static String toTitleCase(String string, String delimiter) {
		String[] words = string.split(delimiter);
		for(int i = 0; i < words.length; i++) {
			if(words[i].length() == 0) continue;
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}
		
		return String.join(delimiter, words);
	}
	
	public static String encodeStringArray(String... strings) { return encodeStringArray(Arrays.asList(strings)); }
	public static String encodeStringArray(Iterable<String> strings) { return encodeStringArray(strings, '(', ')',','); }
	/** @noinspection SameParameterValue*/
	private static String encodeStringArray(Iterable<String> strings, char begin, char end, char delimiter) {
		StringBuilder str = new StringBuilder();
		Iterator<String> iter = strings.iterator();
		while(iter.hasNext()) {
			String elem = iter.next();
			
			final boolean wrap = elem.indexOf(delimiter) >= 0;
			
			if(wrap)
				str.append(begin);
			str.append(elem);
			if(wrap)
				str.append(end);
			
			if(iter.hasNext())
				str.append(delimiter);
		}
		
		return str.toString();
	}
	
	public static String[] parseLayeredString(String str) { return parseLayeredString(str, '(', ')', ','); }
	/** @noinspection SameParameterValue*/
	private static String[] parseLayeredString(String str, char layerStart, char layerEnd, char delimiter) {
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
			else {
				if(chars[i] == layerStart)
					curLayers++;
				
				if((chars[i] != layerStart && chars[i] != layerEnd) || curLayers != 1)
					curStr.append(chars[i]);
				
				if(chars[i] == layerEnd)
					curLayers--;
			}
		}
		
		result.add(curStr.toString());
		
		return result.toArray(String.class);
	}
	
	public static int charCount(char c, String s) {
		int count = 0;
		for(int i = 0; i < s.length(); i++)
			if(s.charAt(i) == c)
				count++;
		
		return count;
		// return s.length() - s.replaceAll("\\"+c, "").length();
	}
	
	public static String plural(int count) { return count == 1 ? "" : "s"; }
	public static String plural(int count, String word) { return plural(count, word, ""); }
	public static String plural(int count, String word, String suffix) {
		return String.valueOf(count) + ' ' +
			(count != 1 && word.endsWith("y") ? word.substring(0, word.length() - 1) : word) +
			(count == 1 ? "" : word.endsWith("s") ? "es" : word.endsWith("y") ? "ies" : "s") +
			suffix;
	}
	
	public static <T> String nullableToString(T obj) { return nullableToString(obj, Object::toString); }
	public static <T> String nullableToString(T obj, MapFunction<T, String> stringifier) {
		return obj == null ? "null" : stringifier.get(obj);
	}
	
	
	// MATH UTILS
	
	
	public static float mapFloat(float num, float prevMin, float prevMax, float newMin, float newMax) {
		return (num-prevMin)/(prevMax-prevMin) * (newMax-newMin) + newMin;
	}
	
	public static <N extends Comparable<N>> N clamp(N val, N min, N max) {
		if(val.compareTo(min) < 0) return min;
		if(val.compareTo(max) > 0) return max;
		return val;
	}
	public static int clamp(int val, int min, int max) { return clamp((Integer)val, (Integer)min, (Integer)max); }
	public static float clamp(float val, float min, float max) { return clamp((Float)val, (Float)min, (Float)max); }
	
	public static int mod(int num, int mod) { return (num % mod + mod) % mod; }
	public static float mod(float num, float mod) { return (num % mod + mod) % mod; }
	//public static long mod(long num, long mod) { return (num % mod + mod) % mod; }
	//public static double mod(double num, double mod) { return (num % mod + mod) % mod; }
	
	
	// GAME UTILS
	
	
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
	
	public static void drawRect(Rectangle rect, int thickness, Color c, Batch batch) {
		drawRect(rect.x, rect.y, rect.width, rect.height, thickness, c, batch);
	}
	public static void drawRect(float x, float y, float width, float height, int thickness, Color c, float alpha, Batch batch) { drawRect(x, y, width, height, thickness, c.cpy().mul(1, 1, 1, alpha), batch); }
	public static void drawRect(float x, float y, float width, float height, int thickness, Color c, Batch batch) {
		fillRect(x, y, width, Math.min(thickness, height), c, batch);
		fillRect(x, y, Math.min(thickness, width), height, c, batch);
		fillRect(x, y+height-1, width+thickness-1, Math.min(thickness, height), c, batch);
		fillRect(x+width-1, y, Math.min(thickness, width), height+thickness-1, c, batch);
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
	
	
	public static void tryPlayMusic(Music music) {
		// music.play();
		noException(music::play, true);
		// if(noException(music::play, true))
		// 	return;// delay(1500, () -> tryPlayMusic(music));
	}
	
	
	// ERROR UTILS
	
	
	// useful as a method reference
	public static <T> boolean notNull(T obj) { return obj != null; }
	
	public static boolean noException(Action action) { return noException(action, false); }
	public static boolean noException(Action action, boolean printError) {
		try {
			action.act();
		} catch(Throwable t) {
			if(printError)
				t.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static String combineThrowableCauses(Throwable t, String prefix) {
		return combineThrowableCauses(t, prefix, -1);
	}
	public static String combineThrowableCauses(Throwable t, String prefix, int maxDepth) {
		StringBuilder str = new StringBuilder(prefix);
		int depth = 0;
		while(t != null && (maxDepth < 0 || depth < maxDepth)) {
			// System.out.println("throwable "+t);
			str.append(": ").append(t.getMessage());
			t = t.getCause();
			depth++;
		}
		return str.toString();
	}
	
	public static void dumpAllStackTraces() {
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		System.out.println(Thread.activeCount()+" Threads running");
		
		InstanceCounter<ThreadGroup> groups = new InstanceCounter<>();
		for(Thread t: traces.keySet())
			groups.add(t.getThreadGroup());
		
		System.out.println(traces.size()+" Threads total in "+groups.size()+" ThreadGroups:");
		for(Entry<ThreadGroup, Integer> entry: groups.entrySet())
			System.out.println('\t'+plural(entry.getValue(), "Thread", ": "+ entry.getKey()));
		
		traces.forEach((thread, elements) -> {
			System.out.println("for " + thread + ":");
			for(StackTraceElement element: elements)
				System.out.println("\t" + element);
		});
	}
	
	
	// MISC UTILS
	
	
	public static void delay(int milliDelay, Action action) { new DelayedAction(milliDelay, action).start(); }
	
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}
	
	public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> clazz, Collection<E> collection) {
		return collection.size() == 0 ? EnumSet.noneOf(clazz) : EnumSet.copyOf(collection);
	}
	public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> clazz, E[] items) {
		return items.length == 0 ? EnumSet.noneOf(clazz) : EnumSet.copyOf(Arrays.asList(items));
	}
	public static EnumSet<TileTypeEnum> enumSet(Collection<TileTypeEnum> collection) {
		return enumSet(TileTypeEnum.class, collection);
	}
	public static EnumSet<TileTypeEnum> enumSet(TileTypeEnum[] items) {
		return enumSet(TileTypeEnum.class, items);
	}
}
