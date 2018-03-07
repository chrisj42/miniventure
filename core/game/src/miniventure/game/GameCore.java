package miniventure.game;

import java.lang.reflect.Field;
import java.util.HashMap;

import miniventure.game.screen.MenuScreen;
import miniventure.game.util.Version;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import org.jetbrains.annotations.Nullable;

public class GameCore {
	
	public static final Version VERSION = new Version("1.3.2");
	
	public static final int DEFAULT_SCREEN_WIDTH = 800;
	public static final int DEFAULT_SCREEN_HEIGHT = 450;
	
	private static final long START_TIME = System.nanoTime();
	
	public static TextureAtlas entityAtlas = new TextureAtlas(), tileAtlas = new TextureAtlas(), tileConnectionAtlas = new TextureAtlas(); // tile overlap atlas not needed b/c the overlap sprite layout is simple enough to code; it goes in binary. However, the tile connection sprite layout is more complicated, so a map is needed to compare against.
	
	private static TextureAtlas iconAtlas;
	public static final HashMap<String, TextureRegion> icons = new HashMap<>();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	
	public static final InputHandler input = new InputHandler();
	
	private static SpriteBatch batch;
	private static FreeTypeFontGenerator fontGenerator;
	private static GlyphLayout layout;
	private static Skin skin;
	
	public static void initGdx() {
		entityAtlas = new TextureAtlas("sprites/entities.txt");
		tileAtlas = new TextureAtlas("sprites/tiles.txt");
		tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
		iconAtlas = new TextureAtlas("sprites/icons.txt");
		
		batch = new SpriteBatch();
		//font = new BitmapFont(); // uses libGDX's default Arial font
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
		layout = new GlyphLayout();
		skin = new Skin(Gdx.files.internal("skins/visui/uiskin.json"));
		
		for(AtlasRegion region: iconAtlas.getRegions())
			icons.put(region.name, region);
	}
	
	public static void updateAndRender(WorldManager world) {
		input.update();
		
		if (world.worldLoaded())
			world.update(Gdx.graphics.getDeltaTime());
		
		hasMenu = menuScreen != null;
		if (menuScreen != null) {
			menuScreen.act();
			menuScreen.draw();
		}
	}
	
	public static boolean hasMenu() { return hasMenu; }
	
	public static void setScreen(@Nullable MenuScreen screen) {
		if(screen == null && menuScreen != null)
			menuScreen.dispose();
		else if(screen != null)
			screen.setParent(menuScreen);
		
		System.out.println("setting screen to " + screen);
		
		menuScreen = screen;
		Gdx.input.setInputProcessor(menuScreen == null ? input : menuScreen);
		input.reset();
	}
	
	public static void dispose () {
		batch.dispose();
		skin.dispose();
		fontGenerator.dispose();
		for(BitmapFont font: generatedFonts.values())
			font.dispose();
		
		if(menuScreen != null)
			menuScreen.dispose();
		
		entityAtlas.dispose();
		tileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
	}
	
	
	public static GlyphLayout getTextLayout(String text) {
		layout.setText(getFont(), text);
		return layout;
	}
	
	@Nullable
	public static MenuScreen getScreen() { return menuScreen; }
	
	public static Skin getSkin() { return skin; }
	
	public static SpriteBatch getBatch() { return batch; }
	
	private static class HashableFontParameter {
		private final FreeTypeFontParameter param;
		public HashableFontParameter(FreeTypeFontParameter param) {
			this.param = param;
		}
		
		@Override
		public boolean equals(Object other) {
			if(param == null && other == null) return true;
			if(!(other instanceof FreeTypeFontParameter)) return false;
			
			FreeTypeFontParameter param = (FreeTypeFontParameter) other;
			try {
				for(Field field: FreeTypeFontParameter.class.getDeclaredFields()) {
					Object obj1 = field.get(this.param);
					Object obj2 = field.get(param);
					if((obj1 == null) != (obj2 == null)) return false;
					if(obj1 == null) continue;
					if(!obj1.equals(obj2))
						return false;
				}
			} catch(IllegalAccessException e) {
				e.printStackTrace();
				return param.equals(this.param);
			}
			
			return true;
		}
		
		@Override
		public int hashCode() {
			int hash = 1;
			try {
				Field[] fields = FreeTypeFontParameter.class.getDeclaredFields();
				for(int i = 0; i < fields.length; i++) {
					Object obj = fields[i].get(param);
					hash += 17 * i + 31 * (obj==null?1:obj.hashCode());
				}
			} catch(IllegalAccessException e) {
				e.printStackTrace();
				return param.hashCode();
			}
			
			return hash;
		}
	}
	
	private static final HashMap<HashableFontParameter, BitmapFont> generatedFonts = new HashMap<>();
	private static final FreeTypeFontParameter defaultFont = getDefaultFontConfig();
	
	public static FreeTypeFontParameter getDefaultFontConfig() {
		FreeTypeFontParameter params = new FreeTypeFontParameter();
		params.size = 15;
		params.color = Color.WHITE;
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		return params;
	}
	
	public static BitmapFont getFont() { return getFont(defaultFont); }
	public static BitmapFont getFont(FreeTypeFontParameter params) {
		HashableFontParameter hashed = new HashableFontParameter(params);
		if(!generatedFonts.containsKey(hashed))
			generatedFonts.put(hashed, fontGenerator.generateFont(params));
		
		return generatedFonts.get(hashed);
	}
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
}
