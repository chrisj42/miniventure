package miniventure.game.world.levelgen;

import java.util.Random;

import miniventure.game.world.Chunk;
import miniventure.game.world.tile.TileType;

public class LevelGenerator {
	
	private static final BiomeSelector biomeSelector = new BiomeSelector(
		/*Biome.PLAINS+"_1",
		Biome.FOREST+"_4",
		Biome.PLAINS+"_6",
		Biome.DESERT+"_2",
		Biome.MOUNTAIN+"_3",
		Biome.PLAINS+"_1"*/
		BiomeCategory.WET+"_2",
		BiomeCategory.DRY+"_5",
		BiomeCategory.ROCKY+"_3"
	);
	
	private static final int MAX_WORLD_SIZE = Integer.MAX_VALUE/2;
	
	private final Coherent2DNoiseFunction categoryNoise, biomeNoise, detailNoise;
	public final int worldWidth, worldHeight;
	
	public LevelGenerator(long seed, int biomeSize, int detailSize) { this(seed, 0, 0, biomeSize, detailSize); }
	public LevelGenerator(long seed, int width, int height, int biomeSize, int detailSize) {
		Random seedPicker = new Random(seed); // I wonder what would happen if I used a hash function for this, and just used the seed to get another long, and then use that, and so forth...
		categoryNoise = new Coherent2DNoiseFunction(seedPicker.nextLong(), 48);
		biomeNoise = new Coherent2DNoiseFunction(seedPicker.nextLong(), biomeSize);
		detailNoise = new Coherent2DNoiseFunction(seedPicker.nextLong(), detailSize);
		
		if(width < 0 || width > MAX_WORLD_SIZE || height < 0 || height > MAX_WORLD_SIZE)
			throw new IllegalArgumentException("illegal world size; dimensions of world must be non-negative and not greater than " + MAX_WORLD_SIZE + "; given dimensions: " + width+","+height);
		
		if(width == 0) width = MAX_WORLD_SIZE;
		if(height == 0) height = MAX_WORLD_SIZE;
		
		worldWidth = width;
		worldHeight = height;
	}
	
	public boolean chunkExists(int x, int y) {
		if(x < 0 || y < 0) return false;
		
		if(x * Chunk.SIZE >= worldWidth || y * Chunk.SIZE >= worldHeight)
			return false;
		
		return true;
	}
	
	public TileType[][] generateChunk(final int x, final int y) {
		int width = Chunk.SIZE, height = Chunk.SIZE;
		if((x+1) * width > worldWidth)
			width = worldWidth - x*width;
		if((y+1) * height > worldHeight)
			height = worldHeight - x*height;
		
		if(width <= 0 || height <= 0)
			throw new IndexOutOfBoundsException("Requested chunk is outside world bounds; chunkX="+x+", chunkY="+y+". Max x requested: "+((x+1)*Chunk.SIZE-1)+", max y requested: "+((y+1)*Chunk.SIZE-1)+". Actual world size: ("+worldWidth+","+worldHeight+")");
		
		return generateTiles(x, y, width, height);
	}
	
	TileType[][] generateTiles(int x, int y, int width, int height) {
		TileType[][] tiles = new TileType[width][height];
		int xt = x * Chunk.SIZE;
		int yt = y * Chunk.SIZE;
		
		for(int xo = 0; xo < tiles.length; xo++) {
			for(int yo = 0; yo < tiles[xo].length; yo++) {
				int xp = xt+xo, yp = yt+yo;
				tiles[xo][yo] = generateTile(xp, yp);
			}
		}
		
		return tiles;
	}
	
	public TileType generateTile(int x, int y) {
		if(x < 0 || y < 0 || x >= worldWidth || y >= worldHeight)
			throw new IllegalArgumentException("Requested tile is outside world bounds; x="+x+", y="+y+". Actual world size: ("+worldWidth+","+worldHeight+")");
		
		Biome biome = biomeSelector.getBiome(categoryNoise.getValue(x, y)).getBiome(biomeNoise.getValue(x, y));
		return biome.getTile(detailNoise.getValue(x, y));
	}
}
