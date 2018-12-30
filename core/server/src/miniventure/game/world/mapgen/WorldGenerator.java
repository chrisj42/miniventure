package miniventure.game.world.mapgen;

import java.util.Random;

import miniventure.game.world.levelgen.LevelGenerator;

public class WorldGenerator {
	
	private final long seed;
	private final Random random;
	private final int levelWidth;
	private final int levelHeight;
	
	public WorldGenerator(long seed, int levelWidth, int levelHeight) {
		this.seed = seed;
		random = new Random(seed);
		this.levelWidth = levelWidth;
		this.levelHeight = levelHeight;
	}
	
	public LevelGenerator getLevelGenerator(int levelId) {
		random.setSeed(seed+levelId);
		return new LevelGenerator(random.nextLong(), levelWidth, levelHeight);
	}
	
}
