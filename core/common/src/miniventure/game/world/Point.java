package miniventure.game.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Point {
	
	public final int x, y;
	
	private Point() { this(0, 0); }
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public static Point floorVector(Vector2 v) {
		return new Point(MathUtils.floor(v.x), MathUtils.floor(v.y));
	}
	public static Point roundVector(Vector2 v) {
		return new Point(MathUtils.round(v.x), MathUtils.round(v.y));
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Point)) return false;
		Point o = (Point) other;
		return x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return javaPointHashCode(x, y); }
	
	public static int javaPointHashCode(int x, int y) {
		int sum = x + y;
		return sum * (sum + 1) / 2 + x;
	}
	
	@Override
	public String toString() { return "("+x+","+y+")"; }
	
}
