package miniventure.game.world.worldgen;

import miniventure.game.GameCore;

public abstract class WorldConfig {
	
	// used to pass parameters necessary for creating/loading a world.
	
	public final String worldname;
	
	WorldConfig(String worldname) {
		this.worldname = worldname;
	}
	
	public static class CreationConfig extends WorldConfig {
		public final int width;
		public final int height;
		public final long seed;
		
		public CreationConfig(String worldname, int width, int height, long seed) {
			super(worldname);
			this.width = width == 0 ? GameCore.DEFAULT_WORLD_SIZE : width;
			this.height = height == 0 ? GameCore.DEFAULT_WORLD_SIZE : height;
			this.seed = seed;
		}
	}
	
	public static class LoadConfig extends WorldConfig {
		public LoadConfig(String worldname) {
			super(worldname);
		}
	}
	
}
