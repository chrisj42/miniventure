package miniventure.game.world;

import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.worldgen.LevelGenerator;

public interface LevelFetcher {
	
	Level makeLevel(int levelId, LevelGenerator generator);
	
	Level loadLevel(int levelId, TileData[][] tileData);
	
}
