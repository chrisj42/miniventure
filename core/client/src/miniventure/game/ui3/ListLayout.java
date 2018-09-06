package miniventure.game.ui3;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.BiVoidFunction;
import miniventure.game.util.function.MonoValueFunction;

import com.badlogic.gdx.math.Vector2;

public class ListLayout implements Layout {
	
	private final boolean vertical;
	
	public ListLayout(boolean vertical) {
		this.vertical = vertical;
	}
	
	@Override
	public Vector2 calcPrefSize(Component[] children, Vector2 rt) {
		rt.setZero();
		for(Component c: children) {
			Vector2 size = c.getPrefSize();
			rt.x = Math.max(rt.x, size.x);
			rt.y += size.y;
		}
		
		return rt;
	}
	
	@Override
	public void applyLayout(Vector2 size, Component[] children) {
		// give components as much space as they want unless total is more than given available size. in that case, resize components to fit, retaining the percent taken up out of all the components, for each component.
		// float[] listAxis = new float[children.length];
		// float[] offAxis = new float[children.length];
		
		Vector2[] sizes = ArrayUtils.mapArray(children, Vector2.class, Component::getPrefSize);
		float offTotal = 0, listTotal = 0;
		
		for(Vector2 csize: sizes) {
			listTotal += vertical ? csize.y : csize.x;
			offTotal = Math.max(offTotal, vertical ? csize.x : csize.y);
		}
		
		float xt = vertical ? offTotal : listTotal;
		float yt = vertical ? listTotal : offTotal;
		
		// only use if total > size, aka ratios are > 1.
		float xRatio = xt / size.x;
		float yRatio = yt / size.y;
		
		if(size.x > xt || size.y > yt) {
			for(Vector2 v: sizes) {
				if(xRatio > 1) v.x *= xRatio;
				if(yRatio > 1) v.y *= yRatio;
			}
		}
		
		for(int i = 0; i < children.length; i++)
			children[i].setSize(sizes[i]);
		
		
		// loop to set positions of elements (use relpos)
		
	}
	
	private static void resizeAxis(float[] values, float total, float max) {
		if(max >= total) return;
		// values must be fit to the max.
		for(int i = 0; i < values.length; i++)
			values[i] = MyUtils.mapFloat(values[i], 0, total, 0, max);
	}
}
