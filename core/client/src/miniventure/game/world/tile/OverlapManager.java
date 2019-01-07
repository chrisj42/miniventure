package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.InstanceCounter;
import miniventure.game.util.RelPos;
import miniventure.game.world.Point;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class OverlapManager {
	
	static final EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	public static final OverlapManager NONE(@NotNull TileTypeEnum type) {
		return new OverlapManager(type);
	}
	
	private final TileTypeEnum type;
	private final RenderStyle renderStyle;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	public OverlapManager(@NotNull TileTypeEnum type, @NotNull RenderStyle renderStyle) {
		this.type = type;
		this.renderStyle = renderStyle;
	}
	private OverlapManager(@NotNull TileTypeEnum type) {
		this.type = type;
		renderStyle = null;
	}
	
	public OverlapManager overrideSprite(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	/// Boolean[] says if the overlappingType is present in each of the surrounding tiles.
	// don't call this method with a typeToOverlap that this overlappingType isn't meant to overlap.
	public ArrayList<TileAnimation<TextureHolder>> getOverlapSprites(InstanceCounter<RelPos> ovLayout) { return getOverlapSprites(ovLayout, false); }
	public ArrayList<TileAnimation<TextureHolder>> getOverlapSprites(InstanceCounter<RelPos> ovLayout, boolean checkCount) {
		ArrayList<TileAnimation<TextureHolder>> animations = new ArrayList<>();
		
		if(renderStyle == null && overrides.size() == 0)
			return animations;
		
		// if(typeToOverlap.compareTo(tileType) >= 0)
		// 	return animations; // doesn't overlap
		
		Array<Integer> indexes = new Array<>(Integer.class);
		
		int[] bits = new int[4];
		if(check(ovLayout, RelPos.LEFT, checkCount))   bits[0] = 1; // 1
		if(check(ovLayout, RelPos.TOP, checkCount))    bits[1] = 1; // 5
		if(check(ovLayout, RelPos.RIGHT, checkCount))  bits[2] = 1; // 7
		if(check(ovLayout, RelPos.BOTTOM, checkCount)) bits[3] = 1; // 3
		int total = 0, value = 1;
		for(int num: bits) {
			total += num * value;
			value *= 2;
		}
		if(total > 0) indexes.add(total+3); // don't care to add if all zeros, because then it's just blank. Also, the +3 is to skip past the first 4 sprites, which are the corners (we add 3 instead of 4 because total will start at 1 rather than 0).
		// four corners; NOTE, the artist should work on the corner sprites in one tile-sized image, to make sure that they only use a quarter of it at absolute most.
		if(check(ovLayout, RelPos.TOP_LEFT, checkCount)     && bits[0] == 0 && bits[1] == 0) indexes.add(0); // 2
		if(check(ovLayout, RelPos.TOP_RIGHT, checkCount)    && bits[1] == 0 && bits[2] == 0) indexes.add(1); // 8
		if(check(ovLayout, RelPos.BOTTOM_RIGHT, checkCount) && bits[2] == 0 && bits[3] == 0) indexes.add(2); // 6
		if(check(ovLayout, RelPos.BOTTOM_LEFT, checkCount)  && bits[3] == 0 && bits[0] == 0) indexes.add(3); // 0
		for(Integer idx: indexes) {
			RenderStyle renderStyle = overrides.getOrDefault(idx, this.renderStyle);
			if(renderStyle != null)
				animations.add(renderStyle.getAnimation(type, (idx<10?"0":"")+idx, tileAnimations));
		}
		
		return animations;
	}
	
	private boolean check(InstanceCounter<RelPos> ovLayout, RelPos rp, boolean checkCount) {
		return ovLayout.contains(rp) && (!checkCount || ovLayout.get(rp) > ovLayout.get(RelPos.CENTER));
	}
	
	
	public static EnumMap<TileTypeEnum, EnumSet<RelPos>> mapTileTypesAround(@NotNull ClientTile tile) { return mapTileTypesAround(tile, true); }
	/** @noinspection SameParameterValue*/ // Exclude covered means that tile types beneath ground layers won't be included.
	private static EnumMap<TileTypeEnum, EnumSet<RelPos>> mapTileTypesAround(@NotNull ClientTile tile, boolean excludeCovered) {
		EnumMap<TileTypeEnum, EnumSet<RelPos>> typeMap = new EnumMap<>(TileTypeEnum.class);
		
		HashSet<Tile> aroundTiles = tile.getAdjacentTiles(true);
		
		for(Tile aroundTile: aroundTiles) {
			List<ClientTileType> types = ((ClientTileStack)aroundTile.getTypeStack()).getTypes(!excludeCovered);
			
			Point thisPos = tile.getLocation();
			Point otherPos = aroundTile.getLocation();
			RelPos tilePos = RelPos.get(otherPos.x - thisPos.x, otherPos.y - thisPos.y);
			
			for(ClientTileType tileType: types) {
				typeMap.putIfAbsent(tileType.getTypeEnum(), EnumSet.noneOf(RelPos.class));
				typeMap.get(tileType.getTypeEnum()).add(tilePos);
			}
		}
		
		return typeMap;
	}
}
