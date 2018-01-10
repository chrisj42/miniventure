package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenuScreen implements Screen {
	
	private final GameCore game;
	
	private SpriteBatch batch;
	
	private OrthographicCamera camera;
	
	
	public MainMenuScreen(final GameCore game) {
		this.game = game;
		
		batch = game.getBatch();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
	}
	
	@Override
	public void dispose() {}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		game.getFont().draw(batch, "Welcome to Miniventure! You're playing Version " + GameCore.VERSION, 100, 200);
		game.getFont().draw(batch, "Use the arrow keys or the mouse to move, the C key to attack, and the V key to", 100, 150);
		game.getFont().draw(batch, "interact with things and place items.", 100, 130);
		game.getFont().draw(batch, "Use the Q and E keys to cycle through your inventory.", 100, 80);
		game.getFont().draw(batch, "Click anywhere, or press space, to begin.", 100, 30);
		
		batch.end();
		
		if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			game.setScreen(new GameScreen(game));
			dispose();
		}
	}
	
	@Override public void resize(int width, int height) {}
	
	@Override public void pause() {}
	@Override public void resume() {}
	
	@Override public void show() {}
	@Override public void hide() {}
}
