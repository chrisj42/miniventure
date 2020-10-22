package miniventure.game.world.worldgen.island;

import java.util.Random;

import miniventure.game.util.MyUtils;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.worldgen.LevelGenerator;
import miniventure.game.world.worldgen.noise.Coherent2DNoiseFunction;
import miniventure.game.world.worldgen.noise.Noise;
import miniventure.game.world.worldgen.noise.NoiseGenerator;
import miniventure.game.world.worldgen.noise.NoiseModifier;
import miniventure.game.world.worldgen.noise.Testing;

import org.jetbrains.annotations.NotNull;

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
	MENU(100, 100, level -> {
		
		// -- MENU MAP --
		
		float[][] terrain = level.getFromGen(
				new Coherent2DNoiseFunction(36, 3).modify(
						combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1})),
						FILL_VALUE_RANGE,
						combine(NoiseGenerator.circleMask(1), MULTIPLY)
				)
		);
		
		float[][] features = level.getFromGen(
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
		
		level.forEach(map);
	}, level -> {}),
	
	WOODLAND(level -> {
		
		// -- WOODLAND SURFACE --
		
		float[][] shape = level.getFromGen(NoiseGenerator.islandShape);
		
		float[][] trees = level.getFromGen(
				new Coherent2DNoiseFunction(8, 3)
				// .modify(NoiseModifier.combine(NoiseGenerator.islandShapeOld, .35f))
		);
		float[][] trees2 = level.getFromGen(
				NoiseGenerator.tunnelPattern(
						new Coherent2DNoiseFunction(16, 5),
						new Coherent2DNoiseFunction(8, 3)
				)
						.modify(NoiseModifier.combine(NoiseGenerator.circleMask(1.4f), MULTIPLY))
		);
		
		float[][] sparse = level.getFromGen(
				new Coherent2DNoiseFunction(8, 2)
		);
		
		float[][] flint = level.getFromGen(
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
		
		map.apply(level);
		
	}, level -> {
		
		// -- WOODLAND CAVERNS --
		
		float[][] noise = level.getFromGen(NoiseGenerator.tunnelPattern(
				new Coherent2DNoiseFunction(16),
				new Coherent2DNoiseFunction(32)
		));
		
		TileNoiseMap map = TileNoiseMap.builder()
				.addRegion(1, DIRT)
				.addRegion(5, STONE)
				.get(noise);
		
		level.forEach(map);
	}),
	
	DESERT(level -> {
		
		// -- DESERT SURFACE --
		
		// describe process
		
		float[][] shape = level.getFromGen(NoiseGenerator.islandShape);
		
		float[][] terrain = level.getFromGen(Testing.removeSmallHoles(
				Testing.removeSmallHoles(
						new Noise(new int[] {12, 10, 5}, new int[] {20, 10, 5, 4, 3, 2, 1})
								.modify(FILL_VALUE_RANGE),
						.4f, true, 20, false),
				.7f, false, 20, false)
		);
		
		float[][] elevation = level.getFromGen(new Coherent2DNoiseFunction(32, 3)
				.modify(NoiseModifier.forEach((noise, x, y) -> noise * noise * noise))
				.modify(
						combine(new Coherent2DNoiseFunction(16, 3).modify(
								combine(new Coherent2DNoiseFunction(16, 3), .5f)
						), .25f)
				));
		
		float[][] moisture = level.getFromGen(new Coherent2DNoiseFunction(30, 3)
				.modify(NoiseModifier.forEach((noise, x, y) -> (float) Math.pow(noise, 3))));
		
		float[][] stone = level.getFromGen(new Coherent2DNoiseFunction(18).modify(combine(NoiseGenerator.circleMask(1), MULTIPLY, NoiseGenerator.circleMask(1)), FILL_VALUE_RANGE));
		
		float[][] trees = level.getFromGen(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1,2,1,2}).modify(FILL_VALUE_RANGE));//.modify(combine(NoiseGenerator.islandMask(1), AVERAGE)));
		
		
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
		
		level.forEach(map);
			
			/*TileDistanceMap.builder()
					.atRange(1, 2, TORCH)
					.get(tile -> tile.getTopLayer() == STONE)
					.apply(island);*/
		
		TileDistanceMap.builder()
				.atRange(1, 4, GRASS)
				.get(tile -> tile.getTopLayer() == WATER)
				.apply(level);
		
	}, level -> {
		
		// -- DESERT CAVERNS --
		
		
	}),
	
	ARCTIC(level -> {
		
		// -- ARCTIC SURFACE --
		
		
	}, level -> {
		
		// -- ARCTIC CAVERNS --
		
		
	}),
	
	FINAL(level -> {
		
		// -- FINAL BOSS SURFACE --
		
		
	}, level -> {
		
		// -- FINAL BOSS CAVERNS --
		
		
	});
	
	public final int width;
	public final int height;
	private final LevelGenerator surfaceGenerator;
	private final LevelGenerator cavernGenerator;
	
	IslandType(LevelGenerator surfaceGenerator, LevelGenerator cavernGenerator) { this(400, 400, surfaceGenerator, cavernGenerator); }
	IslandType(int width, int height, LevelGenerator surfaceGenerator, LevelGenerator cavernGenerator) {
		this.width = width;
		this.height = height;
		this.surfaceGenerator = surfaceGenerator;
		this.cavernGenerator = cavernGenerator;
	}
	
	private LevelGenerator getGenerator(boolean surface) {
		return surface ? surfaceGenerator : cavernGenerator;
	}
	
	public ProtoLevel generateLevel(@NotNull WorldManager world, long seed, boolean surface) {
		ProtoLevel level = getGenerator(surface).generateLevel(world, width, height, seed);
		
		if(level.getTile(0, 0).getTopLayer() != WATER) {
			level.getTile(0, 0).addLayer(DOCK);
		}
		else {
			boolean dock = false;
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(level.getTile(x, y).getTopLayer() != WATER) {
						level.getTile(x, y).replaceLayer(WATER);
						level.getTile(x, y).addLayer(DOCK);
						dock = true;
						break;
					}
				}
				if(dock)
					break;
			}
			if(!dock)
				level.getTile(width/2, height/2).addLayer(DOCK);
		}
		
		return level;
	}
	
	public boolean displayColorMap(@NotNull WorldManager world, boolean surface, long seed, int scale) {
		ProtoLevel level = getGenerator(surface).generateLevel(world, width, height, seed);
		
		return Testing.displayMap(width, height, scale, level.getColors());
	}
	public void displayColorMap(@NotNull WorldManager world, boolean surface, boolean repeat, int scale) {
		Random rand = new Random();
		if(!repeat)
			displayColorMap(world, surface, rand.nextLong(), scale);
		else {
			boolean again = true;
			while(again)
				again = displayColorMap(world, surface, rand.nextLong(), scale);
		}
	}
}
