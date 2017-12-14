package miniventure.game.world.tile;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

class TileTouchCheck {
	
	static final TileTouchCheck[] overlapChecks, connectionChecks;
	static {
		overlapChecks = getTileChecks(GameCore.tileOverlapAtlas);
		connectionChecks = getTileChecks(GameCore.tileConnectionAtlas);
	}
	
	private static TileTouchCheck[] getTileChecks(TextureAtlas atlas) {
		Texture texture = atlas.getTextures().first();
		if (!texture.getTextureData().isPrepared())
			texture.getTextureData().prepare();
		Pixmap pixelMap = texture.getTextureData().consumePixmap();
		
		Array<AtlasRegion> tileMaps = atlas.getRegions(); // hopefully, they are in order.
		TileTouchCheck[] checks = new TileTouchCheck[tileMaps.size];
		for(int i = 0; i < checks.length; i++) {
			checks[i] = new TileTouchCheck(pixelMap, tileMaps.get(i));
		}
		
		return checks;
	}
	
	
	private final int[] map;
	
	private TileTouchCheck(Pixmap pixelMap, TextureRegion region) {
		map = new int[region.getRegionWidth() * region.getRegionHeight()];
		int i = -1;
		// pixmap coordinates have the origin in the top left corner; shift it so it goes from the bottom left instead
		for (int x = 0; x < region.getRegionWidth(); x++) {
			for (int y = region.getRegionHeight()-1; y >= 0; y--) {
				Color color = new Color(pixelMap.getPixel(region.getRegionX() + x, region.getRegionY() + y));
				
				i++;
				if(color.a == 0) continue; // set to zero, tile doesn't matter
				
				if(color.equals(Color.WHITE)) // the tile must be different from the center tile
					map[i] = -1;
				else if(color.equals(Color.BLACK)) // the tile must be equal to the center tile
					map[i] = 1;
			}
		}
	}
	
	boolean checkMatch(TileType[] tiles, TileType other, boolean nullMatches) {
		if(tiles.length != map.length)
			throw new IllegalArgumentException("tile type array must be of equal size to overlap map array; "+tiles.length+"!="+map.length);
		
		TileType center = tiles[tiles.length/2]; // in an array of length 9, 9/2 = 4, the 5th number, which is indeed the center.
		
		for(int i = 0; i < tiles.length; i++) {
			int matchRule = map[i];
			TileType type = tiles[i];
			if(type == null) {
				if(nullMatches) continue;
				else return false;
			}
			if(matchRule == 0 || i == tiles.length/2) continue; // doesn't matter
			
			if(matchRule == 1 && type != center) // must match
				return false;
			if(matchRule == -1 && (other == null && type == center || other != null && type != other)) // must match the opposing type, unless opposing type is null in which case it must not match the center
				return false;
		}
		
		return true;
	}
}
