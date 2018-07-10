package miniventure.game.world.levelgen;

import java.util.ArrayList;

import miniventure.game.world.levelgen.util.FloatField;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class NoiseMapper implements NamedObject {
	
	@NotNull private String name;
	private Coherent2DNoiseFunction source;
	private ArrayList<NoiseMapRegion> regions;
	
	public NoiseMapper(@NotNull String name, Coherent2DNoiseFunction source) {
		this.name = name;
		this.source = source;
		regions = new ArrayList<>();
		regions.add(new NoiseMapRegion(1));
	}
	
	NoiseMapRegion[] getRegions() { return regions.toArray(new NoiseMapRegion[regions.size()]); }
	
	NoiseMapRegion addRegion() {
		// adds and returns a new region
		NoiseMapRegion newRegion = new NoiseMapRegion(1f/regions.size());
		regions.add(newRegion);
		recomputeSizes();
		return newRegion;
	}
	
	public TileTypeEnum getTileType(int x, int y) {
		if(source == null)
			return null;
		return getTileType(x, y, source.getValue(x, y));
	}
	public TileTypeEnum getTileType(int x, int y, float value) {
		float total = 0;
		for(NoiseMapRegion region: regions) {
			total += region.size;
			if(total > value) {
				if(region.givesTileType())
					return region.getTileType();
				else if(region.getChainNoiseMapper() != null)
					return region.getChainNoiseMapper().getTileType(x, y);
				else
					return null;
			}
		}
		
		return null;
	}
	
	void recomputeSizes() {
		float total = 0;
		for(NoiseMapRegion region: regions)
			total += region.size;
		for(NoiseMapRegion region: regions)
			region.size /= total;
	}
	
	public class NoiseMapRegion {
		private float size;
		
		private boolean givesTileType = true;
		@NotNull private TileTypeEnum tileType;
		private NoiseMapper chainNoiseMapper;
		
		private NoiseMapRegion(float size) {
			this.size = size;
			chainNoiseMapper = null;
			tileType = TileTypeEnum.GRASS;
		}
		
		public float getSize() {
			return size;
		}
		
		public void setSize(float size) {
			this.size = size;
			recomputeSizes();
		}
		
		/*public boolean isValid() {
			if(givesTileType)
				return tileType != null;
			else
				return chainNoiseMapper != null;
		}*/
		
		public boolean givesTileType() {
			return givesTileType;
		}
		
		public void setGivesTileType(boolean givesTileType) {
			this.givesTileType = givesTileType;
		}
		
		@NotNull
		public TileTypeEnum getTileType() {
			return tileType;
		}
		
		public void setTileType(@NotNull TileTypeEnum tileType) {
			this.tileType = tileType;
		}
		
		public NoiseMapper getChainNoiseMapper() {
			return chainNoiseMapper;
		}
		
		public void setChainNoiseMapper(NoiseMapper chainNoiseMapper) {
			this.chainNoiseMapper = chainNoiseMapper;
		}
		
		@Override
		public String toString() { return regions.indexOf(this)+" - "+FloatField.format.format(size); }
	}
	
	@Override
	public void setObjectName(@NotNull String name) { this.name = name; }
	@Override @NotNull
	public String getObjectName() { return name; }
	
	@Override
	public String toString() { return name; }
}
