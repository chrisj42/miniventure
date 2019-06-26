package miniventure.game.world.worldgen.noise;

import java.util.Random;

public class GenInfo {
	// public final long seed;
	public final int width;
	public final int height;
	
	private final Random random;
	
	public GenInfo(long seed, int width, int height) {
		// this.seed = seed;
		this.width = width;
		this.height = height;
		random = new Random(seed);
	}
	
	// public GenInfo(GenInfo info) { this(info.seed, info.width, info.height); }
	
	long nextSeed() { return random.nextLong(); }
}
