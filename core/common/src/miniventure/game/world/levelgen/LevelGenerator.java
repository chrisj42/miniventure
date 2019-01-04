package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;

import miniventure.game.world.levelgen.noise.Noise;
import miniventure.game.world.levelgen.noise.NoiseConfig;
import miniventure.game.world.levelgen.noise.NoiseConfiguration;
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
	
	private float[][] values;
	
	public LevelGenerator(long seed) { this(seed, 0, 0); }
	public LevelGenerator(long seed, int width, int height) {
		levelWidth = width;
		levelHeight = height;
		
		// make height map
		// 
		// NoiseConfig heightMap = new NoiseConfig(seed, width, height, ), true, true);
		// multiply height map by island mask
		// values = heightMap.getNoise();
		NoiseConfiguration config = new NoiseConfiguration(
			new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1}),
			NoiseModifier.FILL_VALUE_RANGE,
			NoiseModifier.ISLAND_MASK,
			NoiseModifier.FILL_VALUE_RANGE
		);
		
		values = config.get2DNoise(seed, width, height);
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
		return PLAINS.getTileType(values[x][y]);
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
