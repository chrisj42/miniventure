package miniventure.game.world.worldgen.island;

import miniventure.game.world.management.DisplayWorld;
import miniventure.game.world.management.WorldManager;

public class TestIslandGen {
	
	public static void main(String[] args) {
		final WorldManager world = new DisplayWorld(false);
		
		final int scale = 2;
		// IslandType.SWAMP.displayColorMap(true, true, scale);
		// IslandType.WOODLAND.displayColorMap(false, true, scale);
		// IslandType.JUNGLE.displayColorMap(true, true, scale);
		IslandType.DESERT.displayColorMap(world, true, true, scale);
		// IslandType.ARCTIC.displayColorMap(true, true, scale);
		
	}
	
}
