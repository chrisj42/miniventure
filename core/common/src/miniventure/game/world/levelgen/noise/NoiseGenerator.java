package miniventure.game.world.levelgen.noise;

@FunctionalInterface
public interface NoiseGenerator {
	
	float[][] get2DNoise(long seed, int width, int height);
	
	NoiseGenerator ISLAND_MASK = (seed, width, height) -> {
		final float maxDist = (float) Math.hypot(width/2f, height/2f);
		
		float[][] values = new float[width][height];
		
		for(int x = 0; x < values.length; x++) {
			for(int y = 0; y < values[x].length; y++) {
				float xd = Math.abs(x-width/2f);
				float yd = Math.abs(y-height/2f);
				float dist = (float) Math.hypot(xd, yd);
				float trans = 1 - dist/maxDist;
				values[x][y] = trans;
			}
		}
		
		return values;
	};
}
