package miniventure.game.world.worldgen.noise;

import java.util.Arrays;

import miniventure.game.util.function.MapFunction;

// this interface creates noise maps out of nothing but size and seed.
// NoiseGenerator factory methods will consist of patterns that have no prior value.
@FunctionalInterface
public interface NoiseGenerator extends ValueSetFetcher {
	
	float[][] get2DNoise(long seed, int width, int height);
	
	// seed changes really only matter when you use the same map multiple times. The same seed used on different algorithms cannot be directly said to cause artifacts not seen by any other static association of two seeds. Hence, the need for seed changes is heavily tied to the nature of the generator.
	// Noise instances, that start with a map of white noise and then smooth it, are an example of a generator for which having the same seed causes a noticeable effect. Let's test that...
	default NoiseGenerator modifySeed(MapFunction<Long, Long> seedModifier) {
		return (seed, width, height) -> get2DNoise(seedModifier.get(seed), width, height);
	}
	
	default NoiseGenerator modify(NoiseModifier... modifiers) {
		return (seed, width, height) -> {
			float[][] noise = get2DNoise(seed, width, height);
			for(NoiseModifier mod: modifiers)
				mod.modify(seed, noise);
			return noise;
		};
	}
	
	@Override
	default ValueFetcher get(long seed, int width, int height) {
		float[][] noise = get2DNoise(seed, width, height);
		return (x, y) -> noise[x][y];
	}
	
	NoiseGenerator BLANK = (seed, width, height) -> {
		float[][] values = new float[width][height];
		for(int x = 0; x < values.length; x++)
			Arrays.fill(values[x], 1f);
		return values;
	};
	
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
					values[x][y] = 1 - (float) Math.pow(Math.min(1, dist/maxDist), dropOffSpeed);
				}
			}
			
			return values;
		};
	}
}
