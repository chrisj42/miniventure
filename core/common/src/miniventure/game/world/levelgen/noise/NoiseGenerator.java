package miniventure.game.world.levelgen.noise;

import java.util.Arrays;

@FunctionalInterface
public interface NoiseGenerator {
	
	float[][] get2DNoise(long seed, int width, int height);
	
	NoiseGenerator BLANK = (seed, width, height) -> {
		float[][] values = new float[width][height];
		for(int x = 0; x < values.length; x++)
			Arrays.fill(values[x], 1f);
		return values;
	};
	
	// NoiseGenerator RADIAL_DISTANCE = islandMask(1);
	
	// value given to modifier is 1 if at max value, 0 at minimum value.
	static NoiseGenerator islandMask(float dropOffSpeed) {
		return (seed, width, height) -> {
			final float maxDist = Math.min(width/2f, height/2f);//(float) Math.hypot(width/2f, height/2f);
			
			float[][] values = new float[width][height];
			
			for(int x = 0; x < values.length; x++) {
				for(int y = 0; y < values[x].length; y++) {
					float xd = Math.abs(x-width/2f);
					float yd = Math.abs(y-height/2f);
					float dist = (float) Math.hypot(xd, yd);
					values[x][y] = 1 - (float) Math.pow(dist/maxDist, dropOffSpeed);
				}
			}
			
			return values;
		};
	}
}
