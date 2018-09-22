package miniventure.game.util;

import java.awt.Rectangle;
import java.awt.Point;

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
	public int getY() { return getY(false); }
	public int getY(boolean yDown) { return yDown ? y : -y; }
	
	public static final RelPos[] values = RelPos.values();
	public static RelPos values(int ordinal) { return values[ordinal]; }
	
	public static RelPos get(int x, int y) { return get(x, y, false); }
	public static RelPos get(int x, int y, boolean yDown) {
		y = yDown ? y : -y;
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
	
	public Point forRectangle(Rectangle rect) {
		int xDist = rect.width/2;
		int yDist = rect.height/2;
		Point center = new Point((int)rect.getCenterX(), (int)rect.getCenterY());
		center.x += xDist * getX();
		center.y += yDist * getY(true);
		return center;
	}
}
