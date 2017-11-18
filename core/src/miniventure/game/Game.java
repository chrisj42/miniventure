package miniventure.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Game extends ApplicationAdapter {
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 450;
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	
	private TextureAtlas tiles;
	private Sprite tree;
	
	private Rectangle treeBounds;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
		
		batch = new SpriteBatch();
		tiles = new TextureAtlas("data/tiles.txt");
		tree = tiles.createSprite("tree");
		
		treeBounds = new Rectangle(SCREEN_WIDTH/2, 0, tree.getWidth(), tree.getHeight());
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		tiles.dispose();
	}

	@Override
	public void render () {
		camera.update();
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
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
	}
}
