package miniventure.game.world.worldgen;

import java.util.Random;

import miniventure.game.world.Point;

public class ProtoIsland {
	
	// getting replaced by IslandReference entirely.
	
	// I think "level stub" will become the main class; it will have a method to get a LevelGenerator for it. It will also contain a static method to generate a map of LevelStubs.
	
	private class LevelStub {
		Point position;
		LevelGenerator generator;
		
		private LevelStub(Point position, LevelGenerator generator) {
			this.position = position;
			this.generator = generator;
		}
	}
	
	private final long seed;
	private final Random random;
	private final int levelWidth;
	private final int levelHeight;
	
	private final int mapWidth;
	private final int mapHeight;
	private final LevelStub[] levelStubs;
	
	public ProtoIsland(long seed, int levelWidth, int levelHeight) {
		this.seed = seed;
		random = new Random(seed);
		this.levelWidth = levelWidth;
		this.levelHeight = levelHeight;
		
		mapWidth = mapHeight = 10;
		levelStubs = generateMap();
	}
	
	public Point[] getLevelPositions() {
		Point[] positions = new Point[levelStubs.length];
		for(int i = 0; i < positions.length; i++) {
			positions[i] = levelStubs[i].position;
		}
		return positions;
	}
	
	public LevelGenerator[] getLevelGenerators() {
		LevelGenerator[] generators = new LevelGenerator[levelStubs.length];
		for(int i = 0; i < generators.length; i++) {
			generators[i] = levelStubs[i].generator;
		}
		return generators;
	}
	
	public LevelGenerator getLevelGenerator(int levelId) {
		return levelStubs[levelId].generator;
	}
	
	public long getSeed() {
		return seed;
	}
	
	public int getMapWidth() {
		return mapWidth;
	}
	
	public int getMapHeight() {
		return mapHeight;
	}
	
	private LevelStub[] generateMap() {
		LevelStub[] stubs = new LevelStub[3];
		
		stubs[0] = new LevelStub(new Point(0, 0), new LevelGenerator(random.nextLong(), levelWidth, levelHeight));
		stubs[1] = new LevelStub(new Point(2, 0), new LevelGenerator(random.nextLong(), levelWidth, levelHeight));
		stubs[2] = new LevelStub(new Point(0, -2), new LevelGenerator(random.nextLong(), levelWidth, levelHeight));
		
		return stubs;
	}
}
