package miniventure.game.world.levelgen;

import miniventure.game.world.tile.TileType.TileTypeEnum;

@FunctionalInterface
public interface NoiseTileFetcher {
	TileTypeEnum getTileType(int x, int y, Object[] parameters);
}
