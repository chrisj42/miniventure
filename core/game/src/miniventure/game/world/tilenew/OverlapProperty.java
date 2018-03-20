package miniventure.game.world.tilenew;


import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class OverlapProperty implements TilePropertyInstance {
	
	private final boolean overlaps;
	
	private TileType tileType;
	
	OverlapProperty(@NotNull TileType tileType, boolean overlaps) {
		this.tileType = tileType;
		this.overlaps = overlaps;
	}
	
	boolean canOverlap() { return overlaps; }
	
	@NotNull
	Array<AtlasRegion> getSprites(Tile tile, TileType aroundType, Boolean[] aroundMatches) {
		Array<AtlasRegion> sprites = new Array<>();
		
		if(!aroundType.getProp(TilePropertyType.Overlap).overlaps) return sprites;
		if(tileType.compareTo(aroundType) >= 0) return sprites;
		
		
		Array<Integer> indexes = new Array<>();
		
		int[] bits = new int[4];
		if(aroundMatches[1]) bits[0] = 1;
		if(aroundMatches[5]) bits[1] = 1;
		if(aroundMatches[7]) bits[2] = 1;
		if(aroundMatches[3]) bits[3] = 1;
		int total = 0, value = 1;
		for(int num: bits) {
			total += num * value;
			value *= 2;
		}
		if(total > 0) indexes.add(total+3); // don't care to add if all zeros, because then it's just blank. Also, the +3 is to skip past the first 4 sprites, which are the corners (we add 3 instead of 4 because total will start at 1 rather than 0).
		// four corners
		if(aroundMatches[2] && bits[0] == 0 && bits[1] == 0) indexes.add(0);
		if(aroundMatches[8] && bits[1] == 0 && bits[2] == 0) indexes.add(1);
		if(aroundMatches[6] && bits[2] == 0 && bits[3] == 0) indexes.add(2);
		if(aroundMatches[0] && bits[3] == 0 && bits[0] == 0) indexes.add(3);
		for(Integer idx: indexes)
			sprites.add(aroundType.getProp(TilePropertyType.Render, AnimationProperty.class).getSprite(idx, true, tile));
		
		return sprites;
	}
}
