package miniventure.game.world.worldgen.level;

import miniventure.game.world.worldgen.level.processing.NoiseTileCondition;
import miniventure.game.world.worldgen.level.processing.TileNoiseMap;
import miniventure.game.world.worldgen.level.processing.TileProcessor;
import miniventure.game.world.worldgen.noise.Coherent2DNoiseFunction;
import miniventure.game.world.worldgen.noise.Noise;
import miniventure.game.world.worldgen.noise.NoiseGenerator;

import static miniventure.game.world.tile.TileTypeEnum.DIRT;
import static miniventure.game.world.tile.TileTypeEnum.GRASS;
import static miniventure.game.world.tile.TileTypeEnum.POOF_TREE;
import static miniventure.game.world.tile.TileTypeEnum.SAND;
import static miniventure.game.world.tile.TileTypeEnum.STONE;
import static miniventure.game.world.tile.TileTypeEnum.WATER;
import static miniventure.game.world.worldgen.noise.NoiseModifier.FILL_VALUE_RANGE;
import static miniventure.game.world.worldgen.noise.NoiseModifier.NoiseValueMerger.MULTIPLY;
import static miniventure.game.world.worldgen.noise.NoiseModifier.combine;

public interface LevelGenerator extends MapGenerator {
	
	default ProtoLevel generateLevel(long seed) {
		ProtoLevel map = new ProtoLevel(seed, getWidth(), getHeight());
		generateLevel(map);
		return map;
	}
	
	int getWidth();
	int getHeight();
	
	abstract class AbstractLevelGenerator implements LevelGenerator {
		
		private final int width;
		private final int height;
		
		public AbstractLevelGenerator() { this(400, 400); } // default island size
		public AbstractLevelGenerator(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		@Override
		public int getWidth() { return width; }
		@Override
		public int getHeight() { return height; }
		
	}
	
	LevelGenerator MENU = new AbstractLevelGenerator(100, 100) {
		@Override
		public void generateLevel(ProtoLevel protoLevel) {
			float[][] terrain = protoLevel.getFromGen(
					new Coherent2DNoiseFunction(36, 3).modify(
							combine(new Noise(new int[] {1,32,8,2,4,16}, new int[] {4,2,1})),
							FILL_VALUE_RANGE,
							combine(NoiseGenerator.circleMask(1), MULTIPLY)
					)
			);
			
			float[][] features = protoLevel.getFromGen(
					new Coherent2DNoiseFunction(12, 2)
							.modify(FILL_VALUE_RANGE)
			);
			
			TileProcessor tileMapper = TileNoiseMap.builder()
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
			
			protoLevel.forEach(tileMapper);
		}
	};
}
