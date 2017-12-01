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
	
	static float[][][][] generateSmoothNoise(float[][][][] baseNoiseMap, int width, int height, int octave) {
		// each 2D array in the 2D array is a chunk, stored by x,y coordinate.
		
		int chunksHori = baseNoiseMap.length;
		int chunksVert = 0;
		int chunkWidth = 0;
		int chunkHeight = 0;
		
		for(int cx = 0; cx < chunksHori; cx++) {
			chunksVert = baseNoiseMap[cx].length;
			for(int cy = 0; cy < chunksVert; cy++) {
				chunkWidth = baseNoiseMap[cx][cy].length;
				for(int tx = 0; tx < chunkWidth; tx++) {
					chunkHeight = baseNoiseMap[cx][cy][tx].length;
				}
			}
		}
		
		float[][] baseNoise = new float[chunksHori*chunkWidth][chunksVert*chunkHeight];
		
		for(int x = 0; x < baseNoise.length; x+=chunkWidth) {
			for(int y = 0; y < baseNoise[x].length; y+=chunkHeight) {
				float[][] baseChunk = baseNoiseMap[x/chunkWidth][y/chunkHeight];
				for(int lx = 0; lx < baseChunk.length; lx++)
					System.arraycopy(baseChunk[lx], 0, baseNoise[x+lx], y, baseChunk[lx].length);
			}
		}
		
		float[][] smoothNoise = generateSmoothNoise(baseNoise, width*chunkWidth, height*chunkHeight, octave);
		
		float[][][][] smoothNoiseMap = new float[chunksHori][chunksVert][chunkWidth][chunkHeight];
		
		//System.out.println("smooth noise dims: " + smoothNoise.length+"x"+smoothNoise[0].length);
		//System.out.println("smooth map dims: " + chunksHori+"x"+chunksVert+"x"+chunkWidth+"x"+chunkHeight);
		
		for(int x = 0; x < chunksHori; x++) {
			for(int y = 0; y < chunksVert; y++) {
				for(int lx = 0; lx < chunkWidth; lx++) {
					//System.out.println("accessing " + (x * chunkWidth + lx)+" of smooth noise, "+(y*chunkHeight)+" of smooth noise array: " + smoothNoise[x * chunkWidth + lx].length);
					//System.out.println(smoothNoiseMap[x][y][lx].length+" should be " + chunkHeight);
					for(int ly = 0; ly < chunkHeight; ly++) {
						//System.out.println("accessing "+x+"x"+y+"x"+lx+"x"+ly);
						smoothNoiseMap[x][y][lx][ly] = smoothNoise[x * chunkWidth + lx][y * chunkHeight + ly];
					}
					//System.arraycopy(smoothNoise[x * chunkWidth + lx], y * chunkHeight, smoothNoiseMap[x][y][lx], 0, chunkHeight);
				}
			}
		}
		
		return smoothNoiseMap;
	}
	static float[][] generateSmoothNoise(float[][] baseNoise, int octave) {
		return generateSmoothNoise(baseNoise, baseNoise.length, baseNoise.length==0?0:baseNoise[0].length, octave);
	}
	static float[][] generateSmoothNoise(float[][] baseNoise, int width, int height, int octave) {
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
				float top = interpolate(baseNoise[sample_x0][sample_y0], baseNoise[sample_x1][sample_y0], horizontal_blend);
				float bottom = interpolate(baseNoise[sample_x0][sample_y1], baseNoise[sample_x1][sample_y1], horizontal_blend);
				smoothNoise[x][y] = interpolate(top, bottom, vertical_blend);
			}
		}
		
		return smoothNoise;
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
	
	static float[][][][] generatePerlinNoise(float[][][][] baseNoiseMap, int width, int height, int octaveCount) {
		int chunksHori = baseNoiseMap.length;
		int chunksVert = 0;
		int chunkWidth = 0;
		int chunkHeight = 0;
		
		for(int cx = 0; cx < chunksHori; cx++) {
			chunksVert = baseNoiseMap[cx].length;
			for(int cy = 0; cy < chunksVert; cy++) {
				chunkWidth = baseNoiseMap[cx][cy].length;
				for(int tx = 0; tx < chunkWidth; tx++) {
					chunkHeight = baseNoiseMap[cx][cy][tx].length;
				}
			}
		}
		
		float[][] baseNoise = new float[chunksHori*chunkWidth][chunksVert*chunkHeight];
		
		for(int x = 0; x < baseNoise.length; x+=chunkWidth) {
			for(int y = 0; y < baseNoise[x].length; y+=chunkHeight) {
				float[][] baseChunk = baseNoiseMap[x/chunkWidth][y/chunkHeight];
				for(int lx = 0; lx < baseChunk.length; lx++)
					System.arraycopy(baseChunk[lx], 0, baseNoise[x+lx], y, baseChunk[lx].length);
			}
		}
		
		float[][] perlinNoise = generatePerlinNoise(baseNoise, width*chunkWidth, height*chunkHeight, octaveCount);
		
		float[][][][] perlinNoiseMap = new float[chunksHori][chunksVert][chunkWidth][chunkHeight];
		
		//System.out.println("smooth noise dims: " + smoothNoise.length+"x"+smoothNoise[0].length);
		//System.out.println("smooth map dims: " + chunksHori+"x"+chunksVert+"x"+chunkWidth+"x"+chunkHeight);
		
		for(int x = 0; x < chunksHori; x++) {
			for(int y = 0; y < chunksVert; y++) {
				for(int lx = 0; lx < chunkWidth; lx++) {
					for(int ly = 0; ly < chunkHeight; ly++) {
						perlinNoiseMap[x][y][lx][ly] = perlinNoise[x * chunkWidth + lx][y * chunkHeight + ly];
					}
				}
			}
		}
		
		return perlinNoiseMap;
	}
	static float[][] generatePerlinNoise(float[][] baseNoise, int octaveCount) {
		return generatePerlinNoise(baseNoise, baseNoise.length, baseNoise.length==0?0:baseNoise[0].length, octaveCount);
	}
	static float[][] generatePerlinNoise(float[][] baseNoise, int width, int height, int octaveCount) {
		float[][][] smoothNoise = new float[octaveCount][][]; // an array of 2D arrays containing smoothed instances of baseNoise.
		float persistance = 0.7f; // amplitude, "importance" of the noise.
		
		for (int i = 0; i < octaveCount; i++) {
			smoothNoise[i] = generateSmoothNoise(baseNoise, width, height, i);
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
