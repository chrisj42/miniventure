package miniventure.game.world.worldgen;

import miniventure.game.world.management.WorldManager;
import miniventure.game.world.worldgen.island.ProtoLevel;

import org.jetbrains.annotations.NotNull;

public interface LevelGenerator {
	
	void generateLevel(ProtoLevel level);
	
	default ProtoLevel generateLevel(@NotNull WorldManager world, int width, int height, long seed) {
		ProtoLevel level = new ProtoLevel(world, seed, width, height);
		generateLevel(level);
		return level;
	}
	
}
