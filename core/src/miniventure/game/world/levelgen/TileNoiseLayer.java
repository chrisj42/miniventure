package miniventure.game.world.levelgen;

import miniventure.game.world.tile.TileType;

class TileNoiseLayer {
	
	final int[] samplePeriods;
	final int[] postSmoothing;
	private final TileType[] tiles;
	
	TileNoiseLayer(int[] samplePeriods, int[] postSmoothing, TileType... tiles) {
		this.samplePeriods = samplePeriods;
		this.postSmoothing = postSmoothing;
		this.tiles = tiles;
	}
	
	TileType getTile(float val) {
		return tiles[LevelGenerator.getIndex(tiles.length, val)];
	}
}
