package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.world.Point;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class OverhangManager {
	
	// the main difference between overlap and overhang is that overhang sprites always render before the tile they are on top of, because since we are rendering by layer, the ground will have already rendered, and overhang will only render on non-solid or non-vertical sprites; anything solid and vertical makes for an overlap sprite instead. 
	
	static final EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	public static final OverhangManager NONE(@NotNull TileTypeEnum type) {
		return new OverhangManager(type);
	}
	
	private final TileTypeEnum type;
	private final RenderStyle renderStyle;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	public OverhangManager(@NotNull TileTypeEnum type, @NotNull RenderStyle renderStyle) {
		this.type = type;
		this.renderStyle = renderStyle;
	}
	private OverhangManager(@NotNull TileTypeEnum type) {
		this.type = type;
		renderStyle = null;
	}
	
	public OverhangManager overrideSprite(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	/// Boolean[] says if the overlappingType is present in each of the surrounding tiles.
	// don't call this method with a typeToOverlap that this overlappingType isn't meant to overlap.
	public ArrayList<TileAnimation<TextureHolder>> getOverhangSprites(EnumSet<RelPos> ovLayout) {
		ArrayList<TileAnimation<TextureHolder>> animations = new ArrayList<>();
		
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
			if(renderStyle != null)
				animations.add(renderStyle.getAnimation(type, (idx<10?"0":"")+idx, tileAnimations));
		}
		
		return animations;
	}
}
