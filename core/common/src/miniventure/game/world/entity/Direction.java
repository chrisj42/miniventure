package miniventure.game.world.entity;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.Nullable;

public enum Direction {
	UP, DOWN, LEFT, RIGHT;
	
	public static final Direction[] values = values();
	public static final String[] names = new String[values.length];
	static {
		for(int i = 0; i < values.length; i++)
			names[i] = values[i].name().toLowerCase();
	}
	
	@Nullable
	public static Direction getDirection(float xd, float yd) {
		if (xd == 0 && yd == 0) return null;
		
		if(Math.abs(xd) > Math.abs(yd)) { // the y distance will be more prominent than the x distance (if equal dists, then the direction is up or down).
			if(xd < 0)
				return Direction.LEFT;
			else
				return Direction.RIGHT;
		} else {
			if(yd < 0)
				return Direction.DOWN;
			else
				return Direction.UP;
		}
	}
	
	public Vector2 getVector() {
		Vector2 vector = new Vector2(0, 0);
		
		if(this == UP) vector.y = 1;
		if(this == DOWN) vector.y = -1;
		if(this == LEFT) vector.x = -1;
		if(this == RIGHT) vector.x = 1;
		
		return vector;
	}
}
