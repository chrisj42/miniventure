package miniventure.game.ui3;

import com.badlogic.gdx.math.Vector2;

public class VerticalListLayout implements Layout {
	
	
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
		
	}
}
