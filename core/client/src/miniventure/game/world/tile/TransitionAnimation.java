package miniventure.game.world.tile;

import miniventure.game.world.tile.TileTypeToAnimationMap.StringTileTypeToAnimationMap;

import org.jetbrains.annotations.NotNull;

class TransitionAnimation {
	
	static final TileTypeToAnimationMap<String> tileAnimations = new StringTileTypeToAnimationMap();
	
	private final TileTypeEnum tileType;
	private final String name;
	private final RenderStyle renderStyle;
	
	TransitionAnimation(@NotNull TileTypeEnum tileType, String name, RenderStyle renderStyle) {
		this.tileType = tileType;
		this.name = name;
		this.renderStyle = renderStyle;
	}
	
	String getName() { return name; }
	
	// we specifically don't want to cache this because otherwise the animation wouldn't reset; it needs to be fresh every call.
	TileAnimation getAnimation() {
		return renderStyle.getAnimation(tileType, name, tileAnimations, "transition");
	}
}
