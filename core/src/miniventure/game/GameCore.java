package miniventure.game;

import java.util.HashMap;

import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.Version;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.Nullable;

public class GameCore extends ApplicationAdapter {
	
	public static final Version VERSION = new Version("1.1.1");
	
	//public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 450;
	private static final long START_TIME = System.nanoTime();
	
	public static TextureAtlas entityAtlas, tileAtlas, tileConnectionAtlas; // tile overlap atlas not needed b/c the overlap sprite layout is simple enough to code; it goes in binary. However, the tile connection sprite layout is more complicated, so a map is needed to compare against.
	
	private static TextureAtlas iconAtlas;
	public static final HashMap<String, TextureRegion> icons = new HashMap<>();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	private static LevelManager world;
	private static GameScreen gameScreen;
	
	private static SpriteBatch batch;
	private static BitmapFont font; // this is stored here because it is a really good idea to reuse objects where ever possible; and don't repeat instantiations, aka make a font instance in two classes when the fonts are the same.
	
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		entityAtlas = new TextureAtlas("sprites/entities.txt");
		tileAtlas = new TextureAtlas("sprites/tiles.txt");
		tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
		iconAtlas = new TextureAtlas("sprites/icons.txt");
		
		batch = new SpriteBatch();
		font = new BitmapFont(); // uses libGDX's default Arial font
		
		for(AtlasRegion region: iconAtlas.getRegions())
			icons.put(region.name, region);
		
		world = new LevelManager();
		gameScreen = new GameScreen();
		setScreen(new MainMenu());
	}
	
	@Override
	public void render() {
		if(world.worldLoaded())
			world.render(gameScreen, menuScreen);
		
		hasMenu = menuScreen != null;
		if(menuScreen != null) {
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
		Gdx.input.setInputProcessor(menuScreen);
	}
	
	@Nullable
	static MenuScreen getScreen() { return menuScreen; }
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		
		entityAtlas.dispose();
		tileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null)
			gameScreen.resize(width, height);
	}
	
	public static LevelManager getWorld() { return world; }
	
	public static SpriteBatch getBatch() { return batch; }
	
	public static BitmapFont getFont() { return font; }
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
}
