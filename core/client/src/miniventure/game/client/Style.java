package miniventure.game.client;

import miniventure.game.GameCore;
import miniventure.game.screen.util.ColorRect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.LinkLabel.LinkLabelStyle;
import com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle;

public enum Style {
	
	TextFieldFont(skin -> {
		skin.addFont(params -> {
			params.size = 15;
			params.color = Color.WHITE;
		});
		skin.add(ResizeText.RESIZE);
	}),
	
	Default((delegate, skin) -> {
		// add all default styles
		
		// in terms of font size, the size for the default window size will be specified here, and then on a window size change, the new font sizes will be calculated from the smallest dimension to preserve the ratio of font dim to window dim.
		
		delegate.addFont(params -> {
			params.size = 15;
			params.color = Color.WHITE;
			params.borderColor = Color.BLACK;
			params.borderWidth = 1;
			params.spaceX = -1;
			//params.magFilter = TextureFilter.Linear;
			params.shadowOffsetX = 1;
			params.shadowOffsetY = 1;
			params.shadowColor = Color.BLACK;
		});
		
		delegate.add(ResizeText.RESIZE);
		// skin.add(new LabelStyle(ClientCore.getFont(DefaultLabelFont), null));
	}),
	
	/* -- CRAFTING SCREEN -- */
	
	// the crafter name label at the top of the crafting screen
	CrafterHeader(skin -> {
		skin.addFont(params -> {
			Default.as(FontParameterLoader.class).load(params);
			params.size = 12;
		});
		skin.add(ResizeText.CONSTANT);
	}),
	
	// cost stock column header label
	StockHeader(skin -> {
		skin.addFont(params -> {
			Default.as(FontParameterLoader.class).load(params);
			params.size = 14;
		});
		skin.add(ResizeText.CONSTANT);
	}),
	
	// cost "requirements" column header label
	CostHeader(skin -> {
		skin.addFont(params -> {
			Default.as(FontParameterLoader.class).load(params);
			params.size = 18;
		});
		skin.add(ResizeText.CONSTANT);
	}),
	
	KeepSize(skin -> {
		skin.add(ResizeText.CONSTANT);
	}),
	
	LinkLabel(skin -> {
		skin.add(new LinkLabelStyle(ClientCore.getFont(), Color.SKY, new ColorRect(Color.SKY)));
	});
	
	public enum ResizeText {
		RESIZE, CONSTANT;
	}
	
	private final StyleLoader initializer;
	private final String name;
	private final SkinDelegate skinDelegate = new SkinDelegate();
	
	Style(SimpleStyleLoader initializer) { this((StyleLoader)initializer); }
	Style(StyleLoader initializer) {
		this.initializer = initializer;
		char[] name = name().toCharArray();
		name[0] = Character.toLowerCase(name[0]);
		this.name = new String(name);
	}
	
	public String getName() { return name; }
	
	private void load(Skin skin) {
		skinDelegate.skin = skin;
		initializer.load(skinDelegate, skin);
		skinDelegate.skin = null;
	}
	
	public <T> T as(Class<T> type) {
		if(this == Default)
			return VisUI.getSkin().get(name, type);
		else {
			T val = VisUI.getSkin().optional(name, type);
			if(val == null)
				return Default.as(type);
			return val;
		}
	}
	
	@FunctionalInterface
	private interface StyleLoader {
		void load(SkinDelegate delegate, Skin skin);
	}
	@FunctionalInterface
	private interface SimpleStyleLoader extends StyleLoader {
		void load(SkinDelegate delegate);
		
		@Override
		default void load(SkinDelegate delegate, Skin skin) { load(delegate); }
	}
	
	private class SkinDelegate {
		private Skin skin;
		
		void add(Object obj) { add(obj, obj.getClass()); }
		void add(Object obj, Class type) {
			skin.add(name, obj, type);
		}
		
		void addFont(FontParameterLoader loader) {
			add(loader, FontParameterLoader.class);
			// we don't need to add the actual font because there's already a default and it's going to be overridden very quickly after instantiation anyway.
			// add(ClientCore.getFont(Style.this)); // this adds an actual BitmapFont. It won't be used for long (font will be reset very quickly)
		}
	}
	
	static void loadStyles(Skin skin) {
		for(Style style: Style.values())
			style.load(skin);
	}
	
	// fonts will be specified through instances of this interface. This is fetched and called by ClientCore.getFont(Style).
	// this should create a new FreeTypeFontParameter instance and fill out the fields as needed.
	@FunctionalInterface
	public interface FontParameterLoader {
		default FreeTypeFontParameter get() {
			FreeTypeFontParameter params = new FreeTypeFontParameter();
			load(params);
			return params;
		}
		void load(FreeTypeFontParameter params);
	}
	
	public static class StyleNotFoundException extends RuntimeException {
		public StyleNotFoundException(String msg) {
			super(msg);
		}
		
		public StyleNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public StyleNotFoundException(Throwable cause) {
			super(cause);
		}
	}
}
