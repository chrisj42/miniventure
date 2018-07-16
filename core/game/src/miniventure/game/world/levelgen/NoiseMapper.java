package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.Random;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class NoiseMapper {
	
	@NotNull private String name;
	private NamedNoiseFunction source;
	private ArrayList<NoiseMapRegion> regions;
	private float total;
	
	public NoiseMapper(@NotNull String name, @NotNull NamedNoiseFunction source) {
		this.name = name;
		this.source = source;
		regions = new ArrayList<>();
	}
	
	void setSeedRecursively(Random rand) {
		getSource().setSeed(rand.nextLong());
		for(NoiseMapRegion r: regions)
			if(!r.givesTileType)
				r.chainNoiseMapper.setSeedRecursively(rand);
	}
	
	void setMaxCPV(int cpv) { setMaxCPV(cpv, getMaxCPV()); }
	private void setMaxCPV(final int cpv, final int max) {
		// if the recursive max cpv is less than the given, then do nothing.
		if(max <= cpv) return;
		
		// otherwise, scale the current function cpv with it's current fraction of the max, to the cpv being the max.
		source.setCoordsPerValue(source.getCoordsPerValue()*cpv/max);
		for(NoiseMapRegion r: regions)
			if(!r.givesTileType)
				r.chainNoiseMapper.setMaxCPV(cpv, max);
	}
	
	private int getMaxCPV() {
		int max = source.getCoordsPerValue();
		for(NoiseMapRegion r: regions)
			if(!r.givesTileType)
				max = Math.max(max, r.chainNoiseMapper.getMaxCPV());
		return max;
	}
	
	public int getRegionCount() { return regions.size(); }
	public NoiseMapRegion[] getRegions() { return regions.toArray(new NoiseMapRegion[regions.size()]); }
	
	public NoiseMapRegion addRegion() {
		// adds and returns a new region
		recomputeTotal();
		NoiseMapRegion newRegion = new NoiseMapRegion(TileTypeEnum.GRASS, regions.size()==0?1:total/regions.size());
		addRegion(newRegion);
		return newRegion;
	}
	private void addRegion(NoiseMapRegion region) {
		regions.add(region);
		recomputeTotal();
	}
	public NoiseMapper addRegion(@NotNull TileTypeEnum type, float size) {
		addRegion(new NoiseMapRegion(type, size));
		return this;
	}
	public NoiseMapper addRegion(@NotNull NoiseMapper chainMapper, float size) {
		addRegion(new NoiseMapRegion(chainMapper, size));
		return this;
	}
	public void addRegion(@NotNull TileTypeEnum type, NoiseMapper chainMapper, boolean givesTile, float size) {
		addRegion(new NoiseMapRegion(type, chainMapper, givesTile, size));
	}
	
	public void removeRegion(NoiseMapRegion region) {
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
			float add = this.total==0?1:region.size/this.total;
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
		
		public int move(int amt) {
			int idx = getIndex();
			int nidx = MyUtils.clamp(idx+amt, 0, regions.size()-1);
			regions.remove(idx);
			regions.add(nidx, this);
			return nidx;
		}
		
		@Override
		public String toString() { return NoiseMapper.this+" Region #"+getIndex()+"; size="+size+", givesTile="+givesTileType+", tiletype="+tileType+", chainmapper="+chainNoiseMapper; }
	}
	
	
	public void setName(@NotNull String name) { this.name = name; }
	@NotNull
	public String getName() { return name; }
	
	@Override
	public String toString() { return name; }
}
