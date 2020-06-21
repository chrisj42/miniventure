package miniventure.game.world.management;

import miniventure.game.util.Version;
import miniventure.game.world.Point;

public class LevelDataSet {
	
	public final int id;
	public final int width;
	public final int height;
	// public final long seed;
	
	public final Version dataVersion;
	public final String[][] tileData;
	public final String[] entityData;
	
	public final Point travelPos;
	
	public LevelDataSet(int id) {
		this(id, 0, 0, null, new String[0][], new String[0], null);
	}
	public LevelDataSet(int id, int width, int height, Version dataVersion, String[][] tileData, String[] entityData, Point travelPos) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.dataVersion = dataVersion;
		// this.seed = seed;
		this.tileData = tileData;
		this.entityData = entityData;
		this.travelPos = travelPos;
	}
}
