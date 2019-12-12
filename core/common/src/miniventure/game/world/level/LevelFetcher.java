package miniventure.game.world.level;

import miniventure.game.util.Version;
import miniventure.game.world.file.LevelCache;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.worldgen.island.IslandType;

public interface LevelFetcher<L extends Level> {
	
	L makeLevel(LevelCache cache);
	
	L loadLevel(LevelCache cache, final Version version, TileData[][] tileData, String[] entityData);
	
}
