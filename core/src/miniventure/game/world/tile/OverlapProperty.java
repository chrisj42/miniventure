package miniventure.game.world.tile;

import java.util.TreeSet;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class OverlapProperty implements TileProperty {
	
	private final boolean overlaps;
	
	OverlapProperty(boolean overlaps) {
		this.overlaps = overlaps;
	}
	
	Array<AtlasRegion> getSprites(Tile tile) {
		return getSprites(tile, false);
	}
	Array<AtlasRegion> getSprites(Tile tile, boolean useUnder) {
		Array<AtlasRegion> sprites = new Array<>();
		
		/*
			Steps:
				- get a list of the different tile types around the given tile, ordered according to the z index.
				- remove all types from the list that are at or below the given tile's z index.
				- iterate through each type, looking for the following:
					- find all overlap situations that the other type matches, and add that type's overlap sprite(s) to the list of sprites to render
					
		 */
		
		TileType[] aroundTiles = new TileType[9];
		TreeSet<TileType> types = new TreeSet<>(TileType.tileSorter);
		int i = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Tile oTile = tile.getLevel().getTile(tile.x + x, tile.y + y);
				if(oTile != null) {
					TileType oType = oTile.getType();
					TileType oUnder = oTile.getUnderType();
					if(useUnder) {
						if(oUnder != null)
							aroundTiles[i] = oUnder;
						else
							aroundTiles[i] = oType;
					}
					else if(oUnder != null) // if not using under, those without an under tile will not be drawn.
						aroundTiles[i] = oType;
					
					if(aroundTiles[i] != null && aroundTiles[i].getProp(OverlapProperty.class).overlaps)
						types.add(aroundTiles[i]);
				}
				i++;
			}
		}
		
		final TileType compareType;
		TileType under = tile.getUnderType();
		if(useUnder && under != null && TileType.tileSorter.compare(under, tile.getType()) < 0)
			compareType = under;
		else
			compareType = tile.getType();
		types.removeIf(tileType -> TileType.tileSorter.compare(tileType, compareType) <= 0);
		
		Array<Integer> indexes = new Array<>();
		for(TileType type: types) {
			indexes.clear();
			
			int[] bits = new int[4];
			if(aroundTiles[1] == type) bits[0] = 1;
			if(aroundTiles[5] == type) bits[1] = 1;
			if(aroundTiles[7] == type) bits[2] = 1;
			if(aroundTiles[3] == type) bits[3] = 1;
			int total = 4, value = 1;
			for(int num: bits) {
				total += num * value;
				value *= 2;
			}
			indexes.add(total);
			if(aroundTiles[2] == type && bits[0] == 0 && bits[1] == 0) indexes.add(0);
			if(aroundTiles[8] == type && bits[1] == 0 && bits[2] == 0) indexes.add(1);
			if(aroundTiles[6] == type && bits[2] == 0 && bits[3] == 0) indexes.add(2);
			if(aroundTiles[0] == type && bits[3] == 0 && bits[0] == 0) indexes.add(3);
			for(Integer idx: indexes)
				sprites.add(type.getProp(AnimationProperty.class).getSprite(idx, true, tile, type));
		}
		return sprites;
	}
	
	@Override
	public Integer[] getInitData() {
		return new Integer[0];
	}
}
