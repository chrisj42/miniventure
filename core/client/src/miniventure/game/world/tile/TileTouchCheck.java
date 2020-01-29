package miniventure.game.world.tile;

import java.util.EnumMap;

import miniventure.game.core.GameCore;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class TileTouchCheck {
	
	private static final int MATCH = 1, NOMATCH = -1, SKIP = 0;
	
	public static final TileTouchCheck[] connectionChecks = getTileChecks(GameCore.tileConnectionAtlas);
	
	private static TileTouchCheck[] getTileChecks(TextureAtlas atlas) {
		if(atlas == null) return new TileTouchCheck[0];
		
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
		if(region.getRegionWidth() != 3 || region.getRegionHeight() != 3)
			throw new IllegalArgumentException("TileTouchCheck map regions must be 3 by 3 pixels. Given region is " +region.getRegionWidth()+" by "+region.getRegionHeight() + " pixels.");
		map = new int[RelPos.values().length];
		// pixmap coordinates have the origin in the top left corner; shift it so it goes from the bottom left instead
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				Color color = new Color(pixelMap.getPixel(region.getRegionX() + x, region.getRegionY() + y));
				
				int val;
				if(color.a == 0) // set to zero, tile doesn't matter
					val = SKIP;
				else if(color.equals(Color.WHITE)) // the tile must be different from the center tile
					val = NOMATCH;
				else if(color.equals(Color.BLACK)) // the tile must be equal to the center tile
					val = MATCH;
				else throw new IllegalArgumentException("Provided texture map for TileTouchCheck must be transparent, black, or white; color " + color + " is invalid.");
				
				map[RelPos.get(x-1, 1-y).ordinal()] = val;
			}
		}
	}
	
	public boolean checkMatch(EnumMap<RelPos, Boolean> aroundMatches) {
		for(int i = 0; i < map.length; i++) {
			int matchRule = map[i];
			if(matchRule == SKIP) continue;
			if(RelPos.values(i) == RelPos.CENTER) continue;
			if(aroundMatches.get(RelPos.values(i)) != (matchRule == MATCH)) // if the actual match does not agree with the required match
				return false;
		}
		
		return true;
	}
}
