package miniventure.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;

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
		
		treeBounds = new Rectangle(SCREEN_WIDTH/2 - tree.getWidth()/2, 0, tree.getWidth(), tree.getHeight());
	}

	@Override
	public void render () {
		camera.update();
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		
		batch.draw(tree, treeBounds.x, treeBounds.y);
		
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		tiles.dispose();
	}
}
