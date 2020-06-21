package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.world.tile.TileTypeRenderer.RendererBuilder;

class AnimationSetCompiler<T> {
	
	private RenderStyle defaultStyle;
	private final HashMap<T, RenderStyle> customStyles = new HashMap<>();
	
	private final TileAnimationSetFrames<T> animationMap;
	
	private final RendererBuilder builder;
	
	private boolean enabled = true;
	
	AnimationSetCompiler(RendererBuilder builder, RenderStyle defaultStyle, TileAnimationSetFrames<T> animationMap) {
		this.builder = builder;
		this.defaultStyle = defaultStyle;
		this.animationMap = animationMap;
	}
	
	void disable() { enabled = false; }
	
	RenderStyle getStyle(T animId) {
		return customStyles.getOrDefault(animId, defaultStyle);
	}
	
	TileAnimation getAnimation(T animId) {
		return getStyle(animId).getAnimation(animId, animationMap);
	}
	
	HashMap<T, TileAnimation> compileAnimations() {
		if(!enabled) return new HashMap<>(0);
		return animationMap.compileAnimations(this);
	}
	
	public AnimationSetCompiler<T> customStyle(T spriteName, RenderStyle customStyle) {
		customStyles.put(spriteName, customStyle);
		return this;
	}
	
	public RendererBuilder setDefaultStyle(RenderStyle style) {
		defaultStyle = style;
		return back();
	}
	
	/*public RendererBuilder last(String spriteName, RenderStyle customStyle) {
		customStyles.put(spriteName, customStyle);
		return RendererBuilder.this;
	}*/
	
	public RendererBuilder back() { return builder; }
}
