package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OverlapManager {
	
	static final EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	private static final HashMap<String, Array<TextureHolder>> dummy = new HashMap<>(0);
	
	@NotNull private final TileTypeEnum type;
	@Nullable private final RenderStyle renderStyle;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	// doesn't check for overlap sprites
	public OverlapManager(@NotNull TileTypeEnum type, @Nullable RenderStyle renderStyle) {
		this.type = type;
		this.renderStyle = renderStyle;
	}
	// checks for overlap sprites; only enables if found 
	public OverlapManager(@NotNull TileTypeEnum type) {
		this.type = type;
		
		if(tileAnimations.getOrDefault(type, dummy).size() > 0)
			renderStyle = RenderStyle.SINGLE_FRAME;
		else
			renderStyle = null;
		
		// if(GameCore.debug && renderStyle != null)
			// System.out.println("overlap sprites found for "+type);
	}
	
	public OverlapManager customStyle(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	/// returns sprites for the stored type assuming it is being overlapped at the given positions.
	public ArrayList<TileAnimation> getOverlapSprites(EnumSet<RelPos> ovLayout) {
		ArrayList<TileAnimation> animations = new ArrayList<>();
		
		if(renderStyle == null && overrides.size() == 0)
			return animations;
		
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
				animations.add(renderStyle.getAnimation(type, (idx<10?"0":"")+idx, tileAnimations, "overlap"));
		}
		
		return animations;
	}
}
