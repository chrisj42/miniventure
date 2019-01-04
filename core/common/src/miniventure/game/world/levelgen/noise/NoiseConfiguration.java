package miniventure.game.world.levelgen.noise;

public class NoiseConfiguration implements NoiseGenerator {
	
	private final NoiseGenerator generator;
	private final NoiseModifier[] modifiers;
	
	public NoiseConfiguration(NoiseGenerator generator, NoiseModifier... modifiers) {
		this.generator = generator;
		this.modifiers = modifiers;
	}
	
	@Override
	public float[][] get2DNoise(long seed, int width, int height) {
		float[][] noise = generator.get2DNoise(seed, width, height);
		for(NoiseModifier mod: modifiers)
			mod.modify(seed, noise);
		return noise;
	}
}
