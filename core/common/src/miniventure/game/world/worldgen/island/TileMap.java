package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class TileMap implements TileSource {
	
	public static class TileMapBuilder {
		
		private final LinkedList<WeightedSource> regions;
		
		public TileMapBuilder() {
			regions = new LinkedList<>();
		}
		
		public TileMapBuilder addRegion(float size, @NotNull TileSource region) {
			regions.add(new WeightedSource(region, size));
			return this;
		}
		
		public TileMap get() {
			return new TileMap(regions.toArray(new WeightedSource[0]));
		}
	}
	
	private static class WeightedSource {
		@NotNull
		private final TileSource source;
		private float size; // negative size will cause undefined results.
		
		private WeightedSource(@NotNull TileSource source, float size) {
			this.source = source;
			this.size = size;
		}
	}
	
	private final TileSource[] sources;
	private final float[] sizes;
	
	private TileMap(WeightedSource[] regions) {
		if(regions.length == 0) {
			throw new IllegalArgumentException("regions array in TileMap constructor has length 0; at least one region is required.");
		}
		
		sources = new TileSource[regions.length];
		sizes = new float[regions.length];
		
		float total = 0;
		for(int i = 0; i < regions.length; i++) {
			WeightedSource region = regions[i];
			sources[i] = region.source;
			sizes[i] = region.size;
			total += region.size;
		}
		
		// total computed; scale sizes to fit within range 0 to 1.
		for(int i = 0; i < sizes.length; i++) {
			sizes[i] = MyUtils.mapFloat(sizes[i], 0, total, 0, 1);
		}
	}
	
	@Override
	public TileTypeEnum getTileType(float value) {
		if(value < 0 || value > 1) {
			System.err.println("Tile map recieved noise value outside expected range (0-1): "+value+" - clamping value and continuing processing.");
			value = MyUtils.clamp(value, 0, 1);
		}
		
		// in case the sizes don't add up exactly to 1 (due to floating point error or something, causing a noise value of 1 to never be reached during the loop), the default source is the last one, since that would be the intended match.
		int idx = sources.length - 1;
		float total = 0;
		for(int i = 0; i < sources.length; i++) {
			total += sizes[i];
			if(total >= value) {
				idx = i;
				break;
			}
		}
		
		return sources[idx].getTileType(value);
	}
}
