package miniventure.game.screen;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

public class RespawnScreen implements Screen {
	
	private final GameCore game = GameCore.getGame();
	
	private GameScreen gameScreen;
	
	//private OrthographicCamera camera;
	
	public RespawnScreen(GameScreen prev) {
		this.gameScreen = prev;
	}
	
	@Override
	public void show() {
		
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		game.getBatch().begin();
		
		MyUtils.drawTextCentered(GameCore.getFont(), game.getBatch(),"You Died! Click or press space to respawn.", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		game.getBatch().end();
		
		if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
			game.setScreen(gameScreen);
	}
	
	@Override
	public void resize(int width, int height) {
		
	}
	
	@Override
	public void pause() {
		
	}
	
	@Override
	public void resume() {
		
	}
	
	@Override
	public void hide() {
		
	}
	
	@Override
	public void dispose() {
		
	}
}
