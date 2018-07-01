package miniventure.game.world.tile.newtile.render;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.tile.newtile.TileType;

import com.badlogic.gdx.utils.Array;

public class OverlapManager {
	
	static EnumMap<TileType, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileType.class);
	
	public static final OverlapManager NONE = new OverlapManager();
	
	private final RenderStyle renderStyle;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	public OverlapManager(RenderStyle renderStyle) {
		this.renderStyle = renderStyle;
	}
	private OverlapManager() {
		renderStyle = null;
	}
	
	public OverlapManager overrideSprite(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
}
