package miniventure.game.world.worldgen.island;

import java.util.Random;

import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.Point;
import miniventure.game.world.tile.TileTypeEnum;

// passed around to layer makers during island generation
// island starts out like this, and this is passed to level constructor
public class ProtoIsland {
	
	private final Random random;
	// this does not hold the original island seed, but rather the next seed to be given to a generator upon request.
	private long curSeed;
	
	final int width, height;
	private final ProtoTile[][] tiles;
	
	public ProtoIsland(int width, int height, long seed) {
		random = new Random(seed);
		this.width = width;
		this.height = height;
		tiles = new ProtoTile[width][height];
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				tiles[x][y] = new ProtoTile(x, y);
	}
	
	public long requestSeed() {
		curSeed = random.nextLong();
		random.setSeed(random.nextLong());
		return curSeed;
	}
	
	public ProtoTile getTile(Point p) { return getTile(p.x, p.y); }
	public ProtoTile getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public void forEach(ValueFunction<ProtoTile> tileAction) {
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				tileAction.act(getTile(x, y));
	}
	
	public TileTypeEnum[][][] getMap() {
		TileTypeEnum[][][] types = new TileTypeEnum[width][height][];
		
		TileTypeEnum[] empty = new TileTypeEnum[0]; // experimental attempt to reduce overhead
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				types[x][y] = getTile(x, y).stack.toArray(empty);
		
		return types;
	}
}
