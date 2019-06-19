package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

class TransitionAnimation {
	
	static final EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	private final TileTypeEnum tileType;
	private final String name;
	private final RenderStyle renderStyle;
	
	TransitionAnimation(@NotNull TileTypeEnum tileType, String name, RenderStyle renderStyle) {
		this.tileType = tileType;
		this.name = name;
		this.renderStyle = renderStyle;
	}
	
	String getName() { return name; }
	
	TileAnimation getAnimation() {
		return renderStyle.getAnimation(tileType, name, tileAnimations, "transition");
	}
}
