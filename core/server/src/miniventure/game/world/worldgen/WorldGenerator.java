package miniventure.game.world.worldgen;

import java.util.Random;

import miniventure.game.world.SaveLoadInterface;

public class WorldGenerator {
	
	/*
		Generates a world, saving it to file. All worlds are loaded from file when starting actual play.
	 */
	
	public static void generateWorld(final SaveLoadInterface file) { generateWorld(file, new Random().nextLong()); }
	public static void generateWorld(final SaveLoadInterface file, final long seed) {
		// world file can throw exception when acquiring lock: if lock cannot be acquired.
		
	}
	
}
