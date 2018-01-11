package miniventure.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class MyUtils {
	
	private MyUtils() {} // can't instantiate
	
	@Nullable
	public static <T> Class<? extends T> getDirectSubclass(Class<T> superClass, Class<? extends T> target) {
		Array<Class> parents = new Array<>(target.getInterfaces());
		Class targetSuper = target.getSuperclass();
		
		if(parents.contains(superClass, false) || targetSuper.equals(superClass))
			return target;
		
		if(targetSuper == Object.class && parents.size == 0)
			return null;
		
		if(targetSuper != Object.class && superClass.isAssignableFrom(targetSuper)) {
			//noinspection unchecked
			Class<? extends T> sub = MyUtils.getDirectSubclass(superClass, (Class<? extends T>)targetSuper);
			if(sub != null) return sub;
		}
		
		// go through array
		for(Class parent: parents) {
			if(!superClass.isAssignableFrom(parent)) continue;
			
			//noinspection unchecked
			Class<? extends T> sub = MyUtils.getDirectSubclass(superClass, (Class<? extends T>)parent);
			if(sub != null) return sub;
		}
		
		return null;
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
