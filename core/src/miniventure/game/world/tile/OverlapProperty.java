package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class OverlapProperty implements TileProperty {
	
	private final boolean overlaps;
	
	private TileType tileType;
	//private boolean useSurface;
	
	OverlapProperty(boolean overlaps) {
		this.overlaps = overlaps;
	}
	
	@Override public void init(TileType type) {
		this.tileType = type;
		//useSurface = !tileType.isGroundTile();
	}
	
	boolean canOverlap() { return overlaps; }
	
	@NotNull
	Array<AtlasRegion> getSprites(Tile tile, TileType aroundType, Boolean[] aroundMatches) {
		Array<AtlasRegion> sprites = new Array<>();
		
		if(!aroundType.getProp(OverlapProperty.class).overlaps) return sprites;
		if(TileType.tileSorter.compare(tileType, aroundType) >= 0) return sprites;
		
		//if(aroundMatches == null)
			//System.out.println("getting "+aroundType+" overlaps on " + tile + " using OverlapProperty for " + tileType + "; matches: " + Arrays.toString(aroundMatches));
		//if(TileType.tileSorter.compare(aroundType, tileType) <= 0) return sprites;
		/*
			Steps:
				- get a list of the different tile types around the given tile, ordered according to the z index.
				- remove all types from the list that are at or below the given tile's z index.
				- iterate through each type, looking for the following:
					- find all overlap situations that the other type matches, and add that type's overlap sprite(s) to the list of sprites to render
					
		 */
		
		/// get all the tile types around the current tile 
		//TileType[] aroundTiles = new TileType[9];
		//TileType[][] aroundTypes = new TileType[9][];
		//TreeMap<TileType, Array<TileType>> aroundTypes = new TreeMap<>((list1, list2) -> TileType.tileSorter.compare(list1.get(0), list2.get(0)));
		/*TreeMap<TileType, Boolean[]> aroundTypes = new TreeMap<>(TileType.tileSorter);
		int i = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if(x == 0 && y == 0) continue;
				Tile oTile = tile.getLevel().getTile(tile.x + x, tile.y + y);
				if(oTile != null) {
					*//*for(TileType oType: oTile.getTypes()) { // these are sorted with the lowest one first
						if(TileType.tileSorter.compare(oType, tileType) <= 0) continue;
						if (!aroundTypes.contains(oType))
							aroundTypes.add(new Array<>());
						
						//aroundTypes.
						//aroundTiles[i] = oTile.getTypesAbove()//useSurface ? oTile.getSurfaceType() : oTile.getGroundType();
					
						//if (aroundTiles[i] != null && aroundTiles[i].getProp(OverlapProperty.class).overlaps)
						//	types.add(aroundTiles[i]);
					}*//*
					for(TileType oType: oTile.getTypes()) {
						if(!oType.getProp(OverlapProperty.class).overlaps) continue;
						if(TileType.tileSorter.compare(oType, tileType) <= 0) continue;
						aroundTypes.putIfAbsent(oType, new Boolean[9]);
						aroundTypes.get(oType)[i] = true;
					}
				}
				i++;
			}
		}*/
		
		/*TreeMap<TileType, Boolean[]> overlappingTypes = new TreeMap<>();
		for(i = 0; i < aroundTypes.length; i++) {
			if(aroundTypes[i] == null) continue;
			for(int j = 0; j < aroundTypes[i].length; j++) {
				TileType oType = aroundTypes[i][j];
				if(!overlappingTypes.containsKey(oType))
			}
		}*/
		
		/// remove the types that are drawn under the current type
		//types.removeIf(tileType -> TileType.tileSorter.compare(tileType, this.tileType) <= 0);
		
		/// go through each surrounding type that is drawn *over* the current type, find the layout in which it surrounds the tile, and record the overlap sprite to be drawn for it.
		Array<Integer> indexes = new Array<>();
		
		//for(TileType type: aroundTypes.tailMap(tileType, false).keySet()) {
			//if(!type.getProp(OverlapProperty.class).overlaps) continue;
			//indexes.clear();
			//Boolean[] aroundMatches = aroundTypes.get(type);
			
			int[] bits = new int[4];
			if(aroundMatches[1]) bits[0] = 1;
			if(aroundMatches[5]) bits[1] = 1;
			if(aroundMatches[7]) bits[2] = 1;
			if(aroundMatches[3]) bits[3] = 1;
			int total = 4, value = 1;
			for(int num: bits) {
				total += num * value;
				value *= 2;
			}
			indexes.add(total);
			if(aroundMatches[2] && bits[0] == 0 && bits[1] == 0) indexes.add(0);
			if(aroundMatches[8] && bits[1] == 0 && bits[2] == 0) indexes.add(1);
			if(aroundMatches[6] && bits[2] == 0 && bits[3] == 0) indexes.add(2);
			if(aroundMatches[0] && bits[3] == 0 && bits[0] == 0) indexes.add(3);
			for(Integer idx: indexes)
				sprites.add(aroundType.getProp(AnimationProperty.class).getSprite(idx, true, tile));
		//}
		
		return sprites;
	}
}
