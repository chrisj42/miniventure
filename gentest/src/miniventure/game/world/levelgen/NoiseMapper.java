package miniventure.game.world.levelgen;

import java.util.ArrayList;

import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class NoiseMapper implements NamedObject {
	
	@NotNull private String name;
	private NamedNoiseFunction source;
	private ArrayList<NoiseMapRegion> regions;
	private float total;
	
	public NoiseMapper(@NotNull String name, @NotNull NamedNoiseFunction source) {
		this.name = name;
		this.source = source;
		regions = new ArrayList<>();
	}
	
	int getRegionCount() { return regions.size(); }
	NoiseMapRegion[] getRegions() { return regions.toArray(new NoiseMapRegion[regions.size()]); }
	
	NoiseMapRegion addRegion() {
		// adds and returns a new region
		recomputeTotal();
		NoiseMapRegion newRegion = new NoiseMapRegion(TileTypeEnum.GRASS, total/regions.size());
		addRegion(newRegion);
		return newRegion;
	}
	private void addRegion(NoiseMapRegion region) {
		regions.add(region);
		recomputeTotal();
	}
	NoiseMapper addRegion(@NotNull TileTypeEnum type, float size) {
		addRegion(new NoiseMapRegion(type, size));
		return this;
	}
	NoiseMapper addRegion(@NotNull NoiseMapper chainMapper, float size) {
		addRegion(new NoiseMapRegion(chainMapper, size));
		return this;
	}
	void addRegion(@NotNull TileTypeEnum type, NoiseMapper chainMapper, boolean givesTile, float size) {
		addRegion(new NoiseMapRegion(type, chainMapper, givesTile, size));
	}
	
	void removeRegion(NoiseMapRegion region) {
		if(regions.remove(region))
			recomputeTotal();
	}
	
	public float getTotal() { return total; }
	
	public TileTypeEnum getTileType(int x, int y) {
		if(source == null) {
			System.out.println("noise map "+this+" has no source noise function, returning null");
			return null;
		}
		return getTileType(x, y, source.getNoiseFunction().getValue(x, y));
	}
	public TileTypeEnum getTileType(int x, int y, float value) {
		// System.out.println("getting tiletype for value "+value+" at "+x+','+y+" from mapper "+this);
		float total = 0;
		for(NoiseMapRegion region: regions) {
			float add = region.size/this.total;
			// System.out.println("checking region "+region+", adding "+add+" to running total of "+total);
			total += add;
			if(total >= value) {
				if(region.givesTileType())
					return region.getTileType();
				else if(region.getChainNoiseMapper() != null)
					return region.getChainNoiseMapper().getTileType(x, y);
				else {
					System.out.println("region "+region+" uses mapper but ref is null, returning null");
					return null;
				}
			}
		}
		
		System.out.println("value "+value+" is not in range of mapper "+this+", total = "+total+"(should be 1); returning null");
		return null;
	}
	
	private void recomputeTotal() {
		float total = 0;
		for(NoiseMapRegion region: regions)
			total += region.size;
		this.total = total;
	}
	
	public NamedNoiseFunction getSource() { return source; }
	public void setSource(NamedNoiseFunction source) { this.source = source; }
	
	public class NoiseMapRegion {
		private float size;
		
		private boolean givesTileType;
		@NotNull private TileTypeEnum tileType;
		private NoiseMapper chainNoiseMapper;
		
		private NoiseMapRegion(@NotNull TileTypeEnum type, NoiseMapper chainMapper, boolean givesTile, float size) {
			this.tileType = type;
			this.chainNoiseMapper = chainMapper;
			this.givesTileType = givesTile;
			this.size = size;
		}
		private NoiseMapRegion(@NotNull TileTypeEnum tileType, float size) {
			this.tileType = tileType;
			this.size = size;
			givesTileType = true;
		}
		private NoiseMapRegion(@NotNull NoiseMapper chainMap, float size) {
			tileType = TileTypeEnum.HOLE;
			this.chainNoiseMapper = chainMap;
			this.size = size;
			givesTileType = false;
		}
		
		public float getSize() {
			return size;
		}
		
		public void setSize(float size) {
			this.size = size;
			recomputeTotal();
		}
		
		public boolean givesTileType() {
			return givesTileType;
		}
		
		public void setGivesTileType(boolean givesTileType) {
			this.givesTileType = givesTileType;
		}
		
		@NotNull
		public TileTypeEnum getTileType() {
			// System.out.println("getting type "+tileType);
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
		
		public int getIndex() { return regions.indexOf(this); }
		
		@Override
		public String toString() { return NoiseMapper.this+" Region #"+getIndex()+"; size="+size+", givesTile="+givesTileType+", tiletype="+tileType+", chainmapper="+chainNoiseMapper; }
	}
	
	@Override
	public void setObjectName(@NotNull String name) { this.name = name; }
	@Override @NotNull
	public String getObjectName() { return name; }
	
	@Override
	public String toString() { return name; }
}
