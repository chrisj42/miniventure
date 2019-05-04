package miniventure.game.world.worldgen.noise;

import miniventure.game.util.ArrayUtils;
import miniventure.game.world.worldgen.noise.NoiseMapFetcher.NoiseValueFetcher;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

// for noise patterns that alter existing noise.
// direct instances usually alter the noise according to some predetermined pattern; the seed should not be necessary.
@FunctionalInterface
public interface NoiseModifier {
	
	void modify(GenInfo info, float[][] noise);
	
	NoiseModifier FILL_VALUE_RANGE = (info, noise) -> ArrayUtils.deepMapFloatArray(noise, 0, 1);
	
	@FunctionalInterface
	interface NoiseValueMapper {
		float get(float noise, int x, int y);
	}
	
	static void forEach(float[][] noise, NoiseValueMapper valueFunction) {
		for(int x = 0; x < noise.length; x++)
			for(int y = 0; y < noise[x].length; y++)
				noise[x][y] = valueFunction.get(noise[x][y], x, y);
	}
	
	static NoiseModifier getForEach(NoiseValueMapper valueFunction) {
		return (info, noise) -> forEach(noise, valueFunction);
	}
	
	@FunctionalInterface
	interface ModInitializer {
		NoiseValueMapper init(GenInfo info, float[][] noise);
	}
	
	static NoiseModifier initForEach(ModInitializer maker) {
		return (info, noise) -> forEach(noise, maker.init(info, noise));
	}
	
	static NoiseModifier perturb(NoiseGenerator angle, NoiseGenerator distance, final float maxDist) {
		return (info, noise) -> {
			final int width = noise.length;
			final int height = noise[0].length;
			float[][] anglev = angle.get2DNoise(info);
			float[][] distv = distance.get2DNoise(info);
			final Vector2 off = new Vector2();
			float[][] copy = new float[width][height];
			forEach(copy, (val, x, y) -> {
				off.set(1, 1);
				off.setAngle(anglev[x][y]*360f);
				off.setLength(distv[x][y]*maxDist);
				off.add(x, y);
				return noise[MathUtils.clamp(MathUtils.round(off.x), 0, width-1)][MathUtils.clamp(MathUtils.round(off.y), 0, height-1)];
			});
			for(int i = 0; i < noise.length; i++)
				System.arraycopy(copy[i], 0, noise[i], 0, noise[i].length);
		};
	}
	
	interface NoiseValueMerger {
		float merge(float noise1, float noise2);
		
		NoiseValueMerger MULTIPLY = (noise1, noise2) -> noise1 * noise2;
		NoiseValueMerger OVERWRITE = (noise1, noise2) -> noise2;
	}
	
	// delta weight value of 1 means the change is done completely; a value of 0 means it is not done at all.
	
	// use to average two noise maps.
	static NoiseModifier combine(NoiseMapFetcher generator) {
		return combine(generator, NoiseValueMerger.OVERWRITE, 0.5f);
	}
	// use to merge two noise maps using a given merge method.
	static NoiseModifier combine(NoiseMapFetcher generator, NoiseValueMerger valueMapper) {
		return combine(generator, valueMapper, 1);
	}
	// use to merge two noise maps using a given merge method, but only partially apply the result.
	static NoiseModifier combine(NoiseMapFetcher generator, NoiseValueMerger valueMapper, float deltaWeight) {
		return combine(generator, valueMapper, NoiseMapFetcher.get(deltaWeight));
	}
	// use to interpolate between two noise maps.
	static NoiseModifier combine(NoiseMapFetcher generator, float deltaWeight) {
		return combine(generator, NoiseMapFetcher.get(deltaWeight));
	}
	// use to interpolate between two noise maps on a per-tile basis.
	static NoiseModifier combine(NoiseMapFetcher generator, NoiseMapFetcher deltaWeight) {
		return combine(generator, NoiseValueMerger.OVERWRITE, deltaWeight);
	}
	// main
	static NoiseModifier combine(NoiseMapFetcher generator, NoiseValueMerger valueMerger, NoiseMapFetcher deltaWeight) {
		return (info, noise) -> {
			NoiseValueFetcher source = generator.get(info);
			NoiseValueFetcher weight = deltaWeight.get(info);
			forEach(noise, (val, x, y) -> (valueMerger.merge(val, source.get(x, y)) - val) * weight.get(x, y) + val);
		};
	}
	
	// static void tirtiate
	
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
