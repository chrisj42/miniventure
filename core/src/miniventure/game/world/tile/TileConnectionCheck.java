package miniventure.game.world.tile;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

class TileConnectionCheck {
	
	private static final Pixmap pixelMap;
	public static final TileConnectionCheck[] connectionChecks;
	static {
		TextureAtlas connectionAtlas = GameCore.tileConnectionAtlas;
		Texture texture = connectionAtlas.getTextures().first();
		if (!texture.getTextureData().isPrepared())
			texture.getTextureData().prepare();
		pixelMap = texture.getTextureData().consumePixmap();
		
		Array<AtlasRegion> connections = connectionAtlas.getRegions(); // hopefully, they are in order.
		connectionChecks = new TileConnectionCheck[connections.size];
		for(int i = 0; i < connectionChecks.length; i++)
			connectionChecks[i] = new TileConnectionCheck(connections.get(i));
	}
	
	
	private int[] map;
	
	private TileConnectionCheck(TextureRegion region) {
		map = new int[region.getRegionWidth() * region.getRegionHeight()];
		int i = -1;
		for (int x = 0; x < region.getRegionWidth(); x++) {
			for (int y = 0; y < region.getRegionHeight(); y++) {
				Color color = new Color(pixelMap.getPixel(region.getRegionX() + x, region.getRegionY() + y));
				
				i++;
				if(color.a == 0) continue; // set to zero, tile doesn't matter
				
				if(color == Color.WHITE) // the tile must be different from the center tile
					map[i] = -1;
				else if(color == Color.BLACK) // the tile must be equal to the center tile
					map[i] = 1;
			}
		}
	}
	
	public boolean checkMatch(TileType[] tiles, TileType other) {
		if(tiles.length != map.length)
			throw new IllegalArgumentException("tile type array must be of equal size to connection map array; "+tiles.length+"!="+map.length);
		
		TileType center = tiles[tiles.length/2]; // in an array of length 9, 9/2 = 4, the 5th number, which is indeed the center.
		
		for(int i = 0; i < tiles.length; i++) {
			int matchRule = map[i];
			if(matchRule == 0) continue; // doesn't matter
			// null tiles are treated as matching the center
			if(matchRule == 1 && tiles[i] != center) // must match
				return false;
			if(matchRule == -1 && tiles[i] != other) // must match the opposing type
				return false;
		}
		
		return true;
	}
}
