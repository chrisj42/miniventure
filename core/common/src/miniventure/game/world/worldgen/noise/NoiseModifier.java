package miniventure.game.world.worldgen.noise;

import miniventure.game.util.ArrayUtils;
import miniventure.game.world.worldgen.noise.ValueSetFetcher.ValueFetcher;

// for noise patterns that alter existing noise.
// direct instances usually alter the noise according to some predetermined pattern; the seed should not be necessary.
@FunctionalInterface
public interface NoiseModifier {
	
	void modify(long seed, float[][] noise);
	
	NoiseModifier FILL_VALUE_RANGE = (seed, noise) -> ArrayUtils.deepMapArray(noise, 0, 1);
	
	@FunctionalInterface
	interface NoiseValueMapper {
		float get(float noise, int x, int y);
	}
	
	static NoiseModifier forEach(NoiseValueMapper valueFunction) {
		return (seed, noise) -> {
			for(int x = 0; x < noise.length; x++)
				for(int y = 0; y < noise[x].length; y++)
					noise[x][y] = valueFunction.get(noise[x][y], x, y);
		};
	}
	
	interface NoiseValueMerger {
		float get(float noise1, float noise2);
		
		NoiseValueMerger AVERAGE = (noise1, noise2) -> (noise1 + noise2) / 2;
		NoiseValueMerger MULTIPLY = (noise1, noise2) -> noise1 * noise2;
		NoiseValueMerger IGNORE = (noise1, noise2) -> noise1;
		NoiseValueMerger OVERWRITE = (noise1, noise2) -> noise2;
	}
	
	// delta weight value of 1 means the change is done completely; a value of 0 means it is not done at all.
	static NoiseModifier combine(ValueSetFetcher generator, NoiseValueMerger valueMapper) {
		return combine(generator, valueMapper, 1);
	}
	static NoiseModifier combine(ValueSetFetcher generator, NoiseValueMerger valueMapper, float deltaWeight) {
		return combine(generator, valueMapper, ValueSetFetcher.get(deltaWeight));
	}
	static NoiseModifier combine(ValueSetFetcher generator, float deltaWeight) {
		return combine(generator, ValueSetFetcher.get(deltaWeight));
	}
	static NoiseModifier combine(ValueSetFetcher generator, ValueSetFetcher deltaWeight) {
		return combine(generator, NoiseValueMerger.OVERWRITE, deltaWeight);
	}
	static NoiseModifier combine(ValueSetFetcher generator, NoiseValueMerger valueMapper, ValueSetFetcher deltaWeight) {
		return (seed, noise) -> {
			final int width = noise.length;
			final int height = noise[0].length;
			ValueFetcher source = generator.get(seed, width, height);
			ValueFetcher weight = deltaWeight.get(seed, width, height);
			for(int x = 0; x < noise.length; x++)
				for(int y = 0; y < noise[x].length; y++)
					noise[x][y] = (valueMapper.get(noise[x][y], source.get(x, y)) - noise[x][y]) * weight.get(x, y) + noise[x][y];
		};
	}
	
	// when modifying a noise map value by value, there are two important data sets besides the existing map:
	// - the map of result values
	// - the map of result value importance
	
	
	/*static NoiseModifier weigh(NoiseGenerator generator, float genWeight) {
		return combine(ValueSetFetcher.get(generator), (origNoise, newNoise) -> {
			float result = origNoise + newNoise * genWeight;
			result /= genWeight + 1;
			return result;
		});
	}
	
	static NoiseModifier multiply(NoiseGenerator generator) {
		return forGenerator(generator, (origNoise, newNoise, x, y) -> origNoise * newNoise);
	}*/
	
	
	/*@FunctionalInterface
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
	}*/
}
