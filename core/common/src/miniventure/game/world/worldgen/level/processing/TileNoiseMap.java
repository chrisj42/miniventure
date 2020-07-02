package miniventure.game.world.worldgen.level.processing;

import java.util.LinkedList;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.worldgen.level.ProtoTile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// a data class only, not display.
public class TileNoiseMap implements TileProcessor {
	
	public static TileNoiseMapBuilder builder() {
		return new TileNoiseMapBuilder();
	}
	
	public static class TileNoiseMapBuilder {
		
		private final LinkedList<WeightedSource> regions;
		private TileProcessor superProcessor;
		
		private TileNoiseMapBuilder() {
			regions = new LinkedList<>();
		}
		
		public TileNoiseMapBuilder addRegion(float size, @Nullable TileProcessor region) {
			if(superProcessor != null)
				region = superProcessor.append(region);
			regions.add(new WeightedSource(region, size));
			return this;
		}
		
		// the overlap processor will be applied to all tiles, and then the sub region processors will be applied on top of that. It's a way pf layering tiles.
		public TileNoiseMapBuilder addOverlapRegion(@NotNull TileProcessor region, float preExcess, float postExcess, ValueAction<TileNoiseMapBuilder> subRegionAdder) {
			if(preExcess > 0)
				addRegion(preExcess, region);
			
			TileProcessor curSuper = superProcessor;
			superProcessor = curSuper == null ? region : curSuper.append(region);
			subRegionAdder.act(this);
			superProcessor = curSuper;
			
			if(postExcess > 0)
				addRegion(postExcess, region);
			return this;
		}
		
		public TileNoiseMapBuilder addRoundOffRegion(@Nullable TileProcessor region) {
			// determine the next power of 10 used by the sizes
			float total = 0;
			for(WeightedSource prevRegion: regions)
				total += prevRegion.size;
			
			int power = (int) Math.ceil(Math.log10(total)); // round up to the next power of 10
			float size = (float) Math.pow(10, power) - total;
			return addRegion(size, region);
		}
		
		public TileNoiseMap get(float[][] noise) {
			return new TileNoiseMap(noise, regions.toArray(new WeightedSource[0]));
		}
	}
	
	private static class WeightedSource {
		@Nullable
		private final TileProcessor source;
		private float size; // negative size will cause undefined results.
		
		private WeightedSource(@Nullable TileProcessor source, float size) {
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
		if(processors.length == 0) return;
		
		final float noiseVal = tile.getVal(noise);
		
		// in case the sizes don't add up exactly to 1 (due to floating point error or something, causing a noise value of 1 to never be reached during the loop), the default source is the last one, since that would be the intended match.
		int idx = processors.length - 1;
		float total = 0;
		for(int i = 0; i < processors.length; i++) {
			total += sizes[i];
			if(noiseVal <= total) {
				idx = i;
				break;
			}
		}
		
		if(processors[idx] != null)
			processors[idx].processTile(tile);
	}
}
