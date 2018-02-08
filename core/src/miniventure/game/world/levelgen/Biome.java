package miniventure.game.world.levelgen;

import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TileType.CACTUS;
import static miniventure.game.world.tile.TileType.GRASS;
import static miniventure.game.world.tile.TileType.SAND;
import static miniventure.game.world.tile.TileType.STONE;
import static miniventure.game.world.tile.TileType.TREE;
import static miniventure.game.world.tile.TileType.WATER;

enum Biome {
	
	OCEAN(WATER+"_1"),
	
	DESERT(SAND+"_15", CACTUS+"_1", SAND+"_5"),
	
	MOUNTAIN(STONE+"_1"),
	
	PLAINS(GRASS+"_8", TREE+"_1", GRASS+"_2"),
	
	FOREST(GRASS+"_2", TREE+"_1", GRASS+"_2");
	
	
	@NotNull private final EnumFetcher<TileType> tileFetcher;
	
	Biome(@NotNull String... tilesWithOccurances) {
		tileFetcher = new EnumFetcher<>(TileType.class, tilesWithOccurances);
	}
	
	public TileType getTile(float noise) { return tileFetcher.getType(noise); }
}
