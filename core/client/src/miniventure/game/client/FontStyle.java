package miniventure.game.client;

import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import org.jetbrains.annotations.NotNull;

public enum FontStyle {
	
	OnlyResize(style -> {}),
	
	TextField(style -> {
		style.font.size = 15;
		style.font.color = Color.WHITE;
	}),
	
	Default(style -> {
		FreeTypeFontParameter params = style.font;
		params.size = 15;
		params.color = Color.WHITE;
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		params.spaceX = -1;
		//params.magFilter = TextureFilter.Linear;
		params.shadowOffsetX = 1;
		params.shadowOffsetY = 1;
		params.shadowColor = Color.BLACK;
	}),
	
	KeepSize(Default.change(false)),
	
	KeepSizeScaled(KeepSize.change(KeepSize.get().font.size * Tile.SCALE)),
	
	CrafterHeader(KeepSize.change(12)),
	
	StockHeader(KeepSize.change(14)),
	
	CostHeader(KeepSize.change(18));
	
	private final FontStyler styler;
	
	FontStyle(FontStyler styler) {
		this.styler = styler;
	}
	
	private FontStyler change(int fontSize) { return styler.change(fontSize); }
	private FontStyler change(boolean resize) { return styler.change(resize); }
	private FontStyler change(int fontSize, boolean resize) { return styler.change(fontSize, resize); }
	
	static class StyleData {
		FreeTypeFontParameter font = new FreeTypeFontParameter();
		boolean resize = true;
	}
	
	public StyleData get() {
		StyleData style = new StyleData();
		styler.edit(style);
		return style;
	}
	
	private interface FontStyler {
		
		default FontStyler append(@NotNull FontStyler other) {
			return style -> {
				edit(style);
				other.edit(style);
			};
		}
		
		void edit(StyleData style);
		
		default FontStyler change(int fontSize) { return append(style -> style.font.size = fontSize); }
		default FontStyler change(boolean resize) { return append(style -> style.resize = resize); }
		default FontStyler change(int fontSize, boolean resize) {
			return append(style -> {
				style.font.size = fontSize;
				style.resize = resize;
			});
		}
	}
}
