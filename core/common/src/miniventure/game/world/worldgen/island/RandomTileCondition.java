package miniventure.game.world.worldgen.island;

import java.util.Random;

import miniventure.game.util.function.MapFunction;

public class RandomTileCondition implements TileCondition {
	
	private final long seed;
	private final MapFunction<Float, Boolean> valueChecker;
	private final Random rand;
	
	public RandomTileCondition(long seed, MapFunction<Float, Boolean> valueChecker) {
		this.seed = seed;
		this.valueChecker = valueChecker;
		rand = new Random();
	}
	
	@Override
	public boolean isMatch(ProtoTile tile) {
		rand.setSeed(seed * (1+tile.id));
		return valueChecker.get(rand.nextFloat());
	}
}
