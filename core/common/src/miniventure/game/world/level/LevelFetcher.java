package miniventure.game.world.level;

import miniventure.game.util.Version;
import miniventure.game.world.file.LevelCache;

public interface LevelFetcher<L extends Level> {
	
	L makeLevel(LevelCache cache, long seed);
	
	L loadLevel(LevelCache cache, final Version version, TileMapData tileData, String[] entityData);
	
}
