package miniventure.game.world.levelgen;

import java.util.LinkedList;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

public class NoiseMap {
	
	@FunctionalInterface
	interface WeightFetcher {
		float getWeight(float value, int x, int y);
	}
	
	private static final Random random = new Random(new Random().nextLong());
	
	@Nullable
	private final Long seed;
	private final int width;
	private final int height;
	private final int[] samplePeriods;
	private final int[] postSmoothing;
	private final boolean fillBefore;
	private final boolean fillAfter;
	
	private final LinkedList<WeightFetcher> modifiers = new LinkedList<>();
	
	public NoiseMap(int width, int height, int[] samplePeriods, int[] postSmoothing, boolean fillBefore, boolean fillAfter) {
		this(null, width, height, samplePeriods, postSmoothing, fillBefore, fillAfter);
	}
	public NoiseMap(@Nullable Long seed, int width, int height, int[] samplePeriods, int[] postSmoothing, boolean fillBefore, boolean fillAfter) {
		this.seed = seed;
		this.width = width;
		this.height = height;
		this.samplePeriods = samplePeriods;
		this.postSmoothing = postSmoothing;
		this.fillBefore = fillBefore;
		this.fillAfter = fillAfter;
	}
	
	public void multiply(WeightFetcher weightFetcher) {
		modifiers.add(weightFetcher);
	}
	
	public float[][] getNoise() {
		float[] noise = Noise.getWhiteNoise(seed == null ? random.nextLong() : seed, width*height);
		float[][] noises = Noise.smoothNoise2D(noise, width, height, samplePeriods);
		float[] smoothNoise = Noise.addNoiseWeighted(noises, true);
		smoothNoise = Noise.smoothNoise2DProgressive(smoothNoise, width, height, postSmoothing);
		
		if(fillBefore)
			smoothNoise = Noise.map(smoothNoise, 0, 1);
		
		for(WeightFetcher fetcher: modifiers)
			for(int i = 0; i < smoothNoise.length; i++)
				smoothNoise[i] *= fetcher.getWeight(smoothNoise[i], i / height, i % height);
		
		if(fillAfter)
			smoothNoise = Noise.map(smoothNoise, 0, 1);
		
		float[][] smooth2D = new float[width][height];
		for(int i = 0; i < smoothNoise.length; i++)
			smooth2D[i/height][i%height] = smoothNoise[i];
		
		return smooth2D;
	}
}
