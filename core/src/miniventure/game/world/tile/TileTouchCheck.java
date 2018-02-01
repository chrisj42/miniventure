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
	
	private static final int MATCH = 1, NOMATCH = -1, SKIP = 0;
	
	static final TileTouchCheck[] connectionChecks = getTileChecks(GameCore.tileConnectionAtlas);
	
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
	private final int width = 3, height = 3;
	
	private TileTouchCheck(Pixmap pixelMap, TextureRegion region) {
		if(this.width != region.getRegionWidth() || this.height != region.getRegionHeight())
			throw new IllegalArgumentException("TileTouchCheck map regions must be "+width+" by "+height+" pixels. Given region is " +region.getRegionWidth()+" by "+region.getRegionHeight() + " pixels.");
		map = new int[width * height];
		int i = -1;
		// pixmap coordinates have the origin in the top left corner; shift it so it goes from the bottom left instead
		for (int x = 0; x < width; x++) {
			for (int y = height-1; y >= 0; y--) {
				Color color = new Color(pixelMap.getPixel(region.getRegionX() + x, region.getRegionY() + y));
				
				i++;
				if(color.a == 0) // set to zero, tile doesn't matter
					map[i] = SKIP;
				else if(color.equals(Color.WHITE)) // the tile must be different from the center tile
					map[i] = NOMATCH;
				else if(color.equals(Color.BLACK)) // the tile must be equal to the center tile
					map[i] = MATCH;
				else throw new IllegalArgumentException("Provided texture map for TileTouchCheck must be transparent, black, or white; color " + color + " is invalid.");
			}
		}
	}
	
	/*boolean checkMatch(TileType[] tiles, TileType black, TileType white, boolean nullMatches) {
		return checkMatch(tiles, new Array<>(new TileType[] {black}), new Array<>(new TileType[] {white}), nullMatches);
	}*/
	boolean checkMatch(boolean[] aroundMatches) {
		if(aroundMatches.length != map.length)
			throw new IllegalArgumentException("tile type array must be of equal size to map array; "+aroundMatches.length+"!="+map.length);
		
		// if the white or black tiles are null, then it means it works if it doesn't match the other color.
		
		for(int i = 0; i < map.length; i++) {
			int matchRule = map[i];
			if(matchRule == SKIP) continue;
			/*TileType type = tiles[i];
			if(type == null) {
				if(nullMatches) continue;
				else return false;
			}*/
			
			/*if(matchRule == NOMATCH && (white == null && black.contains(type, true) || white != null && !white.contains(type, true)))
				return false;
			if(matchRule == MATCH && (black == null && white.contains(type, true) || black != null && !black.contains(type, true)))
				return false;*/
			if(aroundMatches[i] != (matchRule == MATCH)) // if the actual match does not agree with the required match
				return false;
			
			/*if(black == null && type != white && (matchRule == BLACK || matchRule == WHITE)) {
				// here, make sure that if the tile is adjacent to a white tile, that the current type is not greater than the white tile in z order.
				if(i >= height && map[i-height] == WHITE && tiles[i-height] == white || i+height < tiles.length && map[i+height] == WHITE && tiles[i+height] == white || i%height > 0 && map[i-1] == WHITE && tiles[i-1] == white || i%height < height-1 && map[i+1] == WHITE && tiles[i+1] == white)
					if(TileType.tileSorter.compare(type, white) > 0)
						return false;
			}*/
		}
		
		return true;
	}
}
