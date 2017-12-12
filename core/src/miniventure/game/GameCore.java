package miniventure.game;

import miniventure.game.screen.GameScreen;
import miniventure.game.screen.MainMenuScreen;
import miniventure.game.world.entity.mob.MobAnimationController;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class GameCore extends Game {
	
	public static final Version VERSION = new Version("1.0.2");
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 450;
	public static TextureAtlas tileAtlas;
	private static final long START_TIME = System.nanoTime();
	
	private SpriteBatch batch;
	private BitmapFont font; // this is stored here because it is a really good idea to reuse objects where ever possible; and don't repeat instantiations, aka make a font instance in two classes when the fonts are the same.
	
	private GameScreen gameScreen; // Screens ought to be disposed, but aren't automatically disposed; so we need to do it ourselves.
	
	@Override
	public void create () {
		tileAtlas = new TextureAtlas("sprites/tiles.txt");
		batch = new SpriteBatch();
		font = new BitmapFont(); // uses libGDX's default Arial font
		
		this.setScreen(new MainMenuScreen(this));
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		if(gameScreen != null)
			gameScreen.dispose();
		
		tileAtlas.dispose();
		MobAnimationController.disposeTextures();
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
}
