package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;

import miniventure.game.world.levelgen.noise.Coherent2DNoiseFunction;
import miniventure.game.world.levelgen.noise.Noise;
import miniventure.game.world.levelgen.noise.NoiseConfiguration;
import miniventure.game.world.levelgen.noise.NoiseGenerator;
import miniventure.game.world.levelgen.noise.NoiseModifier;
import miniventure.game.world.tile.TileTypeEnum;

import static miniventure.game.world.tile.TileTypeEnum.*;

public class LevelGenerator {
	
	/*
		- levels need to be islands.
		- major value map is going to be elevation, increases as you go to the center
	 */
	
	private static final TileMap PLAINS = new TileMap()
		.addRegion(TileTypeEnum.WATER, 2)
		.addRegion(TileTypeEnum.SAND, 1)
		.addRegion(TileTypeEnum.GRASS, 2)
		.addRegion(TileTypeEnum.STONE_PATH, 1)
		.addRegion(TileTypeEnum.STONE, 2)
		.addRegion(TileTypeEnum.STONE, 2);
	
	public final int levelWidth, levelHeight;
	private final int noiseOffX = 0, noiseOffY = 0; // todo bring back for validation when new algo is made? unless algo doesn't need it...
	
	private TileTypeEnum[][] values;
	
	/*
		- many noise configs
	 */
	
	// public LevelGenerator(long seed) { this(seed, 0, 0); }
	public LevelGenerator(long seed, int width, int height) {
		levelWidth = width;
		levelHeight = height;
		
		// values = Testing.getTerrain().get2DNoise(seed, width, height);
		
		float[][] terrain = new NoiseConfiguration(new Coherent2DNoiseFunction(36, 3))
			.modify(NoiseModifier.combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}), 1))
			.modify(NoiseModifier.FILL_VALUE_RANGE)
			.modify(NoiseModifier.multiply(NoiseGenerator.islandMask(1)))
			// .modify(NoiseModifier.FILL_VALUE_RANGE)
			// .modify(NoiseModifier.combine(NoiseGenerator.islandMask(1), .25f))
			.get2DNoise(seed, width, height);
		
		float[][] features = new NoiseConfiguration(new Coherent2DNoiseFunction(12, 2))
			// NoiseConfiguration trees = new NoiseConfiguration(features)
			.modify(NoiseModifier.FILL_VALUE_RANGE)
			.get2DNoise(seed, width, height);
		
		
		values = new TileTypeEnum[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				float terrainv = terrain[x][y];
				float featurev = features[x][y];
				TileTypeEnum type;
				if(terrainv < .1)
					type = TileTypeEnum.WATER;
				else if(featurev < .05)
					type = TileTypeEnum.STONE;
				else if(terrainv < .15)
					type = TileTypeEnum.SAND;
				else {
					/*float max = 0;
					int r = 3;
					// there are more efficient algorithms than this
					for (int yn = y - r; yn <= y + r; yn++) {
						for (int xn = x - r; xn <= x + r; xn++) {
							if(xn < 0 || yn < 0 || xn >= width || yn >= height) continue;
							float e = features[xn][yn];
							if (e > max) { max = e; }
						}
					}*/
					if (Math.random() > 0.99f)
						type = POOF_TREE;
					else
						type = GRASS;
				}
				values[x][y] = type;
			}
		}
	}
	
	public TileTypeEnum[][][] generateTiles() {
		TileTypeEnum[][][] tiles = new TileTypeEnum[levelWidth][levelHeight][];
		// int xt = x * Chunk.SIZE;
		// int yt = y * Chunk.SIZE;
		
		for(int x = 0; x < tiles.length; x++) {
			for(int y = 0; y < tiles[x].length; y++) {
				// int xp = xt+xo, yp = yt+yo;
				tiles[x][y] = generateTile(x, y);
			}
		}
		
		return tiles;
	}
	
	public TileTypeEnum getTileType(int x, int y) {
		if(x < 0 || y < 0 || x >= levelWidth || y >= levelHeight)
			throw new IllegalArgumentException("Requested tile is outside world bounds; x="+x+", y="+y+". Actual world size: ("+levelWidth+','+levelHeight+')');
		// return null;
		// return PLAINS.getTileType(values[x][y]);
		return values[x][y];
	}
	public TileTypeEnum[] generateTile(int x, int y) {
		return getTile(getTileType(x, y));
	}
	
	private TileTypeEnum[] getTile(TileTypeEnum curType) {
		ArrayList<TileTypeEnum> types = new ArrayList<>();
		while(curType != null) {
			types.add(0, curType);
			curType = underTiles.get(curType);
		}
		
		return types.toArray(new TileTypeEnum[0]);
	}
	
	// may remove this if I get a better algorithm
	private static final EnumMap<TileTypeEnum, TileTypeEnum> underTiles = new EnumMap<TileTypeEnum, TileTypeEnum>(TileTypeEnum.class) {{
		put(WATER, HOLE);
		put(SAND, DIRT);
		put(CACTUS, SAND);
		put(CARTOON_TREE, GRASS);
		put(PINE_TREE, GRASS);
		put(DARK_TREE, DIRT);
		put(POOF_TREE, GRASS);
		put(DIRT, HOLE);
		put(GRASS, DIRT);
		put(STONE, DIRT);
		put(RUBY_ORE, DIRT);
		put(TUNGSTEN_ORE, DIRT);
		put(IRON_ORE, DIRT);
		put(COAL_ORE, DIRT);
		put(FLINT, GRASS);
		put(SNOW, GRASS);
		// put(TORCH, DIRT);
	}};
}
