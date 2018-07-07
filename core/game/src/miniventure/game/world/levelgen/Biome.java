package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;

import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TileType.TileTypeEnum.*;

enum Biome {
	
	OCEAN(WATER+"_1"),
	
	DESERT(SAND+"_15", CACTUS+"_1", SAND+"_5"),
	
	MOUNTAIN(STONE+"_30"/*, TREE_PINE+"_1", STONE+"_5"*/),
	
	CANYON(STONE+"_20", GRASS+"_10", TREE_PINE+"_1", GRASS+"_10", STONE+"_20"),
	
	PLAINS(TREE_CARTOON +"_2", GRASS+"_18", STONE+"_1"),
	
	FOREST(GRASS+"_3", TREE_POOF+"_6");
	
	@NotNull private final EnumFetcher<TileTypeEnum> tileFetcher;
	
	private static final EnumMap<TileTypeEnum, TileTypeEnum> underTiles = new EnumMap<TileTypeEnum, TileTypeEnum>(TileTypeEnum.class) {{
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
		tileFetcher = new EnumFetcher<>(TileTypeEnum.class, tilesWithOccurrences);
	}
	
	public TileTypeEnum[] getTile(float noise) {
		ArrayList<TileTypeEnum> types = new ArrayList<>();
		TileTypeEnum curType = tileFetcher.getType(noise);
		while(curType != null) {
			types.add(0, curType);
			curType = underTiles.get(curType);
		}
		
		return types.toArray(new TileTypeEnum[types.size()]);
	}
}
