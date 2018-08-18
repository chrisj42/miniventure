package miniventure.game.ui3;

import com.badlogic.gdx.math.Vector2;

public class DefaultLayout implements Layout {
	
	// does not move any components. calculates size of container to exactly fit all children at their current positions.
	
	@Override
	public Vector2 calcPrefSize(Component[] children, Vector2 rt) {
		if(children.length == 0) return rt.setZero();
		rt.set(children[0].getSize());
		if(children.length == 1) return rt;
		
		Vector2 min = children[0].getRelativePosition();
		Vector2 max = rt.add(min);
		for(int i = 1; i < children.length; i++) {
			Vector2 cmin = children[i].getRelativePosition();
			Vector2 cmax = children[i].getSize().add(cmin);
			min.set(Math.min(min.x, cmin.x), Math.min(min.y, cmin.y));
			max.set(Math.max(max.x, cmax.x), Math.max(max.y, cmax.y));
		}
		
		return rt.set(max.sub(min));
	}
	
	@Override
	public void applyLayout(Vector2 size, Component[] children) {
		// this layout does not move or resize components.
	}
}
