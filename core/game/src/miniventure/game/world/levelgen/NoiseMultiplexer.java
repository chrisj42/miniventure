package miniventure.game.world.levelgen;

import javax.swing.JLabel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

// a data class only, not display.
public class NoiseMultiplexer {
	
	/*
		I am going to give noise maps parameters. These parameters will have to be specified whenever the map is used.
		There will be 3 possible parameter types:
			- Float values (for region sizes)
			- Noise Functions (for source functions)
			- Tile Fetcher (this means noise maps and/or tiletypes)
		
		There will be a button next to "add region" that reads "add parameter"
		clicking it brings up a panel below, similar to the region list panel.
			- you can name the parameter
			- you can select the parameter type
	 */
	
	@NotNull private String name;
	// private final ArrayList<>
	private NamedNoiseFunction source;
	private final ArrayList<NoiseMapRegion> regions;
	private float total;
	
	public NoiseMultiplexer(@NotNull String name, @NotNull NamedNoiseFunction source) {
		this.name = name;
		this.source = source;
		regions = new ArrayList<>();
	}
	
	void setSeedRecursively(Random rand) {
		source.setSeed(rand.nextLong());
		source.resetFunction();
		for(NoiseMapRegion r: regions)
			if(!r.givesTileType)
				r.chainNoiseMapper.setSeedRecursively(rand);
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
	public NoiseMultiplexer addRegion(@NotNull TileTypeEnum type, float size) {
		addRegion(new NoiseMapRegion(type, size));
		return this;
	}
	public NoiseMultiplexer addRegion(@NotNull NoiseMultiplexer chainMapper, float size) {
		addRegion(new NoiseMapRegion(chainMapper, size));
		return this;
	}
	public void addRegion(@NotNull TileTypeEnum type, NoiseMultiplexer chainMapper, boolean givesTile, float size) {
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
	private TileTypeEnum getTileType(int x, int y, float value) {
		// System.out.println("getting tiletype for value "+value+" at "+x+','+y+" from mapper "+this);
		float total = 0;
		for(NoiseMapRegion region: regions) {
			float add = this.total==0?1:region.size/this.total;
			// System.out.println("checking region "+region+", adding "+add+" to running total of "+total);
			total += add;
			if(total >= value)
				return region.getTileType(x, y);
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
	
	public NamedNoiseFunction[] getReferencedFunctions() {
		ArrayList<NamedNoiseFunction> functions = new ArrayList<>();
		HashSet<NoiseMultiplexer> visitedMaps = new HashSet<>();
		LinkedList<NoiseMultiplexer> nextMaps = new LinkedList<>();
		
		nextMaps.add(this);
		while(nextMaps.size() > 0) {
			NoiseMultiplexer map = nextMaps.pollFirst();
			visitedMaps.add(map);
			
			if(!functions.contains(map.getSource()))
				functions.add(map.getSource());
			
			for(NoiseMapRegion region: map.regions) {
				if(region.givesTileType) continue;
				if(!visitedMaps.contains(region.getChainNoiseMapper()))
				nextMaps.addLast(region.chainNoiseMapper);
			}
		}
		
		return functions.toArray(new NamedNoiseFunction[functions.size()]);
	}
	
	public NoiseMultiplexer[] getReferencedMaps() {
		ArrayList<NoiseMultiplexer> maps = new ArrayList<>();
		LinkedList<NoiseMultiplexer> nextMaps = new LinkedList<>();
		
		nextMaps.add(this);
		while(nextMaps.size() > 0) {
			NoiseMultiplexer map = nextMaps.pollFirst();
			maps.add(map);
			
			for(NoiseMapRegion region: map.regions) {
				if(region.givesTileType) continue;
				if(!maps.contains(region.getChainNoiseMapper()))
					nextMaps.addLast(region.chainNoiseMapper);
			}
		}
		
		return maps.toArray(new NoiseMultiplexer[maps.size()]);
	}
	
	public class NoiseMapRegion {
		private float size;
		
		private boolean givesTileType;
		@NotNull private TileTypeEnum tileType;
		private NoiseMultiplexer chainNoiseMapper;
		
		private NoiseMapRegion(@NotNull TileTypeEnum type, NoiseMultiplexer chainMapper, boolean givesTile, float size) {
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
		private NoiseMapRegion(@NotNull NoiseMultiplexer chainMap, float size) {
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
		
		TileTypeEnum getTileType(int x, int y) {
			if(givesTileType)
				return tileType;
			if(chainNoiseMapper != null)
				return chainNoiseMapper.getTileType(x, y);
			
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
		
		public NoiseMultiplexer getChainNoiseMapper() {
			return chainNoiseMapper;
		}
		
		public void setChainNoiseMapper(NoiseMultiplexer chainNoiseMapper) {
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
		public String toString() { return NoiseMultiplexer.this+" Region #"+getIndex()+"; size="+size+", givesTile="+givesTileType+", tiletype="+tileType+", chainmapper="+chainNoiseMapper; }
	}
	
	
	public void setName(@NotNull String name) { this.name = name; }
	@NotNull
	public String getName() { return name; }
	
	@Override
	public String toString() { return name; }
}
