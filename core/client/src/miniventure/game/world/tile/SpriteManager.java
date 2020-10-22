package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.world.tile.TileTypeRenderer.RendererBuilder;

import org.jetbrains.annotations.NotNull;

public class SpriteManager<T> {
	
	@NotNull
	private final TileTypeEnum tileType;
	private RenderStyle defaultStyle;
	private final HashMap<T, RenderStyle> customStyles = new HashMap<>();
	
	private final TileTypeToAnimationMap<T> animationMap;
	private final String animationMapName;
	
	private SpriteManager(@NotNull TileTypeEnum tileType, RenderStyle defaultStyle, TileTypeToAnimationMap<T> animationMap, String animationMapName) {
		this.tileType = tileType;
		this.defaultStyle = defaultStyle;
		this.animationMap = animationMap;
		this.animationMapName = animationMapName;
	}
	
	public TileAnimation getAnimation(RenderTile tile, T name) {
		return customStyles.getOrDefault(name, defaultStyle).getAnimation(tile, tileType, name, animationMap, animationMapName);
	}
	
	public static class SpriteCompiler<T> {
		
		private final SpriteManager<T> manager;
		private final RendererBuilder builder;
		
		SpriteCompiler(RendererBuilder builder, @NotNull TileTypeEnum tileType, RenderStyle defaultStyle, TileTypeToAnimationMap<T> animationMap, String animationMapName) {
			this.builder = builder;
			manager = new SpriteManager<>(tileType, defaultStyle, animationMap, animationMapName);
			validateStyle(defaultStyle);
		}
		
		SpriteManager<T> getManager() { return manager; }
		
		protected void validateStyle(RenderStyle style) {}
		
		public RendererBuilder setDefaultStyle(RenderStyle style) {
			validateStyle(style);
			manager.defaultStyle = style;
			return builder;
		}
		
		public SpriteCompiler<T> customStyle(T spriteName, RenderStyle customStyle) {
			validateStyle(customStyle);
			manager.customStyles.put(spriteName, customStyle);
			return this;
		}
		
		/*public RendererBuilder last(String spriteName, RenderStyle customStyle) {
			customStyles.put(spriteName, customStyle);
			return RendererBuilder.this;
		}*/
		
		public RendererBuilder back() { return builder; }
	}
}
