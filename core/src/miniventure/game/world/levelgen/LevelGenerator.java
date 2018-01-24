package miniventure.game.world.levelgen;

import java.util.EnumMap;
import java.util.Random;

import miniventure.game.world.tile.TileType;

public class LevelGenerator {
	
	
	
	/*
		So, each level is going to be built like this:
		
			First, a base noise map is generated. This usually distinguishes between 2 different tiles, but it might be three, or something.
				To specify the range, an array of tile types is given, followed by an array of floats, one less in length. the floats are the separations between each pair of tiles.
			
			On top of the base noise map, another 
	 */
	
	private static final TileNoiseLayer landGen = new TileNoiseLayer(
		new int[] {50, 40, 30, 25, 20, 10},
		new int[] {20, 4, 3, 3, 2, 2, 1, 1},
		TileType.WATER, TileType.WATER, TileType.WATER, TileType.WATER, TileType.WATER,
		TileType.SAND,
		TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS,
		TileType.STONE, TileType.STONE, TileType.STONE, TileType.STONE
	);
	
	private static final int[] biomeSample = {20, 10};
	private static final int[] biomeSmooth = {3, 2, 1};
	private static final Biome[] biomeGen = {
		Biome.PLAINS,
		Biome.FOREST, Biome.FOREST, Biome.FOREST, Biome.FOREST,
		Biome.PLAINS, Biome.PLAINS, Biome.PLAINS, Biome.PLAINS, Biome.PLAINS, Biome.PLAINS,
		Biome.DESERT, Biome.DESERT,
		Biome.MOUNTAIN, Biome.MOUNTAIN, Biome.MOUNTAIN,
		Biome.PLAINS
	};
	
	enum Biome {
		//OCEAN(TileType.WATER),
		
		FOREST(new TileNoiseLayer(
			new int[] {10, 1, 2},
			new int[] {5, 2},
			TileType.GRASS, TileType.TREE, TileType.GRASS
		)),
		
		PLAINS(new TileNoiseLayer(// maybe flowers later
			new int[] {1},
			new int[] {1},
			TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS,
			TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS,
			TileType.TREE, TileType.GRASS, TileType.GRASS
		)),
		
		DESERT(new TileNoiseLayer(
			new int[] {1},
			new int[] {1},
			TileType.SAND, TileType.SAND, TileType.SAND, TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.SAND, TileType.SAND, TileType.SAND, TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.SAND, TileType.SAND, TileType.SAND, TileType.CACTUS, TileType.SAND, TileType.SAND,  
			TileType.SAND, TileType.SAND, TileType.SAND
		)),
		
		MOUNTAIN(new TileNoiseLayer(
			new int[] {},
			new int[] {},
			TileType.STONE
		));
		
		private final TileNoiseLayer noiseMap;
		
		Biome(TileNoiseLayer noiseMap) {
			this.noiseMap = noiseMap;
		}
		
		public float[][] generateTerrain(long seed, int width, int height) {
			return LevelGenerator.generateTerrain(seed, width, height, noiseMap.samplePeriods, noiseMap.postSmoothing);
		}
		
		public TileType getTile(float val) {
			return noiseMap.getTile(val);
		}
		
		public static final Biome[] values = Biome.values();
	}
	
	public static TileType[][] generateLevel(int width, int height) {
		return generateLevel(new Random().nextLong(), width, height);
	}
	public static TileType[][] generateLevel(long seed, int width, int height) {
		Random seedGen = new Random(seed);
		
		TileType[][] tiles;
		do tiles = generateLevel(seedGen, width, height);
		while(!validateLevel(tiles));
		
		return tiles;
	}
	
	private static TileType[][] generateLevel(Random seedPicker, int width, int height) {
		TileType[][] tiles = new TileType[width][height];
		
		float[][] landData = generateTerrain(seedPicker.nextLong(), width, height, landGen.samplePeriods, landGen.postSmoothing);
		float[][] biomeData = generateTerrain(seedPicker.nextLong(), width, height, biomeSample, biomeSmooth);
		
		EnumMap<Biome, float[][]> biomeTerrain = new EnumMap<>(Biome.class);
		for(Biome b: Biome.values)
			biomeTerrain.put(b, b.generateTerrain(seedPicker.nextLong(), width, height));
		
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				tiles[x][y] = landGen.getTile(landData[x][y]);
				if(tiles[x][y] == TileType.GRASS) {
					Biome biome = biomeGen[getIndex(biomeGen.length, biomeData[x][y])];
					tiles[x][y] = biome.getTile(biomeTerrain.get(biome)[x][y]);
				}
			}
		}
		
		return tiles;
	}
	
	private static boolean validateLevel(TileType[][] tiles) {
		int[] tileCounts = new int[TileType.values.length];
		int total = 0;
		
		for(int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				tileCounts[tiles[x][y].ordinal()]++;
				total++;
			}
		}
		
		for(TileType type: TileType.values) {
			tileCounts[type.ordinal()] = tileCounts[type.ordinal()] * 100 / total; // converts it to a percent, 0 to 100
			//System.out.println(type + " percent: " + tileCounts[type.ordinal()]);
		}
		
		if(tileCounts[TileType.STONE.ordinal()] < 10) return false; // this is stone
		if(tileCounts[TileType.TREE.ordinal()] < 2) return false;
		if(tileCounts[TileType.WATER.ordinal()] < 15) return false;
		if(tileCounts[TileType.GRASS.ordinal()] < 20) return false;
		if(tileCounts[TileType.SAND.ordinal()] < 8) return false;
		if(tileCounts[TileType.GRASS.ordinal()] + tileCounts[TileType.SAND.ordinal()] < 40) return false;
		
		if(tileCounts[TileType.GRASS.ordinal()] < tileCounts[TileType.SAND.ordinal()]) return false;
		if(tileCounts[TileType.GRASS.ordinal()] + tileCounts[TileType.SAND.ordinal()] < tileCounts[TileType.STONE.ordinal()]) return false;
		
		//System.out.println("Map validated!");
		
		return true;
	}
	
	
	static float[][] generateTerrain(long seed, int width, int height, int[] samplePeriods, int[] postSmoothing) {
		float[] noise = Noise.getWhiteNoise(seed, width*height);
		float[][] noises = Noise.smoothNoise2D(noise, width, height, samplePeriods);
		float[] smoothNoise = Noise.addNoiseWeighted(noises);
		smoothNoise = Noise.smoothNoise2DProgressive(smoothNoise, width, height, postSmoothing);
		smoothNoise = Noise.map(smoothNoise, 0, 1);
		
		float[][] smooth2D = new float[width][height];
		for(int i = 0; i < smoothNoise.length; i++)
			smooth2D[i/height][i%height] = smoothNoise[i];
		
		return smooth2D;
	}
	
	static int getIndex(int arrayLength, float val) {
		return Math.min(arrayLength-1, (int) (val*arrayLength));
	}
}
