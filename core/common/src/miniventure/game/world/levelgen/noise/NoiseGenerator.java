package miniventure.game.world.levelgen.noise;

@FunctionalInterface
public interface NoiseGenerator {
	
	float[][] get2DNoise(long seed, int width, int height);
	
	/*interface NoiseDelegate extends NoiseGenerator {
		NoiseGenerator getDelegate();
		@Override
		default float[][] get2DNoise(long seed, int width, int height) {
			return getDelegate().get2DNoise(seed, width, height);
		}
	}*/
}
