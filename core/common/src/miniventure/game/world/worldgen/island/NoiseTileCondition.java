package miniventure.game.world.worldgen.island;

import miniventure.game.util.function.MapFunction;

// used to compare tiles against a noise map.
// to 
public class NoiseTileCondition implements TileCondition {
	
	// for now, I'll try just defining this with a pre-made noise array, because I'm not sure how to handle a noise configuration considering they need to be initialized.
	/*
		maybe I need to have a set of pre-init classes and usable classes?
			- NoiseConfiguration is pre-init, and it needs width/height/seed to generate noise.
			- maybe, for any process that needs a noise map thing, it can be given a fetcher, and every time it starts a process on a map, it fetches the real class. So, instead of a TileCondition you give a PreTileCondition, which has a method to return a tile condition given a ProtoIsland. TileCondition itself could be a PreTileCondition which returns itself, so that TileConditions that don't need to be initialized don't have to worry about it.
		todo - Once again... I should probably wait until I come upon the exact usage of noise maps, and if/how/when they get used in the various island types, before bothering too hard with this.
	 */
	
	private float[][] noise;
	private final MapFunction<Float, Boolean> valueChecker;
	
	public NoiseTileCondition(float[][] noise, MapFunction<Float, Boolean> valueChecker) {
		this.noise = noise;
		this.valueChecker = valueChecker;
	}
	
	@Override
	public boolean isMatch(ProtoTile tile) {
		return valueChecker.get(tile.getVal(noise));
	}
}
