package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;

import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TileType.*;

enum Biome {
	
	OCEAN(WATER+"_1"),
	
	DESERT(SAND+"_15", CACTUS+"_1", SAND+"_5"),
	
	MOUNTAIN(STONE+"_30"/*, TREE_PINE+"_1", STONE+"_5"*/),
	
	CANYON(STONE+"_20", GRASS+"_10", TREE_PINE+"_1", GRASS+"_10", STONE+"_20"),
	
	PLAINS(GRASS+"_24", TREE_CARTOON +"_3", GRASS+"_12", STONE+"_2"),
	
	FOREST(TREE_POOF+"_1", GRASS+"_2", TREE_DARK +"_6");
	
	@NotNull private final EnumFetcher<TileType> tileFetcher;
	
	private static final EnumMap<TileType, TileType> underTiles = new EnumMap<TileType, TileType>(TileType.class) {{
		put(WATER, HOLE);
		put(SAND, DIRT);
		put(CACTUS, SAND);
		put(TREE_CARTOON, GRASS);
		put(TREE_PINE, GRASS);
		put(TREE_DARK, GRASS);
		put(TREE_POOF, GRASS);
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
