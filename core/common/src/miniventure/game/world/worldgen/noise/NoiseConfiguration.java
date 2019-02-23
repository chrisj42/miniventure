package miniventure.game.world.worldgen.noise;

// a way to tack modifiers onto a generator.
// additionally, allows for seed tracking and handling of when you want it the same.

import java.util.HashMap;
import java.util.LinkedList;

import miniventure.game.world.worldgen.noise.NoiseModifier.NoiseValueMerger;

public class NoiseConfiguration implements NoiseGenerator {
	
	// add generators with a name
	// use name and "seed id" to specify a noise map
	
	// configs are set up in two stages -- naming the generators, and then building the sequence of modifiers, including usages of the generators.
	
	// the third stage is actually executing the setup.
	
	private final NoiseGenerator initial;
	private final HashMap<String, NoiseGenerator> generators;
	private final LinkedList<NoiseModifier> modifiers;
	
	public NoiseConfiguration(NoiseGenerator initial) {
		this.initial = initial;
		generators = new HashMap<>();
		modifiers = new LinkedList<>();
	}
	
	public NoiseConfiguration register(String name, NoiseGenerator generator) {
		generators.put(name, generator);
		return this;
	}
	
	
	public NoiseConfiguration modify(NoiseModifier modifier) {
		
		return this;
	}
	
	public NoiseConfiguration combine(String genName, int seedId, NoiseValueMerger valueMerger) {
		return modify((info, noise) -> {
			
		});
	}
	
	@Override
	public float[][] get2DNoise(GenInfo info) {
		// 
		return new float[0][];
	}
}
