package miniventure.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class GameScreen implements Screen {
	
	private OrthographicCamera camera;
	
	private SpriteBatch batch;
	private TextureAtlas tiles;
	private Sprite tree;
	
	private Rectangle treeBounds;
	
	public GameScreen(GameCore game) {
		batch = game.batch;
		game.gameScreen = this;
		
		tiles = new TextureAtlas("data/tiles.txt");
		tree = tiles.createSprite("tree");
		
		treeBounds = new Rectangle();
		treeBounds.x = MathUtils.random(0, GameCore.SCREEN_WIDTH);
		treeBounds.y = MathUtils.random(0, GameCore.SCREEN_HEIGHT);
		treeBounds.width = tree.getWidth();
		treeBounds.height = tree.getHeight();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
	}
	
	@Override
	public void dispose() {
		tiles.dispose();
	}
	
	@Override
	public void render(float delta) {
		// clears the screen with a green color.
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update(); // update the camera "matrices"
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		batch.draw(tree, treeBounds.x-treeBounds.width/2, treeBounds.y-treeBounds.height/2);
		batch.end();
		
		handleInput();
	}
	
	private void handleInput() {
		if(Gdx.input.isTouched()) { // if a mouse button is pressed...
			/*
				We want to transform the touch/mouse coordinates to our camera's coordinate system.
				This is necessary because the coordinate system used for mouse input might be different than the one we use for objects in our world.
			 */
			
			// get the coordinates as a 3D vector, because OrthographicCamera is actually a 3D camera which takes into account z-coordinates as well.
			Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
			
			// transform these coordinates to our camera's coordinate system (modifies the object)
			camera.unproject(mousePos);
			
			treeBounds.x = mousePos.x;
			treeBounds.y = mousePos.y;
			
			/*
				Note: it is very, very bad to instantiate a new object all the time, such as the Vector3 instance. The reason for this is the garbage collector has to kick in frequently to collect these short-lived objects. On the desktop it's not such a big deal, but other platforms can experience issues. To solve this issue in this particular case, we can simply make mousePos a field of the Game class instead of instantiating it all the time.
			 */
		}
		
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		int speed = 200; // this is technically in units/second.
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) treeBounds.x -= speed * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) treeBounds.x += speed * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) treeBounds.y += speed * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) treeBounds.y -= speed * Gdx.graphics.getDeltaTime();
		treeBounds.x = Math.max(treeBounds.x, 0);
		treeBounds.y = Math.max(treeBounds.y, 0);
		treeBounds.x = Math.min(treeBounds.x, GameCore.SCREEN_WIDTH);
		treeBounds.y = Math.min(treeBounds.y, GameCore.SCREEN_HEIGHT);
	}
	
	@Override public void resize(int width, int height) {}
	
	@Override public void pause() {}
	@Override public void resume() {}
	
	@Override public void show() {}
	@Override public void hide() {}
}
