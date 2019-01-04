package miniventure.game.world.levelgen;

import java.util.ArrayList;
import java.util.LinkedList;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class TileMap {
	
	private final ArrayList<NoiseMapRegion> regions;
	private float total;
	
	public TileMap() {
		regions = new ArrayList<>();
	}
	
	public int getRegionCount() { return regions.size(); }
	public NoiseMapRegion[] getRegions() { return regions.toArray(new NoiseMapRegion[0]); }
	
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
	public TileMap addRegion(@NotNull TileTypeEnum type, float size) {
		addRegion(new NoiseMapRegion(type, size));
		return this;
	}
	public TileMap addRegion(@NotNull TileMap chainMapper, float size) {
		addRegion(new NoiseMapRegion(chainMapper, size));
		return this;
	}
	public void addRegion(@NotNull TileTypeEnum type, TileMap chainMapper, boolean givesTile, float size) {
		addRegion(new NoiseMapRegion(type, chainMapper, givesTile, size));
	}
	
	public void removeRegion(NoiseMapRegion region) {
		if(regions.remove(region))
			recomputeTotal();
	}
	
	public float getTotal() { return total; }
	
	public TileTypeEnum getTileType(float value) {
		// System.out.println("getting tiletype for value "+value+" at "+x+','+y+" from mapper "+this);
		float total = 0;
		for(NoiseMapRegion region: regions) {
			float add = this.total==0?1:region.size/this.total;
			// System.out.println("checking region "+region+", adding "+add+" to running total of "+total);
			total += add;
			if(total >= value)
				return region.getTileType(value);
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
	
	public TileMap[] getReferencedMaps() {
		ArrayList<TileMap> maps = new ArrayList<>();
		LinkedList<TileMap> nextMaps = new LinkedList<>();
		
		nextMaps.add(this);
		while(nextMaps.size() > 0) {
			TileMap map = nextMaps.pollFirst();
			maps.add(map);
			
			for(NoiseMapRegion region: map.regions) {
				if(region.givesTileType) continue;
				if(!maps.contains(region.getChainNoiseMapper()))
					nextMaps.addLast(region.chainNoiseMapper);
			}
		}
		
		return maps.toArray(new TileMap[0]);
	}
	
	public class NoiseMapRegion {
		private float size;
		
		private boolean givesTileType;
		@NotNull private TileTypeEnum tileType;
		private TileMap chainNoiseMapper;
		
		private NoiseMapRegion(@NotNull TileTypeEnum type, TileMap chainMapper, boolean givesTile, float size) {
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
		private NoiseMapRegion(@NotNull TileMap chainMap, float size) {
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
		
		TileTypeEnum getTileType(float value) {
			if(givesTileType)
				return tileType;
			if(chainNoiseMapper != null)
				return chainNoiseMapper.getTileType(value);
			
			System.out.println("region "+this+" uses mapper but ref is null, returning null");
			return null;
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
		
		public TileMap getChainNoiseMapper() {
			return chainNoiseMapper;
		}
		
		public void setChainNoiseMapper(TileMap chainNoiseMapper) {
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
		public String toString() { return TileMap.this+" Region #"+getIndex()+"; size="+size+", givesTile="+givesTileType+", tiletype="+tileType+", chainmapper="+chainNoiseMapper; }
	}
	
	
	/*public void setName(@NotNull String name) { this.name = name; }
	@NotNull
	public String getName() { return name; }
	
	@Override
	public String toString() { return name; }*/
}
