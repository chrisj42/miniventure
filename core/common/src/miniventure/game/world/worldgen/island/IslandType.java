package miniventure.game.world.worldgen.island;

import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.noise.Testing;

public enum IslandType {
	
	// note: "refined island shape" refers to using the noise config and threshold from Testing.main(), and then removing all but the biggest match group (represents land), and any non-match group (represents water) that doesn't have a tile on the edge of the map. This results in a single landmass that has no holes.
	
	STARTER() {
		@Override
		void generateIsland(ProtoIsland island) {
			/*
				- refined island shape
					- fill land as dirt
						- all land within 2 tiles of sea is sand
						- using noise for sand really doesn't work out too well because I post-process the island, and noise values will reflect the inconsistencies that the post-processing tried to fix.
					- map perimeter is deep water 
					-- else if noise value is just under land threshold, or tile is within certain radius from center, it's water
					-- else (i.e. low noise value and outside radius) it's deep water
				
				
				
				- usage of noise repeatedly requires noise to be created beforehand and passed in, so it remains available
			 */
			
			float[][] terrain = Testing.getTerrain().get2DNoise(island.requestSeed(), island.width, island.height);
			
			TileGroupMap groupMap = TileGroupMap.process(new NoiseTileCondition(terrain, val -> val > .2f), island);
			
			
		}
	},
	
	DESERT() {
		@Override
		void generateIsland(ProtoIsland island) {
			// describe process
		}
	},
	
	SWAMP() {
		@Override
		void generateIsland(ProtoIsland island) {
			// describe process
		}
	},
	
	ARCTIC() {
		@Override
		void generateIsland(ProtoIsland island) {
			// describe process
		}
	},
	
	JUNGLE() {
		@Override
		void generateIsland(ProtoIsland island) {
			// describe process
		}
	};
	
	// only accessible by instances created in this class.
	IslandType() {}
	
	/*
		This method is where the island type gets liberties with how it generates the island.
	 	- structures such as tile groups are complicated to pin down to a certain step; moreso, noise
	 	functions need to be initialized correctly...
	 	todo - I should probably, again, wait until I have an actual use case...
	 */
	abstract void generateIsland(ProtoIsland island);
	
	public TileTypeEnum[][][] generateIsland(long seed, int width, int height) {
		ProtoIsland island = new ProtoIsland(width, height, seed);
		generateIsland(island);
		return island.getMap();
	}
}
