package miniventure.game.world.worldgen.island;

import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.TileNoiseMap.TileMapBuilder;
import miniventure.game.world.worldgen.island.TileProcessor.TileMultiProcess;
import miniventure.game.world.worldgen.island.TileProcessorChain.ProcessChainBuilder;
import miniventure.game.world.worldgen.noise.Coherent2DNoiseFunction;
import miniventure.game.world.worldgen.noise.Noise;
import miniventure.game.world.worldgen.noise.NoiseGenerator;
import miniventure.game.world.worldgen.noise.Testing;

import static miniventure.game.world.tile.TileTypeEnum.*;
import static miniventure.game.world.worldgen.noise.NoiseModifier.FILL_VALUE_RANGE;
import static miniventure.game.world.worldgen.noise.NoiseModifier.NoiseValueMerger.MULTIPLY;
import static miniventure.game.world.worldgen.noise.NoiseModifier.combine;

public enum IslandType {
	
	// note: "refined island shape" refers to using the noise config and threshold from Testing.main(), and then removing all but the biggest match group (represents land), and any non-match group (represents water) that doesn't have a tile on the edge of the map. This results in a single landmass that has no holes.
	
	/*
	++ from TerrainGenerator (used to be LevelGenerator) class:
	
		- many noise configs
		- since there are multiple ways in which tiles can be filled, there ought to be a sequence of tile applications
		- applications are valid only on certain tiles
		- for each tile, go backward on the application list until you find one that is valid
		
		- in terms of biomes, I just need to diversify the flora and fauna there
			- no plains, but instead just "woodland" or "tropical"
				- open areas
				- forested areas
				- marshy areas
				- lake areas
				- maybe mountains..? or just hills?
			- "arctic"
				- similar to tropical but snow on everything
				- foresty areas
				- frozen lakes
				- more mountains than tropical
			- desert
				- lots of sand dunes
				- some oases
				- variety of fauna, hopefully more plants than just "cactus"; maybe dead bushes?
			- mountains/rocky
				- lots of mountains
				- pre-made caves?
			
	 */
	
	// a background for the main menu
	MENU(100, 100) {
		@Override
		void generateIsland(ProtoIsland island) {
			float[][] terrain = island.getFromGen(
				new Coherent2DNoiseFunction(36, 3).modify(
					combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1})),
					FILL_VALUE_RANGE,
					combine(NoiseGenerator.islandMask(1), MULTIPLY)
				)
			);
			
			float[][] features = island.getFromGen(
				new Coherent2DNoiseFunction(12, 2)
				.modify(FILL_VALUE_RANGE)
			);
			
			// I could do this entirely (or partly) with the interfaces... but I'll try not to make so many objects.
			TileProcessor map = new TileMapBuilder
				(10, WATER)
				.addRegion(5, SAND)
				.addRegion(85, new TileMultiProcess(
					DIRT,
					new TileDelegator(
						new NoiseTileCondition(features, val -> val < .3),
						STONE,
						tile -> {
							tile.addLayer(GRASS);
							if (Math.random() > 0.99f)
								tile.addLayer(POOF_TREE);
						}
					)
				)).get(terrain);
			
			/*TileProcessor ifelse = tile -> {
				float terrainv = tile.getVal(terrain);
				float featurev = tile.getVal(features);
				if(terrainv < .1)
					tile.addLayer(WATER);
				else if(terrainv < .15)
					tile.addLayer(SAND);
				else {
					tile.addLayer(DIRT);
					if(featurev < .3)
						tile.addLayer(STONE);
					else {
						tile.addLayer(GRASS);
						if (Math.random() > 0.99f)
							tile.addLayer(POOF_TREE);
					}
				}
			};*/
			
			island.forEach(map);
		}
	},
	
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
			
			float[][] shape = island.getFromGen(Testing.getTerrain());
			
			float[][] stone = island.getFromGen(new Coherent2DNoiseFunction(18).modify(combine(NoiseGenerator.islandMask(1), MULTIPLY, NoiseGenerator.islandMask(1)), FILL_VALUE_RANGE));
			
			float[][] trees = island.getFromGen(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1,2,1,2}).modify(FILL_VALUE_RANGE));//.modify(combine(NoiseGenerator.islandMask(1), AVERAGE)));
			
			
			TileProcessorChain features = new ProcessChainBuilder()
				.add(new NoiseTileCondition(stone, val -> val > .65), TileTypeEnum.STONE)
				.add(new NoiseTileCondition(trees, val -> val > .76), TileTypeEnum.POOF_TREE)
				.getChain();
			
			TileNoiseMap map = new TileMapBuilder(
				15, TileTypeEnum.WATER)
				.addRegion(3, TileTypeEnum.SAND)
				.addRegion(82, new TileMultiProcess(
					TileTypeEnum.GRASS,
					features
				))
				.get(shape);
			
			island.forEach(map);
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
			
			float[][] shape = island.getFromGen(Testing.getTerrain());
			
			float[][] trees = island.getFromGen(new Coherent2DNoiseFunction(24));
			
			island.forEach(new TileDelegator(
				new NoiseTileCondition(shape, val -> val < .18f),
				GRASS,
				new TileMapBuilder(
					1, POOF_TREE)
				.addRegion(2, PINE_TREE)
				.addRegion(2, CARTOON_TREE)
				.addRegion(1, POOF_TREE).get(trees)
			));
		}
	};
	
	private final int width;
	private final int height;
	
	IslandType() { this(300, 300); }
	IslandType(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	/*
		This method is where the island type gets liberties with how it generates the island.
	 	- structures such as tile groups are complicated to pin down to a certain step; moreso, noise
	 	functions need to be initialized correctly...
	 	todo - I should probably, again, wait until I have an actual use case...
	 */
	abstract void generateIsland(ProtoIsland island);
	
	public TileTypeEnum[][][] generateIsland(long seed) {
		ProtoIsland island = new ProtoIsland(seed, width, height);
		generateIsland(island);
		return island.getMap();
	}
	
	public boolean displayColorMap(long seed, int scale) {
		ProtoIsland island = new ProtoIsland(seed, width, height);
		generateIsland(island);
		
		return Testing.displayMap(width, height, scale, island.getColors());
	}
}
