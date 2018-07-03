package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.world.Point;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.Animation;
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
	public Array<Animation<TextureHolder>> getOverlapSprites(EnumSet<RelPos> ovLayout) {
		Array<Animation<TextureHolder>> animations = new Array<>();
		
		if(renderStyle == null && overrides.size() == 0)
			return animations;
		
		// if(typeToOverlap.compareTo(tileType) >= 0)
		// 	return animations; // doesn't overlap
		
		Array<Integer> indexes = new Array<>(Integer.class);
		
		int[] bits = new int[4];
		if(ovLayout.contains(RelPos.LEFT))   bits[0] = 1; // 1
		if(ovLayout.contains(RelPos.TOP))    bits[1] = 1; // 5
		if(ovLayout.contains(RelPos.RIGHT))  bits[2] = 1; // 7
		if(ovLayout.contains(RelPos.BOTTOM)) bits[3] = 1; // 3
		int total = 0, value = 1;
		for(int num: bits) {
			total += num * value;
			value *= 2;
		}
		if(total > 0) indexes.add(total+3); // don't care to add if all zeros, because then it's just blank. Also, the +3 is to skip past the first 4 sprites, which are the corners (we add 3 instead of 4 because total will start at 1 rather than 0).
		// four corners; NOTE, the artist should work on the corner sprites in one tile-sized image, to make sure that they only use a quarter of it at absolute most.
		if(ovLayout.contains(RelPos.TOP_LEFT)     && bits[0] == 0 && bits[1] == 0) indexes.add(0); // 2
		if(ovLayout.contains(RelPos.TOP_RIGHT)    && bits[1] == 0 && bits[2] == 0) indexes.add(1); // 8
		if(ovLayout.contains(RelPos.BOTTOM_RIGHT) && bits[2] == 0 && bits[3] == 0) indexes.add(2); // 6
		if(ovLayout.contains(RelPos.BOTTOM_LEFT)  && bits[3] == 0 && bits[0] == 0) indexes.add(3); // 0
		for(Integer idx: indexes) {
			RenderStyle renderStyle = overrides.getOrDefault(idx, this.renderStyle);
			if(renderStyle == null)
				continue;
			animations.add(renderStyle.getAnimation(tileAnimations.get(type).get((idx < 10 ? "0" : "") + idx)));
		}
		
		return animations;
	}
	
	
	public static EnumMap<TileTypeEnum, EnumSet<RelPos>> mapTileTypesAround(@NotNull Tile tile) { return mapTileTypesAround(tile, true); }
	// Exclude covered means that tile types beneath ground layers won't be included.
	// TODO make "include covered" mode where it checks through the ground layer's datamap to get the TileLayers underneath. I'll probably have a DataTag for a TileStack, that ground tiles have. This will contain the tiles up until another ground type, which has a datamap... etc.
	private static EnumMap<TileTypeEnum, EnumSet<RelPos>> mapTileTypesAround(@NotNull Tile tile, boolean excludeCovered) {
		EnumMap<TileTypeEnum, EnumSet<RelPos>> typeMap = new EnumMap<>(TileTypeEnum.class);
		
		HashSet<Tile> aroundTiles = tile.getAdjacentTiles(true);
		
		for(Tile aroundTile: aroundTiles) {
			// TO-DO in the future, I'll fetch a TileStack and just get the root from it.
			// but... there are non-ground tiles which can be opaque, so I can't base it off that.
			// FIXME I'm just going to assume that I want to go through all tiles. This has yet to be figured out / decided on.
			TileType[] types = aroundTile.getTypeStack().getTypes();
			
			Point thisPos = tile.getLocation();
			Point otherPos = aroundTile.getLocation();
			RelPos tilePos = RelPos.get(otherPos.x - thisPos.x, otherPos.y - thisPos.y);
			
			for(TileType tileType: types) {
				typeMap.putIfAbsent(tileType.getEnumType(), EnumSet.noneOf(RelPos.class));
				typeMap.get(tileType.getEnumType()).add(tilePos);
			}
		}
		
		return typeMap;
	}
}
