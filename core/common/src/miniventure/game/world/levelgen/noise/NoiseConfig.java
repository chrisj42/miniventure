package miniventure.game.world.levelgen.noise;

import java.util.LinkedList;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

public class NoiseConfig {
	
	private interface NoiseModifier {
		void modify(float[] values, int width, int height, long seed);
	}
	
	@FunctionalInterface
	interface WeightFetcher extends NoiseModifier {
		float getWeight(float value, int x, int y);
		
		@Override
		default void modify(float[] values, int width, int height, long seed) {
			for(int i = 0; i < values.length; i++)
				values[i] *= getWeight(values[i], i/height, i%height);
		}
	}
	
	private static final Random random = new Random(new Random().nextLong());
	
	@Nullable
	private final Long seed;
	private final int width;
	private final int height;
	private final NoiseGenerator noiseGen;
	private final boolean fillBefore;
	private final boolean fillAfter;
	
	private final LinkedList<NoiseModifier> modifiers = new LinkedList<>();
	
	public NoiseConfig(int width, int height, NoiseGenerator noiseGen, boolean fillBefore, boolean fillAfter) {
		this(null, width, height, noiseGen, fillBefore, fillAfter);
	}
	public NoiseConfig(@Nullable Long seed, int width, int height, NoiseGenerator noiseGen, boolean fillBefore, boolean fillAfter) {
		this.seed = seed;
		this.width = width;
		this.height = height;
		this.noiseGen = noiseGen;
		this.fillBefore = fillBefore;
		this.fillAfter = fillAfter;
	}
	
	public void multiply(WeightFetcher weightFetcher) {
		modifiers.add(weightFetcher);
	}
	
	public void multiply(NoiseGenerator noiseGenerator) {
		modifiers.add((values, width, height, seed) -> {
			float[][] otherVals = noiseGenerator.get2DNoise(seed, width, height);
			// for(int i = 0; i < values.length; i++)
			// 	values[i] = (values[i]+otherVals[i])/2;
			
			float[] newvals = Noise.map(values, 0, 1);
			System.arraycopy(newvals, 0, values, 0, values.length);
		});
	}
	
	public void multiply(NoiseConfig other) {
		modifiers.add((values, width, height, seed) -> {
			float[][] otherVals = other.getNoise(seed+1, width, height);
			for(int i = 0; i < values.length; i++)
				values[i] *= otherVals[i/height][i%height];
		});
	}
	
	// given array is expected to match size of current array
	public void multiply(float[][] values) {
		multiply((WeightFetcher) (value, x, y) -> values[x][y]);
	}
	
	public float[][] getNoise() { return getNoise(this.seed == null ? random.nextLong() : this.seed, width, height); }
	private float[][] getNoise(final long seed, final int width, final int height) {
		float[] smoothNoise = new float[0];//noiseGen.get2DNoise(seed, width, height);
		
		if(fillBefore)
			smoothNoise = Noise.map(smoothNoise, 0, 1);
		
		for(NoiseModifier mod: modifiers)
			mod.modify(smoothNoise, width, height, seed);
		
		if(fillAfter)
			smoothNoise = Noise.map(smoothNoise, 0, 1);
		
		float[][] smooth2D = new float[width][height];
		for(int i = 0; i < smoothNoise.length; i++)
			smooth2D[i/height][i%height] = smoothNoise[i];
		
		return smooth2D;
	}
}
