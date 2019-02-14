package miniventure.game.world.worldgen.noise;

public class NoiseMap {
	
	/*
		this should attempt to coordinate a noise config with a tile source;
		this should take coordinates, determine the noise value, and pass it to the tile map to get a tile.
		
		A TileNoiseAssociationMap... takes a list of noise configs, then a list of tile maps and the noise config they should use.
		
		TileMap references need to have an associated noise map, even though the TileMap instance itself doesn't.
		
	 */
	
	
	// private NoiseConfiguration config;
	private float[][] cache;
	
	// public NoiseMap(NoiseConfiguration config) {}
	
}
