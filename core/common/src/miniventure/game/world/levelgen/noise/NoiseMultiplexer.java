package miniventure.game.world.levelgen.noise;

public class NoiseMultiplexer implements NoiseGenerator {
	
	// this class takes a bunch of noise map configurations, and combines them together in certain ways to get a final map as a result.
	// methods like multiply, map, average, all relating to parameter sets to include
	
	// the modifiers in each NoiseConfiguration are like tweaking each noise map sampling; this is where we can merge noise maps together like octaves.
	
	// todo somehow combine configuration and its selected merge strat
	interface NoiseMergeStrategy {
		float[][] merge(float[][] newNoise);
	}
	
	public NoiseMultiplexer() {
		
	}
	
	@Override
	public float[][] get2DNoise(long seed, int width, int height) {
		return new float[0][];
	}
}
