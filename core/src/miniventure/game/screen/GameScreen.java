package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class GameScreen implements Screen {
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	
	private Player mainPlayer;
	private int curLevel;
	
	public GameScreen(GameCore game) {
		batch = game.getBatch();
		game.setGameScreen(this);
		
		createWorld();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
	}
	
	private void createWorld() {
		Level.resetLevels();
		curLevel = 0;
		
		mainPlayer = new Player();
		mainPlayer.moveTo(GameCore.SCREEN_WIDTH/2, GameCore.SCREEN_HEIGHT/2);
		
		Level.getLevel(curLevel).addEntity(mainPlayer);
	}
	
	@Override
	public void dispose() {}
	
	@Override
	public void render(float delta) {
		// clears the screen with a green color.
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		mainPlayer.checkInput(delta);
		Level.getLevel(curLevel).update(delta);
		
		if(Gdx.input.isKeyJustPressed(Keys.MINUS)) {
			camera.viewportHeight *= 2;
			camera.viewportWidth *= 2;
		}
		if(Gdx.input.isKeyJustPressed(Keys.EQUALS) || Gdx.input.isKeyJustPressed(Keys.PLUS)) {
			camera.viewportHeight /= 2;
			camera.viewportWidth /= 2;
		}
		
		Vector2 playerPos = new Vector2();
		mainPlayer.getBounds().getCenter(playerPos);
		//playerPos.sub(GameCore.SCREEN_WIDTH/2, GameCore.SCREEN_HEIGHT/2);
		camera.position.set(playerPos, camera.position.z);
		camera.update(); // updates the camera "matrices"
		
		Vector3 worldMin = camera.unproject(new Vector3(0, 0, 0));
		Vector3 worldMax = camera.unproject(new Vector3(camera.viewportWidth, camera.viewportHeight, 0));
		Rectangle renderSpace = new Rectangle(worldMin.x, worldMin.y, worldMax.x-worldMin.x, worldMax.y-worldMin.y);
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		Level.getLevel(curLevel).render(renderSpace, batch, delta);
		// TO-DO render GUI here
		batch.end();
	}
	
	@Override public void resize(int width, int height) {}
	
	@Override public void pause() {}
	@Override public void resume() {}
	
	@Override public void show() {}
	@Override public void hide() {}
}
