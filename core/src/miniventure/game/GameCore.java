package miniventure.game;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.Version;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.Nullable;

public class GameCore extends ApplicationAdapter {
	
	public static final Version VERSION = new Version("1.2.4");
	
	public static final int DEFAULT_SCREEN_WIDTH = 800;
	public static final int DEFAULT_SCREEN_HEIGHT = 450;
	
	private static final long START_TIME = System.nanoTime();
	
	public static TextureAtlas entityAtlas, tileAtlas, tileConnectionAtlas; // tile overlap atlas not needed b/c the overlap sprite layout is simple enough to code; it goes in binary. However, the tile connection sprite layout is more complicated, so a map is needed to compare against.
	
	private static TextureAtlas iconAtlas;
	public static final HashMap<String, TextureRegion> icons = new HashMap<>();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	private static LevelManager world;
	private static GameScreen gameScreen;
	
	public static final InputHandler input = new InputHandler();
	
	private static SpriteBatch batch;
	private static BitmapFont font; // this is stored here because it is a really good idea to reuse objects where ever possible; and don't repeat instantiations, aka make a font instance in two classes when the fonts are the same.
	private static GlyphLayout layout;
	private static Skin skin;
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		entityAtlas = new TextureAtlas("sprites/entities.txt");
		tileAtlas = new TextureAtlas("sprites/tiles.txt");
		tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
		iconAtlas = new TextureAtlas("sprites/icons.txt");
		
		batch = new SpriteBatch();
		font = new BitmapFont(); // uses libGDX's default Arial font
		font.setColor(Color.WHITE);
		layout = new GlyphLayout(font, "");
		skin = new Skin(Gdx.files.internal("skins/visui/uiskin.json"));
		
		for(AtlasRegion region: iconAtlas.getRegions())
			icons.put(region.name, region);
		
		world = new LevelManager();
		gameScreen = new GameScreen();
		setScreen(new MainMenu());
	}
	
	@Override
	public void render() {
		try {
			if (world.worldLoaded())
				world.updateAndRender(gameScreen, menuScreen);
			
			hasMenu = menuScreen != null;
			if (menuScreen != null) {
				menuScreen.act();
				menuScreen.draw();
			}
		} catch(Throwable t) {
			StringWriter string = new StringWriter();
			PrintWriter printer = new PrintWriter(string);
			t.printStackTrace(printer);
			
			JTextArea errorDisplay = new JTextArea(string.toString());
			errorDisplay.setEditable(false);
			JOptionPane.showMessageDialog(null, errorDisplay, "An error has occurred", JOptionPane.ERROR_MESSAGE);
			
			throw t;
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
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		skin.dispose();
		
		if(menuScreen != null)
			menuScreen.dispose();
		if(gameScreen != null)
			gameScreen.dispose();
		
		entityAtlas.dispose();
		tileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null)
			gameScreen.resize(width, height);
		if(menuScreen != null)
			menuScreen.getViewport().update(width, height, true);
	}
	
	
	public static GlyphLayout getTextLayout(String text) {
		layout.setText(font, text);
		return layout;
	}
	
	@Nullable static MenuScreen getScreen() { return menuScreen; }
	
	public static Skin getSkin() { return skin; }
	
	public static LevelManager getWorld() { return world; }
	
	public static SpriteBatch getBatch() { return batch; }
	
	public static BitmapFont getFont() { return font; }
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
}
