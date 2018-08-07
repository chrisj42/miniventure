package miniventure.game.util;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public enum RelPos {
	TOP_LEFT, TOP, TOP_RIGHT,
	LEFT, CENTER, RIGHT,
	BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
	
	private final int x, y;
	
	RelPos() {
		x = (ordinal() % 3) - 1;
		y = (ordinal() / 3) - 1;
	}
	
	public int getX() { return x; }
	public int getY() { return -y; }
	
	public static final RelPos[] values = RelPos.values();
	public static RelPos values(int ordinal) { return values[ordinal]; }
	
	public static RelPos get(int x, int y) {
		y = -y;
		if(x < -1) x = -1;
		if(x > 1) x = 1;
		if(y < -1) y = -1;
		if(y > 1) y = 1;
		
		x++;
		y++;
		return values[x + y*3];
	}
	
	public RelPos getOpposite() {
		int nx = -x + 1;
		int ny = -y + 1;
		return get(nx, ny);
	}
	
	public RelPos rotate() { return rotate(true); }
	public RelPos rotate(boolean clockwise) {
		// to do clockwise, flip x/y, then negate x
		// to do counter-clockwise, negate x, then flip x/y
		
		// get x/y
		int x = this.x;
		int y = this.y;
		
		// do rotation
		if(!clockwise)
			x = -x;
		int temp = x;
		//noinspection SuspiciousNameCombination
		x = y;
		y = temp;
		if(clockwise)
			x = -x;
		
		// return new pos
		return get(x, y);
	}
	
	
	public RelPos getNext(boolean clockwise) {
		if(this == CENTER) return CENTER;
		
		// y goes opp of x
		
		// if on sides, y goes in dir of x (or opp if ccw)
		// if on top/bottom, x goes opp y
		
		// if in corner:
		// if x == y, move x by the neg of value
		// else, inc y by x
		
		int nx = x, ny = y;
		
		if(x == 0)
			nx += -y;
		else if(y == 0)
			ny += x;
		else if(x == y)
			nx += -y;
		else
			ny += x;
		
		
		// fetch pos
		RelPos pos = get(nx, ny);
		if(clockwise)
			return pos;
		else
			return pos.rotate(false);
	}
	
	
	/** positions the given rect around the given anchor. The double size is what aligns it to a point rather than a rect. */
	public Vector2 positionRect(Vector2 rectSize, Vector2 anchor) {
		Rectangle bounds = new Rectangle(anchor.x-rectSize.x, anchor.y-rectSize.y, rectSize.x*2, rectSize.y*2);
		return positionRect(rectSize, bounds);
	}
	// the point is returned as a rectangle with the given dimension and the found location, within the provided dummy rectangle.
	public Rectangle positionRect(Vector2 rectSize, Vector2 anchor, Rectangle dummy) {
		Vector2 pos = positionRect(rectSize, anchor);
		dummy.setSize(rectSize.x, rectSize.y);
		dummy.setPosition(pos.x, pos.y);
		return dummy;
	}
	
	/** positions the given rect to a relative position in the container. */
	public Vector2 positionRect(Vector2 rectSize, Rectangle container) { return positionRect(rectSize.x, rectSize.y, container); }
	public Vector2 positionRect(float rectWidth, float rectHeight, Rectangle container) {
		Vector2 blcorner = container.getCenter(new Vector2());
		
		// this moves the inner box correctly
		blcorner.x += (x * container.getWidth() / 2) - ((x+1) * rectWidth / 2);
		blcorner.y += (y * container.getHeight() / 2) - ((y+1) * rectHeight / 2);
		
		return blcorner;
	}
	
	// the point is returned as a rectangle with the given dimension and the found location, within the provided dummy rectangle.
	public Rectangle positionRect(Vector2 rectSize, Rectangle container, Rectangle dummy) {
		Vector2 pos = positionRect(rectSize, container);
		dummy.setSize(rectSize.x, rectSize.y);
		dummy.setPosition(pos.x, pos.y);
		return dummy;
	}
}
