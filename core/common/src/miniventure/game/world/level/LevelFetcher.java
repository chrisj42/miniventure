package miniventure.game.world.level;

import miniventure.game.util.Version;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.worldgen.island.IslandType;

public interface LevelFetcher {
	
	Level makeLevel(int levelId, long seed, IslandType islandType);
	
	Level loadLevel(final Version version, int levelId, TileData[][] tileData, String[] entityData);
	
}
