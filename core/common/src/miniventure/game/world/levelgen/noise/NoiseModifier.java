package miniventure.game.world.levelgen.noise;

import miniventure.game.util.ArrayUtils;

@FunctionalInterface
public interface NoiseModifier {
	
	void modify(long seed, float[][] noise);
	
	
	NoiseModifier FILL_VALUE_RANGE = (seed, noise) -> ArrayUtils.deepMapArray(noise, 0, 1);
	
	
	static NoiseModifier combine(NoiseGenerator generator, float genWeight) {
		return forGenerator(generator, (origNoise, newNoise, x, y) -> {
			float result = origNoise + newNoise * genWeight;
			result /= genWeight + 1;
			return result;
		});
	}
	
	static NoiseModifier multiply(NoiseGenerator generator) {
		return forGenerator(generator, (origNoise, newNoise, x, y) -> origNoise * newNoise);
	}
	
	
	@FunctionalInterface
	interface ValueFetcher {
		float get(float noise, int x, int y);
	}
	
	static NoiseModifier forEach(ValueFetcher valueFunction) {
		return (seed, noise) -> {
			for(int x = 0; x < noise.length; x++)
				for(int y = 0; y < noise[x].length; y++)
					noise[x][y] = valueFunction.get(noise[x][y], x, y);
		};
	}
	
	
	@FunctionalInterface
	interface ModifierMaker {
		NoiseModifier init(long seed, int width, int height);
	}
	
	static NoiseModifier constructLater(ModifierMaker modMaker) {
		return (seed, noise) -> modMaker.init(seed, noise.length, noise[0].length).modify(seed, noise);
	}
	
	
	@FunctionalInterface
	interface MatchValueFetcher {
		float get(float origNoise, float newNoise, int x, int y);
	}
	
	static NoiseModifier forGenerator(NoiseGenerator generator, MatchValueFetcher valueFunction) {
		return constructLater((seed, width, height) -> {
			float[][] values = generator.get2DNoise(seed+10, width, height);
			return forEach((noise, x, y) -> valueFunction.get(noise, values[x][y], width, height));
		});
	}
}
