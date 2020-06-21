package miniventure.game.world.entity.mob.player;

import miniventure.game.world.Point;

public class SpawnLocation {
	
	private final int x;
	private final int y;
	private final int levelId;
	
	SpawnLocation(Point pos, int levelId) { this(pos.x, pos.y, levelId); }
	SpawnLocation(int x, int y, int levelId) {
		this.x = x;
		this.y = y;
		this.levelId = levelId;
	}
	
	SpawnLocation(String data) {
		String[] parts = data.split(";");
		this.x = new Integer(parts[0]);
		this.y = new Integer(parts[1]);
		this.levelId = new Integer(parts[2]);
	}
	
	public String serialize() {
		return x+";"+y+";"+levelId;
	}
	
	// public 
}
