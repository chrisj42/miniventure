package miniventure.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class MyUtils {
	
	private MyUtils() {} // can't instantiate
	
	// In terms of types, what this method guarantees is that, given a target super class, and a starting class that extends the super, it will return a class that is a super of T, and extends S, ideally one level below. But if the target equals the superClass from the beginning (which can't be prevented), that is the only case where it won't return a direct subclass of superClass.
	/** @noinspection unchecked*/
	@NotNull
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
	
	public static void writeOutlinedText(BitmapFont font, SpriteBatch batch, String text, float x, float y) {
		writeOutlinedText(font, batch, text, x, y, Color.WHITE, Color.BLACK);
	}
	
	public static void writeOutlinedText(BitmapFont font, SpriteBatch batch, String text, float x, float y, Color center, Color outline) {
		font.setColor(outline);
		font.draw(batch, text, x-1, y-1);
		font.draw(batch, text, x-1, y+1);
		font.draw(batch, text, x+1, y-1);
		font.draw(batch, text, x+1, y+1);
		font.setColor(center);
		font.draw(batch, text, x, y);
	}
	
	public static String toTitleCase(String string) {
		String[] words = string.split(" ");
		for(int i = 0; i < words.length; i++) {
			if(words[i].length() == 0) continue;
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}
		
		return String.join(" ", words);
	}
	
	// this method moves a rectangle *just* enough so that it fits inside another rectangle. In the event that the "outer" rect is smaller than the rect being moved, the rect being moved will be centered onto the outer rect. The padding is only used if the moving rect isn't already inside the outer one.
	public static Rectangle moveRectInside(Rectangle toMove, Rectangle outer, float padding) {
		if(toMove.width >= outer.width)
			toMove.x = outer.x - (toMove.width - outer.width) / 2;
		else {
			if(toMove.x < outer.x)
				toMove.x = outer.x + padding;
			if(toMove.x + toMove.width > outer.x + outer.width)
				toMove.x = outer.x + outer.width - toMove.width - padding;
		}
		
		if(toMove.height >= outer.height)
			toMove.y = outer.y - (toMove.height - outer.height) / 2;
		else {
			if(toMove.y < outer.y)
				toMove.y = outer.y + padding;
			if(toMove.y + toMove.height > outer.y + outer.height)
				toMove.y = outer.y + outer.height - toMove.height - padding;
		}
		
		return toMove;
	}
}
