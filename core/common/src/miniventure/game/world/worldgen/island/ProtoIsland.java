package miniventure.game.world.worldgen.island;

import java.awt.Color;
import java.util.Random;

import miniventure.game.world.Point;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.noise.GenInfo;
import miniventure.game.world.worldgen.noise.NoiseGenerator;

// passed around to layer makers during island generation
// island starts out like this, and this is passed to level constructor
public class ProtoIsland extends GenInfo {
	
	private final Random random;
	
	// this does not hold the original island seed, but rather the next seed to be given to a generator upon request.
	// private long curSeed;
	
	private final ProtoTile[][] tiles;
	
	// public ProtoIsland(GenInfo info) { this(info.seed, info.width, info.height); }
	public ProtoIsland(long seed, int width, int height) {
		super(seed, width, height);
		random = new Random(seed);
		tiles = new ProtoTile[width][height];
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				tiles[x][y] = new ProtoTile(x, y);
	}
	
	long requestSeed() {
		// curSeed = random.nextLong();
		// random.setSeed(random.nextLong());
		// return curSeed;
		return random.nextLong();
	}
	
	public ProtoTile getTile(Point p) { return getTile(p.x, p.y); }
	public ProtoTile getTile(int x, int y) {
		return tiles[x][y];
	}
	
	void forEach(TileProcessor processor) {
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				processor.processTile(getTile(x, y));
	}
	
	public TileTypeEnum[][][] getMap() {
		TileTypeEnum[][][] types = new TileTypeEnum[width][height][];
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				types[x][y] = getTile(x, y).getStack();
		
		return types;
	}
	
	public Color[][] getColors() {
		Color[][] colors = new Color[width][height];
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				colors[x][y] = getTile(x, y).getTopLayer().color;
		
		return colors;
	}
	
	public float[][] getFromGen(NoiseGenerator gen) {
		return gen.get2DNoise(new GenInfo(requestSeed(), width, height));
	}
}
