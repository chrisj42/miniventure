package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class OverlapProperty implements TileProperty {
	
	private final boolean overlaps;
	
	private TileType tileType;
	
	OverlapProperty(boolean overlaps) {
		this.overlaps = overlaps;
	}
	
	@Override public void init(@NotNull TileType type) {
		this.tileType = type;
	}
	
	boolean canOverlap() { return overlaps; }
	
	@NotNull
	Array<AtlasRegion> getSprites(Tile tile, TileType aroundType, Boolean[] aroundMatches) {
		Array<AtlasRegion> sprites = new Array<>();
		
		if(!aroundType.getProp(OverlapProperty.class).overlaps) return sprites;
		if(tileType.compareTo(aroundType) >= 0) return sprites;
		
		
		Array<Integer> indexes = new Array<>();
		
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
		
		return sprites;
	}
}
