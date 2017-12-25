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
	
	private static final int BLACK = -1, WHITE = 1;
	
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
	private final int width, height;
	
	private TileTouchCheck(Pixmap pixelMap, TextureRegion region) {
		this.width = region.getRegionWidth();
		this.height = region.getRegionHeight();
		map = new int[width * height];
		int i = -1;
		// pixmap coordinates have the origin in the top left corner; shift it so it goes from the bottom left instead
		for (int x = 0; x < width; x++) {
			for (int y = height-1; y >= 0; y--) {
				Color color = new Color(pixelMap.getPixel(region.getRegionX() + x, region.getRegionY() + y));
				
				i++;
				if(color.a == 0) continue; // set to zero, tile doesn't matter
				
				if(color.equals(Color.WHITE)) // the tile must be different from the center tile
					map[i] = WHITE;
				else if(color.equals(Color.BLACK)) // the tile must be equal to the center tile
					map[i] = BLACK;
			}
		}
	}
	
	boolean checkMatch(TileType[] tiles, TileType black, TileType white, boolean nullMatches) {
		return checkMatch(tiles, new Array<>(new TileType[] {black}), new Array<>(new TileType[] {white}), nullMatches);
	}
	boolean checkMatch(TileType[] tiles, Array<TileType> black, Array<TileType> white, boolean nullMatches) {
		if(tiles.length != map.length)
			throw new IllegalArgumentException("tile type array must be of equal size to overlap map array; "+tiles.length+"!="+map.length);
		
		// if the white or black tiles are null, then it means it works if it doesn't match the other color.
		
		for(int i = 0; i < tiles.length; i++) {
			int matchRule = map[i];
			TileType type = tiles[i];
			if(type == null) {
				if(nullMatches) continue;
				else return false;
			}
			
			if(matchRule == WHITE && (white == null && black.contains(type, true) || white != null && !white.contains(type, true)))
				return false;
			if(matchRule == BLACK && (black == null && white.contains(type, true) || black != null && !black.contains(type, true)))
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
