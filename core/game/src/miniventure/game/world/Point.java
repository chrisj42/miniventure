package miniventure.game.world;

public class Point {
	
	public final int x, y;
	
	private Point() { this(0, 0); }
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Point)) return false;
		Point o = (Point) other;
		return x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() {
		return new java.awt.Point(x, y).hashCode();
	}
	
	@Override
	public String toString() { return "("+x+","+y+")"; }
	
}
