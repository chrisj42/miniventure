package miniventure.game.world.worldgen;

public class WorldConfig {
	
	// used to pass parameters necessary for creating/loading a world.
	
	public final String worldname;
	public final long seed;
	
	WorldConfig(String worldname, long seed) {
		this.worldname = worldname;
		this.seed = seed;
	}
	
}
