package miniventure.game.util;

import java.util.LinkedList;
import java.util.Stack;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyUtils {
	
	private MyUtils() {} // can't instantiate
	
	public static boolean nullablesAreEqual(@Nullable Object o1, @Nullable Object o2) {
		if(o1 != null) return o1.equals(o2);
		return o2 == null;
	}
	
	public static String toTitleCase(String string) {
		String[] words = string.split(" ");
		for(int i = 0; i < words.length; i++) {
			if(words[i].length() == 0) continue;
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}
		
		return String.join(" ", words);
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
		//int offset = 0;
		int curLayers = 0;
		StringBuilder curStr = new StringBuilder();
		for(int i = 0; i < chars.length; i++) {
			if(chars[i] == delimiter && curLayers == 0) {
				//result.add(new String(chars, offset, i - offset));
				//offset = i+1;
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
		
		//result.add(str.substring(offset+1, str.length()-1)); // past the layerStart, and before the layerEnd.
		result.add(curStr.toString());
		
		return result.toArray(String.class);
	}
	
	// In terms of types, what this method guarantees is that, given a target super class, and a starting class that extends the super, it will return a class that is a super of T, and extends S, ideally one level below. But if the target equals the superClass from the beginning (which can't be prevented), that is the only case where it won't return a direct subclass of superClass.
	@NotNull
	@SuppressWarnings("unchecked")
	public static <S, T extends S> Class<? extends S> getDirectSubclass(@NotNull Class<S> superClass, @NotNull Class<T> target) {
		if (superClass.equals(target))
			return target; // this returns the super class, not it's direct subclass, but in this situation that's that best we're gonna get.
		
		// because we only ever use super classes from the original target, it is safe at any time to assume that any class found is super T.
		
		Class<? super T> targetSuper = target.getSuperclass(); // due to the equality check above, this class will always extend S.
		
		boolean hasSuper = targetSuper != null; // if target is the Object class, then there won't a super class.
		if (hasSuper && targetSuper.equals(superClass))
			return target;
		
		Array<Class<? super T>> parents = new Array<>((Class<? super T>[]) target.getInterfaces());
		if (parents.contains(superClass, false))
			return target;
		
		// target is not a direct subclass of superClass, so recurse first into the super class, and then into the interfaces.
		
		if (hasSuper && superClass.isAssignableFrom(targetSuper))
			return getDirectSubclass(superClass, (Class<? extends S>) targetSuper);
		else {
			// it HAS to be one of the interfaces that extend the superClass.
			for(Class<? super T> parent: parents)
				if(superClass.isAssignableFrom(parent))
					return getDirectSubclass(superClass, (Class<? extends S>) parent);
			
			// if the code reaches here, then something went terribly wrong, because target is required to extend superClass, but at this point we've gone through both the interfaces and the super class, and haven't found it.
			
			throw new IncompatibleClassChangeError("Class " + target + " is required by generics to extend " + superClass + ", but neither the super class nor any implemented interfaces do.");
		}
	}
	
	/*public static void writeOutlinedText(Batch batch, String text, float x, float y) { writeOutlinedText(batch, text, x, y, Color.WHITE); }
	public static void writeOutlinedText(Batch batch, String text, float x, float y, Color center) { writeOutlinedText(batch, text, x, y, center, Color.BLACK); }
	public static void writeOutlinedText(Batch batch, String text, float x, float y, Color center, Color outline) {
		FreeTypeFontParameter params = GameCore.getDefaultFontConfig();
		params.color = center;
		params.borderColor = outline;
		params.borderWidth = 1;
		
		BitmapFont font = GameCore.getFont(params);
		
		font.draw(batch, text, x, y);
	}*/
	
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
	
	public static float map(float num, float prevMin, float prevMax, float newMin, float newMax) {
		return (num-prevMin)/(prevMax-prevMin) * (newMax-newMin) + newMin;
	}
	
	public static void fillRect(Rectangle rect, Color c, Batch batch) {
		fillRect(rect.x, rect.y, rect.width, rect.height, c.r, c.g, c.b, c.a, batch);
	}
	public static void fillRect(float x, float y, Color c, Batch batch) {
		fillRect(x, y, c.r, c.g, c.b, c.a, batch);
	}
	public static void fillRect(float x, float y, float width, float height, Color c, Batch batch) {
		fillRect(x, y, width, height, c.r, c.g, c.b, c.a, batch);
	}
	public static void fillRect(float x, float y, float width, float height, float r, float g, float b, float a, Batch batch) {
		Color c = batch.getColor();
		batch.setColor(r, g, b, a);
		batch.draw(GameCore.icons.get("white").texture, x, y, width, height);
		batch.setColor(c);
	}
	public static void fillRect(float x, float y, float r, float g, float b, float a, Batch batch) {
		Color c = batch.getColor();
		batch.setColor(r, g, b, a);
		batch.draw(GameCore.icons.get("white").texture, x, y);
		batch.setColor(c);
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
}
