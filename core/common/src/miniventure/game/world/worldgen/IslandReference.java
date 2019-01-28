package miniventure.game.world.worldgen;

import miniventure.game.world.Level;
import miniventure.game.world.LevelFetcher;
import miniventure.game.world.Point;
import miniventure.game.world.tile.Tile.TileData;

public class IslandReference {
	
	private final int levelId;
	private final Point location;
	
	// gen parameters
	private final long seed;
	private final LevelConfiguration levelConfig;
	
	// load parameters
	private String[] entityData;
	private TileData[][] tileData;
	
	public IslandReference(int levelId, Point location, long seed, LevelConfiguration levelConfig) {
		this.levelId = levelId;
		this.location = location;
		this.seed = seed;
		this.levelConfig = levelConfig;
	}
	
	public IslandReference(int levelId, Point location, long seed, LevelConfiguration levelConfig, TileData[][] tileData, String[] entityData) {
		this.levelId = levelId;
		this.location = location;
		this.seed = seed;
		this.levelConfig = levelConfig;
		this.tileData = tileData;
		this.entityData = entityData;
	}
	
	public void save(String[] entityData, TileData[][] tileData) {
		this.entityData = entityData;
		this.tileData = tileData;
	}
	
	public Level getLevel(LevelFetcher fetcher) {
		if(tileData != null)
			fetcher.loadLevel(levelId, tileData);
	}
}
