package miniventure.game.world.levelgen;

import java.awt.Point;
import java.util.EnumMap;
import java.util.Random;
import java.util.Stack;

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
		TileType.HOLE, TileType.HOLE, TileType.HOLE, TileType.HOLE
	);
	
	private static final Biome[] biomeGen = {
		Biome.PLAINS,
		Biome.FOREST,
		Biome.FOREST,
		Biome.FOREST,
		Biome.FOREST,
		Biome.PLAINS,
		Biome.PLAINS,
		Biome.PLAINS,
		Biome.PLAINS,
		Biome.PLAINS,
		Biome.PLAINS,
		Biome.DESERT,
		Biome.DESERT,
		Biome.MOUNTAIN,
		Biome.MOUNTAIN,
		Biome.MOUNTAIN,
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
			TileType.GRASS, TileType.GRASS, TileType.GRASS,
			TileType.GRASS, TileType.GRASS, TileType.GRASS,
			TileType.GRASS, TileType.GRASS, TileType.GRASS,
			TileType.GRASS, TileType.GRASS, TileType.GRASS,
			TileType.TREE, TileType.GRASS, TileType.GRASS
		)),
		
		DESERT(new TileNoiseLayer(
			new int[] {1},
			new int[] {1},
			TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.SAND, TileType.SAND, TileType.SAND,
			TileType.TREE, TileType.SAND, TileType.SAND, // tree = cactus 
			TileType.SAND, TileType.SAND, TileType.SAND
		)),
		
		MOUNTAIN(new TileNoiseLayer(
			new int[] {},
			new int[] {},
			TileType.HOLE
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
	
	private static int[] makeArray(int start, int end) {
		int dir = end-start < 0 ? -1 : 1;
		int[] nums = new int[Math.abs(end-start)+1];
		for(int i = 0; i < nums.length; i++)
			nums[i] = start+i*dir;
		
		return nums;
	}
	
	static TileType[][] generateLevel(int width, int height) {
		return generateLevel(new Random().nextLong(), width, height);
	}
	static TileType[][] generateLevel(long seed, int width, int height) {
		Random seedPicker = new Random(seed);
		
		
		
		float[][] landData = generateTerrain(seedPicker.nextLong(), width, height, landGen.samplePeriods, landGen.postSmoothing);
		/*boolean[][] land = new boolean[width][height];
		for(int x = 0; x < landData.length; x++)
			for(int y = 0; y < landData[x].length; y++)
				land[x][y] = landData[x][y] >= 0.33f;
		*/
		EnumMap<Biome, float[][]> biomeTerrain = new EnumMap<>(Biome.class);
		for(Biome b: Biome.values)
			biomeTerrain.put(b, b.generateTerrain(seedPicker.nextLong(), width, height));
		
		float[][] biomeData;
		
		TileType[][] tiles = new TileType[width][height];
		
		int[][] biomes = new int[width][height];
		//int[][] biomeAreas = new int[width][height];
		//boolean isValid;
		
		//int curArea;
		
		//do {
			
			biomeData = generateTerrain(seedPicker.nextLong(), width, height, new int[] {20, 10}, new int[] {3, 2, 1});
			
			for (int x = 0; x < biomeData.length; x++)
				for (int y = 0; y < biomeData[x].length; y++)
					biomes[x][y] = getIndex(biomeGen.length, biomeData[x][y]);
			
			
			/*for (int i = 0; i < biomeAreas.length; i++)
				Arrays.fill(biomeAreas[i], -1);
			
			curArea = -1;
			Stack<Point> inArea = new Stack<>();
			Stack<Point> diffArea = new Stack<>();
			diffArea.push(new Point(0, 0));
			
			while (diffArea.size() > 0) {
				Point newArea = diffArea.pop(); // get a point in the next area.
				while (diffArea.size() > 0 && biomeAreas[newArea.x][newArea.y] != -1) // insure that the point is still considered new (could have been put multiple times in this stack).
					newArea = diffArea.pop();
				
				if (biomeAreas[newArea.x][newArea.y] != -1)
					continue; // can't find any unset tiles anywhere, we are done.
				
				curArea++; // set the area number of the new area.
				
				inArea.push(newArea); // add the tile to the new area.
				
				while (inArea.size() > 0) {
					Point next = inArea.pop(); // get the next tile in the current area.
					biomeAreas[next.x][next.y] = curArea; // set this tile to the current area
					checkAreaTiles(biomeAreas, biomes, next.x, next.y, inArea, diffArea); // check surrounding tiles, and add them to appropriate stacks. 
				}
			}
			
			System.out.println("biomes made: " + (curArea + 1));
			*/
			
			
			// check to make sure there are no regions that are excessively big
			/*int[] areaSize = new int[curArea+1];
			for(int x = 0; x < biomeAreas.length; x++)
				for(int y = 0; y < biomeAreas[x].length; y++)
					if(landData[x][y] >= 0.33f)
						areaSize[biomeAreas[x][y]]++;
			*/
			/*isValid = true;
			int max = 0;
			for(int size: areaSize) {
				max = Math.max(max, size);
				if(size > width*height/10)
					isValid = false;
			}
			
			int maxX, maxY;
			int i = 0;
			do {
				maxX = i/height;
				maxY = i%height;
				i++;
			} while(areaSize[biomeAreas[maxX][maxY]] != max);
			*/
			//System.out.println("biggest area: " + max);
			
		//}
		//while(!isValid && false);
		
		
		// biome areas are set, now assign a biome to each number
		/*Biome[] areaBiomes = new Biome[curArea + 1];
		for (int i = 0; i < areaBiomes.length; i++)
			areaBiomes[i] = Biome.values[seedPicker.nextInt(Biome.values.length)];
		*/
		// biomes are set, now assign tile types based on biome
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				TileType landTile = landGen.getTile(landData[x][y]);
				
				if(landTile == TileType.GRASS) {
					Biome biome = biomeGen[biomes[x][y]];//areaBiomes[biomeAreas[x][y]];
					TileType biomeTile = biome.getTile(biomeTerrain.get(biome)[x][y]);
					tiles[x][y] = biomeTile;
				} else
					tiles[x][y] = landTile;
			}
		}
		
		// make sure all water bordered by not-water is sand, a beach.
		/*for(int x = 0; x < tiles.length; x++) {
			for(int y = 0; y < tiles[x].length; y++) {
				if (tiles[x][y] == TileType.WATER) {
					boolean makeSand = false;
					for(int xp = -1; xp <= 1; xp++) {
						for(int yp = -1; yp <= 1; yp++) {
							if(x+xp < 0 || x+xp >= width || y+yp < 0 || y+yp >= height) continue;
							TileType otherType = tiles[x+xp][y+yp];
							if(otherType != TileType.WATER && otherType != TileType.SAND)
								makeSand = true;
						}
					}
					
					if(makeSand)
						tiles[x][y] = TileType.SAND;
				}
			}
		}*/
		
		return tiles;
	}
	
	private static void checkAreaTiles(int[][] biomeAreas, int[][] biomes, int x, int y, Stack<Point> inArea, Stack<Point> diffArea) {
		// it is a given that the current tile is already marked
		
		for(int xi = Math.max(0, x-1); xi <= Math.min(x+1, biomeAreas.length-1); xi++) {
			for(int yi = Math.max(0, y-1); yi <= Math.min(y+1, biomeAreas[xi].length-1); yi++) {
				//if(xi == x && yi == y) continue;
				if(biomeAreas[xi][yi] == -1) {
					Point p = new Point(xi, yi);
					if(biomes[xi][yi] == biomes[x][y])
						inArea.push(p); // add it to the list of tiles in this area.
					else
						diffArea.push(p); // add it to the list of tiles outside this area.
				}
			}
		}
	}
	
	
	static boolean validateLevel(TileType[][] tiles) {
		//if(true) return true;
		
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
			System.out.println(type + " percent: " + tileCounts[type.ordinal()]);
		}
		
		if(tileCounts[TileType.HOLE.ordinal()] < 10) return false; // this is stone
		if(tileCounts[TileType.TREE.ordinal()] < 2) return false;
		if(tileCounts[TileType.WATER.ordinal()] < 15) return false;
		if(tileCounts[TileType.GRASS.ordinal()] < 20) return false;
		if(tileCounts[TileType.SAND.ordinal()] < 8) return false;
		if(tileCounts[TileType.GRASS.ordinal()] + tileCounts[TileType.SAND.ordinal()] < 40) return false;
		
		if(tileCounts[TileType.GRASS.ordinal()] < tileCounts[TileType.SAND.ordinal()]) return false;
		if(tileCounts[TileType.GRASS.ordinal()] + tileCounts[TileType.SAND.ordinal()] < tileCounts[TileType.HOLE.ordinal()]) return false;
		
		
		System.out.println("Map validated!");
		
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
