package miniventure.game;

import miniventure.game.screen.GameScreen;
import miniventure.game.screen.MainMenuScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameCore extends Game {
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 450;
	
	private SpriteBatch batch;
	private BitmapFont font; // this is stored here because it is a really good idea to reuse objects where ever possible; and don't repeat instantiations, aka make a font instance in two classes when the fonts are the same.
	
	private GameScreen gameScreen; // Screens ought to be disposed, but aren't automatically disposed; so we need to do it ourselves.
	
	@Override
	public void create () {
		
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
}
