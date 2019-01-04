package miniventure.game.world.levelgen.noise;

import miniventure.game.util.ArrayUtils;

@FunctionalInterface
public interface NoiseModifier {
	
	void modify(long seed, float[][] noise);
	
	
	NoiseModifier ISLAND_MASK = constructLater((seed, width, height) -> {
		final float maxDist = (float) Math.hypot(width/2f, height/2f);
		return forEach((value, x, y) -> {
			float xd = Math.abs(x-width/2f);
			float yd = Math.abs(y-height/2f);
			float dist = (float) Math.hypot(xd, yd);
			float trans = 1 - dist/maxDist;
			return value * (float) Math.pow(trans, 1.75);
		});
	});
	
	
	NoiseModifier FILL_VALUE_RANGE = (seed, noise) -> ArrayUtils.deepMapArray(noise, 0, 1);
	
	
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
}
