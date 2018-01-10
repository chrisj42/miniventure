package miniventure.game;

import java.util.HashMap;

import miniventure.game.screen.GameScreen;
import miniventure.game.screen.MainMenuScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class GameCore extends Game {
	
	public static final Version VERSION = new Version("1.0.6");
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 450;
	private static final long START_TIME = System.nanoTime();
	
	public static TextureAtlas entityAtlas, tileAtlas, tileConnectionAtlas;
	
	private static TextureAtlas iconAtlas;
	public static HashMap<String, TextureRegion> icons = new HashMap<>();
	
	private SpriteBatch batch;
	private BitmapFont font; // this is stored here because it is a really good idea to reuse objects where ever possible; and don't repeat instantiations, aka make a font instance in two classes when the fonts are the same.
	
	private static GameScreen gameScreen; // Screens ought to be disposed, but aren't automatically disposed; so we need to do it ourselves.
	
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
		
		this.setScreen(new MainMenuScreen(this));
	}
	
	public static GameScreen getGameScreen() { return gameScreen; }
	
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
	
	public SpriteBatch getBatch() {
		return batch;
	}
	
	public BitmapFont getFont() {
		return font;
	}
	
	public void setGameScreen(GameScreen gameScreen) {
		this.gameScreen = gameScreen;
	}
	
	public static float getElapsedProgramTime() {
		return (System.nanoTime() - START_TIME)/1E9f;
	}
	
	@Nullable
	public static Class getDirectSubclass(Class superClass, Class target) {
		Array<Class> parents = new Array<>(target.getInterfaces());
		Class targetSuper = target.getSuperclass();
		
		if(parents.contains(superClass, false) || targetSuper.equals(superClass))
			return target;
		
		if(targetSuper == Object.class && parents.size == 0)
			return null;
		
		if(targetSuper != Object.class) {
			Class sub = getDirectSubclass(superClass, targetSuper);
			if(sub != null) return sub;
		}
		
		// go through array
		for(Class parent: parents) {
			Class sub = getDirectSubclass(superClass, parent);
			if(sub != null)
				return sub;
		}
		
		return null;
	}
	
	public static void writeOutlinedText(BitmapFont font, SpriteBatch batch, String text, float x, float y) {
		writeOutlinedText(font, batch, text, x, y, Color.WHITE, Color.BLACK);
	}
	public static void writeOutlinedText(BitmapFont font, SpriteBatch batch, String text, float x, float y, Color center, Color outline) {
		font.setColor(outline);
		font.draw(batch, text, x-1, y-1);
		font.draw(batch, text, x-1, y+1);
		font.draw(batch, text, x+1, y-1);
		font.draw(batch, text, x+1, y+1);
		font.setColor(center);
		font.draw(batch, text, x, y);
	}
	
	public static String toTitleCase(String string) {
		String[] words = string.split(" ");
		for(int i = 0; i < words.length; i++) {
			if(words[i].length() == 0) continue;
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}
		
		return String.join(" ", words);
	}
}
