package miniventure.game.world.levelgen;

import miniventure.game.world.tile.TileType;

class TileNoiseLayer {
	
	final int[] samplePeriods;
	final int[] postSmoothing;
	//final TileBound[] tileBounds;
	final TileType[] tiles;
	
	/*public TileNoiseLayer(int[] samplePeriods, int[] postSmoothing, TileType[] tiles, float... ranges) {
		this.samplePeriods = samplePeriods;
		this.postSmoothing = postSmoothing;
		
		tileBounds = new TileBound[tiles.length];
		float cur = 0;
		for(int i = 0; i < tiles.length; i++) {
			float range;
			//if(ranges.length >= i)
		}
	}*/
	
	public TileNoiseLayer(int[] samplePeriods, int[] postSmoothing, TileType... tiles) {
		this.samplePeriods = samplePeriods;
		this.postSmoothing = postSmoothing;
		/*this.tileBounds = new TileBound[tiles.length-1];
		float inc = 1f/tiles.length;
		for(int i = 0; i < tileBounds.length; i++)
			tileBounds[i] = new TileBound(tiles[i], inc*i);*/
		this.tiles = tiles;
	}
	
	public TileType getTile(float val) {
		/*for(TileBound bound: tileBounds)
			if(val <= bound.max)
				return bound.tile;
		
		return null;*/
		return tiles[LevelGenerator.getIndex(tiles.length, val)];
	}
	
	/*private static class TileBound implements Comparable<TileBound> {
		
		private final TileType tile;
		private final float max;
		
		TileBound(TileType tile, float max) {
			this.tile = tile;
			this.max = max;
		}
		
		@Override
		public int compareTo(@NotNull TileNoiseLayer.TileBound o) {
			return Float.compare(max, o.max);
		}
	}*/
}
