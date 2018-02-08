package miniventure.game.world.levelgen;

import java.util.Random;

import miniventure.game.world.tile.TileType;

public class InfiniteLevelGenerator {
	
	private static final BiomeSelector biomeSelector = new BiomeSelector(
		Biome.PLAINS+"_1",
		Biome.FOREST+"_4",
		Biome.PLAINS+"_6",
		Biome.DESERT+"_2",
		Biome.MOUNTAIN+"_3",
		Biome.PLAINS+"_1"
	);
	
	
	private final Coherent2DNoiseFunction biomeNoise, detailNoise;
	
	public InfiniteLevelGenerator(long seed, int biomeSize, int detailSize) {
		Random seedPicker = new Random(seed); // I wonder what would happen if I used a hash function for this, and just used the seed to get another long, and then use that, and so forth...
		biomeNoise = new Coherent2DNoiseFunction(seedPicker.nextLong(), biomeSize);
		detailNoise = new Coherent2DNoiseFunction(seedPicker.nextLong(), detailSize);
	}
	
	public TileType[][] generateTiles(int x, int y, int width, int height) {
		TileType[][] tiles = new TileType[width][height];
		
		for(int xo = 0; xo < width; xo++) {
			for(int yo = 0; yo < height; yo++) {
				Biome biome = biomeSelector.getBiome(biomeNoise.getValue(x, y));
				tiles[xo][yo] = biome.getTile(detailNoise.getValue(x, y));
			}
		}
		
		return tiles;
	}
	
}
