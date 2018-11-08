package miniventure.game.world.levelgen;

import java.util.HashMap;
import java.util.Random;

import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class GroupNoiseMapper extends NoiseMapper {
	
	/*
		Honestly, this shouldn't be a subclass. It really takes an input and gives a float which you can then give to a noise mapper. And each of the regions have a source noise function. they *are* noise functions, even. Not map regions, noise functions. Then afterwards, you pipe that into a noise mapper. but that shouldn't have to be part of this class.
	 */
	
	private final HashMap<NoiseMapRegion, Coherent2DNoiseFunction> regionFunctions = new HashMap<>();
	
	public GroupNoiseMapper(@NotNull String name, @NotNull NamedNoiseFunction source) {
		super(name, source);
	}
	
	@Override
	void setSeedRecursively(Random rand) {
		regionFunctions.clear();
		for(NoiseMapRegion region: getRegions()) {
			getSource().setSeed(rand.nextLong());
			getSource().resetFunction();
			regionFunctions.put(region, getSource().getNoiseFunction());
		}
		super.setSeedRecursively(rand);
	}
	
	/*
		So it uses the region sizes to determine the needed value for the biome noise.
		the first region will be treated as the filler, with its size being the spacing between biomes.
		the sizes of the following regions will be the literal maximum values for the biome.
	 */
	
	@Override
	public TileTypeEnum getTileType(int x, int y) {
		NoiseMapRegion[] regions = getRegions();
		NoiseMapRegion filler = regions[0];
		
		if(regions.length == 1) return filler.getTileType(x, y);
		
		float threshold = filler.getSize();
		
		float[] values = new float[regions.length-1];
		float sum = 0;
		
		for(int i = 1; i < regions.length; i++) {
			values[i-1] = regionFunctions.get(regions[i]).getValueRaw(x, y);
			sum += values[i-1];
		}
		
		NoiseMapRegion biome = null;
		// boolean close = false;
		for(int i = 0; i < values.length; i++) {
			if(values[i]*2 - sum > threshold) {
				// in range to *be* this biome.
				if(biome == null)
					biome = regions[i+1];
				// else
				// 	System.out.println("multimatch for value "+values[i]+"; first region="+biome+", second region="+regions[i+1]+" sticking with first match.");
			}
		}
		
		if(biome == null) biome = filler; // no biomes were in range.
		return biome.getTileType(x, y);
	}
}
