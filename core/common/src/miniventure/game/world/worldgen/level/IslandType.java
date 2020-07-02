package miniventure.game.world.worldgen.level;

import java.util.Random;

import miniventure.game.util.MyUtils;
import miniventure.game.world.file.IslandCache;
import miniventure.game.world.worldgen.level.processing.NoiseTileCondition;
import miniventure.game.world.worldgen.level.processing.TileConditionChain;
import miniventure.game.world.worldgen.level.processing.TileDistanceMap;
import miniventure.game.world.worldgen.level.processing.TileNoiseMap;
import miniventure.game.world.worldgen.level.processing.TileProcessor;
import miniventure.game.world.worldgen.noise.Coherent2DNoiseFunction;
import miniventure.game.world.worldgen.noise.Noise;
import miniventure.game.world.worldgen.noise.NoiseGenerator;
import miniventure.game.world.worldgen.noise.NoiseModifier;
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
	
	WOODLAND(protoLevel -> {
		float[][] shape = protoLevel.getFromGen(NoiseGenerator.islandShape);
		
		float[][] trees = protoLevel.getFromGen(
				new Coherent2DNoiseFunction(8, 3)
				// .modify(NoiseModifier.combine(NoiseGenerator.islandShapeOld, .35f))
		);
		float[][] trees2 = protoLevel.getFromGen(
				NoiseGenerator.tunnelPattern(
						new Coherent2DNoiseFunction(16, 5),
						new Coherent2DNoiseFunction(8, 3)
				)
						.modify(NoiseModifier.combine(NoiseGenerator.circleMask(1.4f), MULTIPLY))
		);
		
		float[][] sparse = protoLevel.getFromGen(
				new Coherent2DNoiseFunction(8, 2)
		);
		
		float[][] flint = protoLevel.getFromGen(
				new Coherent2DNoiseFunction(16, 2)
						.modify(NoiseModifier.combine(new Coherent2DNoiseFunction(16, 2), MULTIPLY))
		);
		
		TileProcessor coastal = TileConditionChain.builder()
				.add(new NoiseTileCondition(sparse, val -> val <= .075f), POOF_TREE)
				.add(new NoiseTileCondition(sparse, val -> val > .92f), STONE)
				.add(new NoiseTileCondition(sparse, val -> val > .91f), SMALL_STONE)
				.getChain();
		
		TileProcessor middle = TileConditionChain.builder()
				.add(new NoiseTileCondition(trees, val -> val > .95f), POOF_TREE)
				.add(new NoiseTileCondition(sparse, val -> val > .85f), STONE)
				.add(new NoiseTileCondition(sparse, val -> val > .84f), SMALL_STONE)
				.getChain();
		
		TileProcessor forest = TileConditionChain.builder()
				.add(new NoiseTileCondition(trees2, val -> val > .25f),
						new NoiseTileCondition(sparse, val -> val < .875f).onMatch(POOF_TREE))
				.add(new NoiseTileCondition(sparse, val -> val > .9f), STONE)
				.add(new NoiseTileCondition(sparse, val -> val > .89f), SMALL_STONE)
				.getChain();
		
		TileProcessor grassland = TileNoiseMap.builder()
				// .addRegion(25, null)
				.addRegion(32, coastal)
				.addRegion(10, middle)
				.addRoundOffRegion(forest)
				.get(shape);
		
		TileDistanceMap map = TileDistanceMap.builder()
				.atDistance(0, WATER)
				.atRange(1, 5, SAND)
				.atRange(6, 18, GRASS)
				.get(true, GRASS.append(grassland),
						new NoiseTileCondition(shape, val -> val <= .1f)
				);
		
		map.apply(protoLevel);
		
	}, protoLevel -> {
		
		// WOODLAND CAVERNS
		
		float[][] noise = protoLevel.getFromGen(NoiseGenerator.tunnelPattern(
				new Coherent2DNoiseFunction(16),
				new Coherent2DNoiseFunction(32)
		));
		
		TileNoiseMap map = TileNoiseMap.builder()
				.addRegion(1, DIRT)
				.addRegion(5, STONE)
				.get(noise);
		
		protoLevel.forEach(map);
	}),
	
	DESERT(protoLevel -> {
		
		// DESERT SURFACE
		
		float[][] shape = protoLevel.getFromGen(NoiseGenerator.islandShape);
		
		float[][] terrain = protoLevel.getFromGen(Testing.removeSmallHoles(
				Testing.removeSmallHoles(
						new Noise(new int[] {12, 10, 5}, new int[] {20, 10, 5, 4, 3, 2, 1})
								.modify(FILL_VALUE_RANGE),
						.4f, true, 20, false),
				.7f, false, 20, false)
		);
		
		float[][] elevation = protoLevel.getFromGen(new Coherent2DNoiseFunction(32, 3)
				.modify(NoiseModifier.forEach((noise, x, y) -> noise * noise * noise))
				.modify(
						combine(new Coherent2DNoiseFunction(16, 3).modify(
								combine(new Coherent2DNoiseFunction(16, 3), .5f)
						), .25f)
				));
		
		float[][] moisture = protoLevel.getFromGen(new Coherent2DNoiseFunction(30, 3)
				.modify(NoiseModifier.forEach((noise, x, y) -> (float) Math.pow(noise, 3))));
		
		float[][] stone = protoLevel.getFromGen(new Coherent2DNoiseFunction(18).modify(combine(NoiseGenerator.circleMask(1), MULTIPLY, NoiseGenerator.circleMask(1)), FILL_VALUE_RANGE));
		
		float[][] trees = protoLevel.getFromGen(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1,2,1,2}).modify(FILL_VALUE_RANGE));//.modify(combine(NoiseGenerator.islandMask(1), AVERAGE)));
		
		
		TileConditionChain desert = TileConditionChain.builder()
				// .add(new NoiseTileCondition(stone, val -> val > .65), STONE)
				// .add(new NoiseTileCondition(stone, val -> val > .6), FLINT)
				// .add(new NoiseTileCondition(trees, val -> val > .76), CACTUS)
				.add(tile -> MyUtils.getRandom().nextInt(40) == 0, CACTUS)
				.getChain(SAND);
		
		TileConditionChain biomes = TileConditionChain.builder()
				.add(new NoiseTileCondition(elevation, val -> val > .6f), STONE)
				.add(tile -> tile.getVal(elevation) < .2f && tile.getVal(moisture) > .9f, WATER)
				.getChain();
		
		TileNoiseMap map = TileNoiseMap.builder()
				.addRegion(15, WATER)
				.addOverlapRegion(SAND, 0, 0, m -> m
						.addRegion(82, biomes)
				)
				.get(shape);
		
		protoLevel.forEach(map);
		
		/*TileDistanceMap.builder()
				.atRange(1, 2, TORCH)
				.get(tile -> tile.getTopLayer() == STONE)
				.apply(protoLevel);*/
		
		TileDistanceMap.builder()
				.atRange(1, 4, GRASS)
				.get(tile -> tile.hasType(WATER))
				.apply(protoLevel);
	}),
	
	ARCTIC(),
	
	FINAL();
	
	private final int width;
	private final int height;
	private final MapGenerator[] mapGenerators;
	
	IslandType(MapGenerator... generators) { this(400, 400, generators); }
	IslandType(int width, int height, MapGenerator... generators) {
		this.width = width;
		this.height = height;
		mapGenerators = generators;
	}
	
	public int getLevelCount() {
		return mapGenerators.length;
	}
	
	public ProtoLevel generateLevel(long islandSeed, int depth) {
		Random rand = MyUtils.getRandom(islandSeed);
		for(int i = 1; i < depth; i++) rand.nextLong();
		final long seed = rand.nextLong();
		
		ProtoLevel island = new ProtoLevel(seed, width, height);
		mapGenerators[depth].generateLevel(island);
		if(depth > 0)
			return island;
		
		// ensure dock TODO have the player build one
		if(!island.getTile(0, 0).hasType(WATER)) {
			island.getTile(0, 0).addLayer(DOCK);
		}
		else {
			boolean dock = false;
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(!island.getTile(x, y).hasType(WATER)) {
						island.getTile(x, y).addLayer(WATER);
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
		
		return island;
	}
	
	// testing
	
	public boolean displayColorMap(boolean surface, long seed, int scale) {
		ProtoLevel island = new ProtoLevel(seed, width, height);
		
		mapGenerators[surface?0:1].generateLevel(island);
		
		return Testing.displayMap(width, height, scale, island.getColors());
	}
	public void displayColorMap(boolean surface, boolean repeat, int scale) {
		Random rand = new Random();
		if(!repeat)
			displayColorMap(surface, rand.nextLong(), scale);
		else {
			boolean again = true;
			while(again)
				again = displayColorMap(surface, rand.nextLong(), scale);
		}
	}
	
	public static final IslandType[] values = IslandType.values();
}
