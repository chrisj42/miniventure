package miniventure.game.world;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.IslandReference;
import miniventure.game.util.Version;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.NotNull;

public class LevelCache {
	
	/*
		There are two competing purposes for this class:
			- info sent to client about positioning and type of island
			- info used by server to generate a new island / load an existing one
		
		So, the server needs this type info... but also some extra, like seed, and entity/tile data.
		Client needs levelid, location, and levelConfig (which is the island type)
	 */
	
	private final IslandReference island;
	
	// gen parameter
	private final long seed;
	
	// load parameters
	private Version dataVersion;
	private String[] entityData;
	private TileData[][] tileData;
	
	public LevelCache(int levelId, Point location, long seed, IslandType islandType) {
		this.island = new IslandReference(levelId, location, islandType);
		this.seed = seed;
		dataVersion = GameCore.VERSION;
	}
	
	public LevelCache(@NotNull Version version, int levelId, Point location, long seed, IslandType islandType, @NotNull TileData[][] tileData, @NotNull String[] entityData) {
		this.island = new IslandReference(levelId, location, islandType);
		this.dataVersion = version;
		this.seed = seed;
		this.tileData = tileData;
		this.entityData = entityData;
	}
	
	public void save(String[] entityData, TileData[][] tileData) {
		this.entityData = entityData;
		this.tileData = tileData;
		dataVersion = GameCore.VERSION;
	}
	
	public Level getLevel(LevelFetcher fetcher) {
		if(tileData != null)
			return fetcher.loadLevel(dataVersion, island.levelId, tileData, entityData);
		
		return fetcher.makeLevel(island.levelId, seed, island.type);
	}
}
