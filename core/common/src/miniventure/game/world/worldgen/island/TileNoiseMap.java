package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.util.MyUtils;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class TileNoiseMap implements TileProcessor {
	
	public static class TileMapBuilder {
		
		private final LinkedList<WeightedSource> regions;
		
		public TileMapBuilder(float firstRegionSize, @NotNull TileProcessor firstProcessor) {
			regions = new LinkedList<>();
			addRegion(firstRegionSize, firstProcessor);
		}
		
		public TileMapBuilder addRegion(float size, @NotNull TileProcessor region) {
			regions.add(new WeightedSource(region, size));
			return this;
		}
		
		public TileNoiseMap get(float[][] noise) {
			return new TileNoiseMap(noise, regions.toArray(new WeightedSource[0]));
		}
	}
	
	private static class WeightedSource {
		@NotNull
		private final TileProcessor source;
		private float size; // negative size will cause undefined results.
		
		private WeightedSource(@NotNull TileProcessor source, float size) {
			this.source = source;
			this.size = size;
		}
	}
	
	private final float[][] noise;
	private final TileProcessor[] processors;
	private final float[] sizes;
	
	private TileNoiseMap(float[][] noise, WeightedSource[] regions) {
		this.noise = noise;
		processors = new TileProcessor[regions.length];
		sizes = new float[regions.length];
		
		float total = 0;
		for(int i = 0; i < regions.length; i++) {
			WeightedSource region = regions[i];
			processors[i] = region.source;
			sizes[i] = region.size;
			total += region.size;
		}
		
		// total computed; scale sizes to fit within range 0 to 1.
		for(int i = 0; i < sizes.length; i++) {
			sizes[i] = MyUtils.mapFloat(sizes[i], 0, total, 0, 1);
		}
	}
	
	@Override
	public void processTile(ProtoTile tile) {
		final float noiseVal = tile.getVal(noise);
		
		// in case the sizes don't add up exactly to 1 (due to floating point error or something, causing a noise value of 1 to never be reached during the loop), the default source is the last one, since that would be the intended match.
		int idx = processors.length - 1;
		float total = 0;
		for(int i = 0; i < processors.length; i++) {
			total += sizes[i];
			if(total >= noiseVal) {
				idx = i;
				break;
			}
		}
		
		processors[idx].processTile(tile);
	}
}
