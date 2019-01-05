package miniventure.game.world.levelgen.noise;

import java.util.LinkedList;

public class NoiseConfiguration implements NoiseGenerator {
	
	private final NoiseGenerator generator;
	private final LinkedList<NoiseModifier> modifiers;
	
	public NoiseConfiguration(NoiseGenerator generator) {
		this.generator = generator;
		this.modifiers = new LinkedList<>();
	}
	
	public NoiseConfiguration modify(NoiseModifier modifier) {
		modifiers.add(modifier);
		return this;
	}
	
	@Override
	public float[][] get2DNoise(long seed, int width, int height) {
		float[][] noise = generator.get2DNoise(seed, width, height);
		for(NoiseModifier mod: modifiers)
			mod.modify(seed, noise);
		return noise;
	}
}
