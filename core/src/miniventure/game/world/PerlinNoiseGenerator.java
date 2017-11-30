package miniventure.game.world;

import java.util.Random;

import net.openhft.hashing.LongHashFunction;

class PerlinNoiseGenerator {
	
	private static final Random random = new Random();
	
	static float[][] generateWhiteNoise(long seed, int width, int height) {
		random.setSeed(seed);
		return generateWhiteNoise(width, height);
	}
	private static float[][] generateWhiteNoise(int width, int height) {
		float[][] noise = new float[width][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				noise[x][y] = random.nextFloat();
			}
		}
		return noise;
	}
	
	// think of a range 0 to 1. This is mapped to the range x0 to x1. Alpha is on the 0-1 range, and the mapped value for alpha is what is returned. So if alpha is 0.5, this method returns the average of x0 and x1.
	static float interpolate(float x0, float x1, float alpha) {
		return x0 * (1 - alpha) + alpha * x1;
	}
	
	static float[][] generateSmoothNoise(LongHashFunction hashFunction, int sx, int sy, int width, int height, int octave) {
		float[][] smoothNoise = new float[width][height];
		
		int samplePeriod = 1 << octave; // calculates 2 ^ octave
		float sampleFrequency = 1.0f / samplePeriod;
		for (int x = 0; x < width; x++) {
			int sample_x0 = (x / samplePeriod) * samplePeriod; // this gets the greatest multiple of samplePeriod <= x.
			int sample_x1 = (sample_x0 + samplePeriod) % width; // wrap around
			float horizontal_blend = (x - sample_x0) * sampleFrequency;
			
			for (int y = 0; y < height; y++) {
				int sample_y0 = (y / samplePeriod) * samplePeriod;
				int sample_y1 = (sample_y0 + samplePeriod) % height; // wrap around
				float vertical_blend = (y - sample_y0) * sampleFrequency;
				float top = interpolate(getHash(hashFunction, sx+sample_x0, sy+sample_y0), getHash(hashFunction, sx+sample_x1, sy+sample_y0), horizontal_blend);
				float bottom = interpolate(getHash(hashFunction, sx+sample_x0, sy+sample_y1), getHash(hashFunction, sx+sample_x1, sy+sample_y1), horizontal_blend);
				smoothNoise[x][y] = interpolate(top, bottom, vertical_blend);
			}
		}
		
		return smoothNoise;
	}
	
	private static float getHash(LongHashFunction hashFunction, int n1, int n2) {
		long hash = hashFunction.hashInts(new int[] {n1, n2});
		return new Random(hash).nextFloat();
		/*double asDouble = (double) hash;
		asDouble /= 2;
		float floatVal = (float) asDouble;
		floatVal = Math.abs(floatVal);
		return floatVal / Float.MAX_VALUE;
		//return map(floatVal, 0, Float.MAX_VALUE, 0, 1);*/
	}
	
	public static float map(float num, float prevMin, float prevMax, float newMin, float newMax) {
		return (num-prevMin)/(prevMax-prevMin) * (newMax-newMin) + newMin;
	}
	
	/*static float[][] generatePerlinNoise(float[][] baseNoise, int octaveCount) {
		
		int width = baseNoise.length;
		int height = baseNoise[0].length;
		
	}*/
	static float[][] generatePerlinNoise(LongHashFunction hashFunction, int x, int y, int width, int height, int octaveCount) {
		float[][][] smoothNoise = new float[octaveCount][][]; // an array of 2D arrays containing smoothed instances of baseNoise.
		float persistance = 0.7f; // amplitude, "importance" of the noise.
		
		for (int i = 0; i < octaveCount; i++) {
			smoothNoise[i] = generateSmoothNoise(hashFunction, x, y, width, height, i);
		}
		
		float[][] perlinNoise = new float[width][height]; // an array of floats initialised to 0
		
		float amplitude = 1.0f;
		float totalAmplitude = 0.0f;
		
		for (int octave = octaveCount - 1; octave >= 0; octave--) {
			amplitude *= persistance;
			totalAmplitude += amplitude;
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
				}
			}
		}
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				perlinNoise[i][j] /= totalAmplitude;
			}
		}
		
		return perlinNoise;
	}
}
