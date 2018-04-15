package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;

import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TileType.*;

enum Biome {
	
	OCEAN(WATER+"_1"),
	
	DESERT(SAND+"_15", CACTUS+"_1", SAND+"_5"),
	
	MOUNTAIN(STONE+"_1"),
	
	PLAINS(GRASS+"_8", TREE+"_1", GRASS+"_2"),
	
	FOREST(GRASS+"_2", TREE+"_1", GRASS+"_2");
	
	@NotNull private final EnumFetcher<TileType> tileFetcher;
	
	private static final EnumMap<TileType, TileType> underTiles = new EnumMap<TileType, TileType>(TileType.class) {{
		put(WATER, HOLE);
		put(SAND, DIRT);
		put(CACTUS, SAND);
		put(TREE, GRASS);
		put(DIRT, HOLE);
		put(GRASS, DIRT);
		put(STONE, DIRT);
	}};
	
	Biome(@NotNull String... tilesWithOccurrences) {
		tileFetcher = new EnumFetcher<>(TileType.class, tilesWithOccurrences);
	}
	
	public TileType[] getTile(float noise) {
		ArrayList<TileType> types = new ArrayList<>();
		TileType curType = tileFetcher.getType(noise);
		while(curType != null) {
			types.add(0, curType);
			curType = underTiles.get(curType);
		}
		
		return types.toArray(new TileType[types.size()]);
	}
}
