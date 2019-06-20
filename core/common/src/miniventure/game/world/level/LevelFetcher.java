package miniventure.game.world.level;

import miniventure.game.util.Version;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.worldgen.island.IslandType;

public interface LevelFetcher<L extends Level> {
	
	L makeLevel(int levelId, IslandType islandType);
	
	L loadLevel(final Version version, int levelId, TileData[][] tileData, String[] entityData);
	
}
