package miniventure.game.world.worldgen;

import miniventure.game.world.worldgen.island.ProtoLevel;

public interface LevelGenerator {
	
	void generateLevel(ProtoLevel level);
	
	default ProtoLevel generateLevel(int width, int height, long seed) {
		ProtoLevel level = new ProtoLevel(seed, width, height);
		generateLevel(level);
		return level;
	}
	
}
