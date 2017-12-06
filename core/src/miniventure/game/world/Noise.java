package miniventure.game.world;

import java.util.Arrays;
import java.util.Random;

class Noise {
	static float[] getWhiteNoise(long seed, int length) {
		float[] noise = new float[length];
		Random random = new Random(new Random(seed).nextLong());
		for(int i = 0; i < noise.length; i++)
			noise[i] = random.nextFloat();
		
		return noise;
	}
	
	/// returns a number between n1 and n2, depending on relPos; relPos=0 returns n1, relPos=1 returns n2.
	private static float interpolate(float n1, float n2, float relPos) { return n1*(1-relPos) + n2*relPos; }
	
	/// this is like the above, but before interpolating, it biases the measurement.
	private static float interpolate(float n1, float n2, float relPos, float[] weights, float min, float max) {
		n1 -= min;
		n2 -= min;
		float sectionSize = (max-min) / weights.length;
		int wi1 = (int)(n1/sectionSize);
		int wi2 = (int)(n2/sectionSize);
		if(wi1 >= weights.length) wi1 = weights.length-1;
		if(wi2 >= weights.length) wi2 = weights.length-1;
		if(wi1 < 0) wi1 = 0;
		if(wi2 < 0) wi2 = 0;
		float w1 = 1 - weights[wi1]; // to transform the format, b/c 1 points to n1 instead of n2.
		float w2 = weights[wi2]; // already matches format.
		// the weights and relPos can now simply be averaged.
		relPos = (w1 + w2 + relPos) / 3;
		
		return interpolate(n1, n2, relPos);
	}
	
	/// smooths via linear interpolation. The sample period is the number of values to skip in each direction per sample. 
	static float[] smoothNoise2D(float[] noise, int width, int height, int samplePeriod) {
		return smoothNoise2D(noise, width, height, new float[0], samplePeriod);
	}
	static float[] smoothNoise2D(float[] noise, int width, int height, float[] weights, int samplePeriod) {
		float[] smoothNoise = new float[noise.length];
		if(smoothNoise.length == 0) return smoothNoise;
		
		// For each coordinate direction, I need to find the coordinate's relative position between the two closest multiples of the period.
		// I then determine the number in the very same relative position between the two noise values that the coordinates point to, and store them into the smoothNoise array.
		
		// But first, in order to calculate weights, find the lowest and highest values of the noise array. Then, divide the range by the number of weights. This is the size of each section. Divide a number by this to get its weight index.
		
		if(weights.length == 0) weights = new float[] {1};
		
		float[] extrema = getMinMax(noise);
		float min = extrema[0], max = extrema[1];
		
		for(int x = 0; x < width; x++) {
			int xWaveStart = (x / samplePeriod) * samplePeriod; // find lower multiple of period, the beginning of this cycle of the "wave".
			int xWaveEnd = (xWaveStart + samplePeriod) % width; // get upper multiple, the end of the current wave. Have to add modulo width b/c the wave period might be longer than the width.
			
			float xRelPos = (x - xWaveStart) / (float)samplePeriod; // gets relpos, with cast to make sure it's allowed to be a decimal.
			
			for(int y = 0; y < height; y++) {
				int yWaveStart = (y / samplePeriod) * samplePeriod;
				int yWaveEnd = (yWaveStart + samplePeriod) % height;
				
				float yRelPos = (y - yWaveStart) / (float)samplePeriod;
				
				// now, interpolate the relative positions for the associated noise values. 
				float topRel = interpolate(noise[xWaveStart*height + yWaveStart], noise[xWaveEnd*height + yWaveStart], xRelPos, weights, min, max); // using noise values for the horizontal wave, at the start of the vertical wave
				float bottomRel = interpolate(noise[xWaveStart*height + yWaveEnd], noise[xWaveEnd*height + yWaveEnd], xRelPos, weights, min, max); // using noise values for the horizontal wave, at the end of the vertical wave
				float totalRel = interpolate(topRel, bottomRel, yRelPos, weights, min, max); // using vertical wave values at the correct horizontal positions to get overall relative wave position.
				
				smoothNoise[x*height + y] = totalRel; // save the value.
			}
		}
		
		return smoothNoise;
	}
	
	/// basically just calls the above method repeatedly with the given samplePeriods, and returns the results in a 2D array.
	static float[][] smoothNoise2D(float[] noise, int width, int height, int... samplePeriods) {
		return smoothNoise2D(noise, width, height, new float[0], samplePeriods);
	}
	static float[][] smoothNoise2D(float[] noise, int width, int height, float[] weights, int... samplePeriods) {
		float[][] smoothedNoise = new float[samplePeriods.length][noise.length];
		for(int i = 0; i < smoothedNoise.length; i++)
			smoothedNoise[i] = smoothNoise2D(noise, width, height, weights, samplePeriods[i]);
		
		return smoothedNoise;
	}
	
	/// same as above, but instead of returning the results separately, it compounds each smoothing onto the same array.
	static float[] smoothNoise2DProgressive(float[] noise, int width, int height, int... samplePeriods) {
		return smoothNoise2DProgressive(noise, width, height, new float[0], samplePeriods);
	}
	static float[] smoothNoise2DProgressive(float[] noise, int width, int height, float[] weights, int... samplePeriods) {
		float[] smoothedNoise = new float[noise.length];
		System.arraycopy(noise, 0, smoothedNoise, 0, noise.length);
		for(int i = 0; i < samplePeriods.length; i++)
			smoothedNoise = smoothNoise2D(smoothedNoise, width, height, weights, samplePeriods[i]);
		
		return smoothedNoise;
	}
	
	/// adds up the noise values from each array in parallel, giving each one a certain weight, stored in the weights array.
	static float[] addNoiseWeighted(float[][] noiseMaps) {
		float[] weights = new float[noiseMaps.length];
		Arrays.fill(weights, 1f);
		return addNoiseWeighted(noiseMaps, weights);
	}
	static float[] addNoiseWeighted(float[][] noiseMaps, float... weights) {
		if(noiseMaps.length != weights.length)
			throw new IllegalArgumentException("weights array and noise function array must have the same length.");
		
		if(noiseMaps.length == 0) return new float[0];
		
		float[] weightedNoise = new float[noiseMaps[0].length];
		
		float totalWeight = 0; // all the weight applied to each value; will divide all values by this afterwards to ensure they are still in the same range.
		
		for(int i = 0; i < noiseMaps.length; i++) {
			float weight = weights[i];
			totalWeight += weight; // only one per entire array, b/c each sum in the sum array only gets added to once by each noise array. 
			for(int j = 0; j < noiseMaps[i].length; j++)
				weightedNoise[j] += weight * noiseMaps[i][j];
		}
		
		// now divide all by the total noise
		for(int i = 0; i < weightedNoise.length; i++)
			weightedNoise[i] /= totalWeight;
		
		return weightedNoise;
	}
	
	/// moves values in a certain range toward a given value through multiplication.
	static float[] filterNoiseWeighted(float[] noise, float... targets) {
		float[] extrema = getMinMax(noise);
		float sectionSize = (extrema[1] - extrema[0]) / targets.length;
		
		float[] filteredNoise = new float[noise.length];
		
		for(int i = 0; i < noise.length; i++) {
			float val = noise[i];
			val -= extrema[0];
			int weightIdx = (int) (val / sectionSize);
			if (weightIdx >= targets.length) weightIdx--;
			float weight = targets[weightIdx];
			filteredNoise[i] = interpolate(noise[i], weight, Math.abs(noise[i]-weight));
		}
		
		return filteredNoise;
	}
	
	static float map(float num, float prevMin, float prevMax, float newMin, float newMax) {
		return (num-prevMin)/(prevMax-prevMin) * (newMax-newMin) + newMin;
	}
	
	static float[] map(float[] nums, float newMin, float newMax) {
		float[] extrema = getMinMax(nums);
		float min = extrema[0], max = extrema[1];
		
		float[] newNums = new float[nums.length];
		for(int i = 0; i < nums.length; i++)
			newNums[i] = map(nums[i], min, max, newMin, newMax);
		
		return newNums;
	}
	
	
	private static float[] getMinMax(float[] values) {
		if(values.length == 0) return new float[] {0, 0};
		float min = values[0], max = values[0];
		for(int i = 1; i < values.length; i++) {
			min = Math.min(min, values[i]);
			max = Math.max(max, values[i]);
		}
		
		return new float[] {min, max};
	}
}
