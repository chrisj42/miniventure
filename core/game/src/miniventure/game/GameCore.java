package miniventure.game;

import java.util.HashMap;

import miniventure.game.texture.TextureAtlasHolder;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.Version;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class GameCore {
	
	public static final Version VERSION = new Version("1.3.6");
	
	public static final int DEFAULT_SCREEN_WIDTH = 800;
	public static final int DEFAULT_SCREEN_HEIGHT = 450;
	
	private static final long START_TIME = System.nanoTime();
	
	public static TextureAtlasHolder entityAtlas;
	public static TextureAtlas tileAtlas = new TextureAtlas(), tileConnectionAtlas = new TextureAtlas(); // tile overlap atlas not needed b/c the overlap sprite layout is simple enough to code; it goes in binary. However, the tile connection sprite layout is more complicated, so a map is needed to compare against.
	
	private static TextureAtlas iconAtlas;
	public static final HashMap<String, TextureHolder> icons = new HashMap<>();
	
	private static SpriteBatch batch;
	private static FreeTypeFontGenerator fontGenerator;
	private static GlyphLayout layout = new GlyphLayout();
	private static Skin skin;
	
	private static boolean initialized = false;
	
	public static void initGdx() {
		if(initialized) return;
		initialized = true;
		entityAtlas = new TextureAtlasHolder(new TextureAtlas("sprites/entities.txt"));
		tileAtlas = new TextureAtlas("sprites/tiles.txt");
		tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
		iconAtlas = new TextureAtlas("sprites/icons.txt");
		
		batch = new SpriteBatch();
		//font = new BitmapFont(); // uses libGDX's default Arial font
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
		skin = new Skin(Gdx.files.internal("skins/visui/uiskin.json"));
		
		//noinspection ConstantConditions
		for(AtlasRegion region: iconAtlas.getRegions())
			icons.put(region.name, new TextureHolder(region));
	}
	
	public static void initNonGdx() {
		if(initialized) return;
		initialized = true;
		// initialize entity atlas and icon atlas, b/c that's what the server needs to determine entity sizes (icons b/c of item entities)
		LwjglNativesLoader.load();
		Gdx.files = new LwjglFiles();
		
		// maybe if I manually created a TextureAtlasData?
		FileHandle spriteFolder = Gdx.files.internal("sprites");
		TextureAtlasData entityData = new TextureAtlasData(spriteFolder.child("entities.txt"), spriteFolder, false);
		TextureAtlasData iconData = new TextureAtlasData(spriteFolder.child("icons.txt"), spriteFolder, false);
		
		entityAtlas = new TextureAtlasHolder(entityData);
		for(Region region: iconData.getRegions())
			icons.put(region.name, new TextureHolder(region));
	}
	
	public static void dispose () {
		batch.dispose();
		skin.dispose();
		fontGenerator.dispose();
		if(defaultFont != null)
			defaultFont.dispose();
		
		entityAtlas.dispose();
		tileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
	}
	
	
	public static GlyphLayout getTextLayout(String text) {
		if(fontGenerator != null)
			layout.setText(getFont(), text);
		return layout;
	}
	
	public static Skin getSkin() { return skin; }
	
	public static SpriteBatch getBatch() { return batch; }
	
	private static BitmapFont defaultFont;
	
	private static FreeTypeFontParameter getDefaultFontConfig() {
		FreeTypeFontParameter params = new FreeTypeFontParameter();
		params.size = 15;
		params.color = Color.WHITE;
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		params.spaceX = -1;
		//params.magFilter = TextureFilter.Linear;
		params.shadowOffsetX = 1;
		params.shadowOffsetY = 1;
		params.shadowColor = Color.BLACK;
		return params;
	}
	
	public static BitmapFont getFont() {
		if(defaultFont == null)
			defaultFont = fontGenerator.generateFont(getDefaultFontConfig());
		
		defaultFont.setColor(Color.WHITE);
		return defaultFont;
	}
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
}
