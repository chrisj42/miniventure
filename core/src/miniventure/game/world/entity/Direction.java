package miniventure.game.world.entity;

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
	public static Direction getDirection(int xd, int yd) {
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
}
