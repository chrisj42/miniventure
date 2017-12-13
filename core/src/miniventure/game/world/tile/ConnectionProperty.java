package miniventure.game.world.tile;

import java.util.TreeSet;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class ConnectionProperty implements TileProperty {
	
	/*
		The sprites will be named "tile/##[_#]", the _# only included if the tile is animated for that sprite.
		
		The first ## is the index that matches the names of each of the sprites in the tile connection map. They are applied whenever the corresponding tile connection map sprite matches the tile's surrounding neighbors.
		they are drawn in order.
		
		Note: tiles at the edges of the map treat the border as matching itself.
		
		This will work, because though it will draw sides as if it intersects all three on the given side, the ones on the corners will draw over that (if they're supposed to).
	 */
	
	//private int[] connectionCheckIndexes;
	private boolean connects;
	
	public ConnectionProperty(boolean connects) {
		this.connects = connects;
		/*if(connects) {
			connectionCheckIndexes = new int[TileConnectionCheck.connectionChecks.length];
			for(int i = 0; i < connectionCheckIndexes.length; i++)
				connectionCheckIndexes[i] = i;
		}
		else
			connectionCheckIndexes = new int[] {0};*/
		
	}
	
	public Array<AtlasRegion> getSprites(Tile tile) {
		Array<AtlasRegion> sprites = new Array<>();
		sprites.add(tile.getType().animationProperty.getSprite("00", tile));
		
		if(!connects) return sprites;
		
		TileType[] aroundTiles = new TileType[9];
		TreeSet<TileType> types = new TreeSet<>(TileType.tileSorter);
		int i = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Tile oTile = tile.getLevel().getTile(tile.x + x, tile.y + y);
				aroundTiles[i] = oTile == null ? null : oTile.getType();
				if(aroundTiles[i] != null) types.add(aroundTiles[i]);
				i++;
			}
		}
		
		types.removeIf(tileType -> TileType.tileSorter.compare(tile.getType(), tileType) <= 0);
		for(TileType type: types) {
			//for(int connectionIdx: connectionCheckIndexes) {
			for (i = 0; i < TileConnectionCheck.connectionChecks.length; i++) {
				if (TileConnectionCheck.connectionChecks[i].checkMatch(aroundTiles, type)) {
					String connectIdx = (i < 10 ? "0" : "") + i;
					sprites.add(type.animationProperty.getSprite(connectIdx, tile));
				}
			}
		}
		
		return sprites;
	}
	
	@Override
	public Integer[] getInitData() {
		return new Integer[0];
	}
}
