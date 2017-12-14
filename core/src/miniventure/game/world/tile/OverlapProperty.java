package miniventure.game.world.tile;

import java.util.TreeSet;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class OverlapProperty implements TileProperty {
	
	/*
		The sprites will be named "tile/##[_#]", the _# only included if the tile is animated for that sprite.
		
		The first ## is the index that matches the names of each of the sprites in the tile overlap map. They are applied whenever the corresponding tile overlap map sprite matches the tile's surrounding neighbors.
		they are drawn in order.
		
		Note: tiles at the edges of the map treat the border as matching itself.
		
		This will work, because though it will draw sides as if it intersects all three on the given side, the ones on the corners will draw over that (if they're supposed to).
	 */
	
	//private int[] overlapCheckIndexes;
	private final boolean overlaps;
	
	OverlapProperty(boolean overlaps) {
		this.overlaps = overlaps;
		/*if(overlaps) {
			overlapCheckIndexes = new int[TileOverlapCheck.overlapChecks.length];
			for(int i = 0; i < overlapCheckIndexes.length; i++)
				overlapCheckIndexes[i] = i;
		}
		else
			overlapCheckIndexes = new int[] {0};*/
		
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
					aroundTiles[i] = oTile.getType();
					if(useUnder && aroundTiles[i].animationProperty.renderBehind != null)
						aroundTiles[i] = aroundTiles[i].animationProperty.renderBehind;
					if(aroundTiles[i].overlapProperty.overlaps)
						types.add(aroundTiles[i]);
				}
				i++;
			}
		}
		
		final TileType compareType;
		TileType under = tile.getType().animationProperty.renderBehind;
		if(useUnder && under != null && TileType.tileSorter.compare(under, tile.getType()) < 0)
			compareType = under;
		else 
			compareType = tile.getType();
		types.removeIf(tileType -> TileType.tileSorter.compare(tileType, compareType) <= 0);
		//if(types.size() > 0)
			//System.out.println("tiles around " + tile.getType() + " that overlap: " + types);
		for(TileType type: types)
			for (i = 0; i < TileTouchCheck.overlapChecks.length; i++)
				if (TileTouchCheck.overlapChecks[i].checkMatch(aroundTiles, type, true))
					sprites.add(type.animationProperty.getSprite(i, true, tile, type));
		
		return sprites;
	}
	
	@Override
	public Integer[] getInitData() {
		return new Integer[0];
	}
}
