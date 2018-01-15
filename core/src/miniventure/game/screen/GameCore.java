package miniventure.game.screen;

import java.util.HashMap;

import miniventure.game.util.Version;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameCore extends Game {
	
	public static final Version VERSION = new Version("1.1.1");
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 450;
	private static final long START_TIME = System.nanoTime();
	
	public static TextureAtlas entityAtlas, tileAtlas, tileConnectionAtlas;
	
	private static TextureAtlas iconAtlas;
	public static HashMap<String, TextureRegion> icons = new HashMap<>();
	
	private SpriteBatch batch;
	private static BitmapFont font; // this is stored here because it is a really good idea to reuse objects where ever possible; and don't repeat instantiations, aka make a font instance in two classes when the fonts are the same.
	
	private static GameScreen gameScreen; // Screens ought to be disposed, but aren't automatically disposed; so we need to do it ourselves.
	
	private static GameCore game;
	
	@Override
	public void create () {
		entityAtlas = new TextureAtlas("sprites/entities.txt");
		tileAtlas = new TextureAtlas("sprites/tiles.txt");
		tileConnectionAtlas = new TextureAtlas("sprites/tileconnectmap.txt");
		iconAtlas = new TextureAtlas("sprites/icons.txt");
		batch = new SpriteBatch();
		font = new BitmapFont(); // uses libGDX's default Arial font
		
		for(AtlasRegion region: iconAtlas.getRegions())
			icons.put(region.name, region);
		
		game = this;
		
		this.setScreen(new MainMenuScreen());
	}
	
	public static GameCore getGame() { return game; }
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		if(gameScreen != null)
			gameScreen.dispose();
		
		entityAtlas.dispose();
		tileAtlas.dispose();
		tileConnectionAtlas.dispose();
		iconAtlas.dispose();
	}
	
	public SpriteBatch getBatch() { return batch; }
	
	public static BitmapFont getFont() { return font; }
	
	public void setGameScreen(GameScreen gameScreen) { GameCore.gameScreen = gameScreen; }
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
	
}
