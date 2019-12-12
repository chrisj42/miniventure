package miniventure.game.world.worldgen.island;

import java.util.Random;

import miniventure.game.world.tile.TileTypeEnum;
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
					combine(NoiseGenerator.circleMask(1), MULTIPLY)
				)
			);
			
			float[][] features = island.getFromGen(
				new Coherent2DNoiseFunction(12, 2)
				.modify(FILL_VALUE_RANGE)
			);
			
			TileProcessor map = TileNoiseMap.builder()
				.addRegion(10, WATER)
				.addRegion(5, SAND)
				.addRegion(85, DIRT.append(
					new NoiseTileCondition(features, val -> val < .3).onMatchElse(
						STONE,
						tile -> {
							tile.addLayer(GRASS);
							if (Math.random() > 0.99f)
								tile.addLayer(POOF_TREE);
						}
					)
				)).get(terrain);
			
			island.forEach(map);
		}
		
		@Override
		void generateCaverns(ProtoIsland island) {}
	},
	
	WOODLAND() {
		@Override
		void generateIsland(ProtoIsland island) {
			float[][] shape = island.getFromGen(NoiseGenerator.islandShape);
			
			float[][] trees = island.getFromGen(
				new Coherent2DNoiseFunction(16, 4)
				// .modify(NoiseModifier.combine(NoiseGenerator.islandShapeOld, .35f))
			);
			
			float[][] sparse = island.getFromGen(
				new Coherent2DNoiseFunction(8, 2)
			);
			
			TileProcessor forest = TileConditionChain.builder()
				.add(new NoiseTileCondition(trees, val -> val > .8f), POOF_TREE)
				.add(new NoiseTileCondition(sparse, val -> val > .925f), STONE)
				.getChain();
			
			TileProcessor grassland = TileNoiseMap.builder()
				// .addRegion(25, null)
				.addRegion(35, new NoiseTileCondition(sparse, val -> val <= .075f).onMatch(POOF_TREE))
				.addRoundOffRegion(forest)
				.get(shape);
			
			TileDistanceMap map = TileDistanceMap.builder()
				.atDistance(0, WATER)
				.atRange(1, 5, SAND)
				.atRange(6, 18, GRASS)
				.get(true, GRASS.append(grassland),
					new NoiseTileCondition(shape, val -> val <= .1f)
				);
			
			map.apply(island);
		}
		
		@Override
		void generateCaverns(ProtoIsland island) {
			
		}
	},
	
	DESERT() {
		@Override
		void generateIsland(ProtoIsland island) {
			// describe process
			
			float[][] shape = island.getFromGen(NoiseGenerator.islandShape);
			
			float[][] stone = island.getFromGen(new Coherent2DNoiseFunction(18).modify(combine(NoiseGenerator.circleMask(1), MULTIPLY, NoiseGenerator.circleMask(1)), FILL_VALUE_RANGE));
			
			float[][] trees = island.getFromGen(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1,2,1,2}).modify(FILL_VALUE_RANGE));//.modify(combine(NoiseGenerator.islandMask(1), AVERAGE)));
			
			
			TileConditionChain features = TileConditionChain.builder()
				.add(new NoiseTileCondition(stone, val -> val > .65), STONE)
				// .add(new NoiseTileCondition(stone, val -> val > .6), FLINT)
				.add(new NoiseTileCondition(trees, val -> val > .76), CACTUS)
				.getChain();
			
			TileNoiseMap map = TileNoiseMap.builder()
				.addRegion(15, WATER)
				.addOverlapRegion(SAND, 5, 0, m -> m
					.addRegion(82, features)
				)
				.get(shape);
			
			island.forEach(map);
		}
		
		@Override
		void generateCaverns(ProtoIsland island) {
			
		}
	},
	
	ARCTIC() {
		@Override
		void generateIsland(ProtoIsland island) {
			// describe process
		}
		
		@Override
		void generateCaverns(ProtoIsland island) {
			
		}
	};
	
	private final int width;
	private final int height;
	
	IslandType() { this(600, 600); }
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
	abstract void generateCaverns(ProtoIsland island);
	
	public TileTypeEnum[][][] generateIsland(long seed, boolean surface) {
		ProtoIsland island = new ProtoIsland(seed, width, height);
		if(surface)
			generateIsland(island);
		else
			generateCaverns(island);
		
		if(island.getTile(0, 0).getTopLayer() != WATER) {
			island.getTile(0, 0).addLayer(DOCK);
		}
		else {
			boolean dock = false;
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(island.getTile(x, y).getTopLayer() != WATER) {
						island.getTile(x, y).replaceLayer(WATER);
						island.getTile(x, y).addLayer(DOCK);
						dock = true;
						break;
					}
				}
				if(dock)
					break;
			}
			if(!dock)
				island.getTile(width/2, height/2).addLayer(DOCK);
		}
		
		return island.getMap();
	}
	
	public boolean displayColorMap(long seed, int scale) {
		ProtoIsland island = new ProtoIsland(seed, width, height);
		generateIsland(island);
		
		return Testing.displayMap(width, height, scale, island.getColors());
	}
	public void displayColorMap(boolean repeat, int scale) {
		Random rand = new Random();
		if(!repeat)
			displayColorMap(rand.nextLong(), scale);
		else {
			boolean again = true;
			while(again)
				again = displayColorMap(rand.nextLong(), scale);
		}
	}
}
