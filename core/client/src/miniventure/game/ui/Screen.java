package miniventure.game.ui;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;

public class Screen {
	
	private static final OrthographicCamera camera = new OrthographicCamera(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT);
	
	private Container root;
	private Color background = Color.ORANGE;
	
	public Screen() {
		root = new Container();
		root.setBackground(Background.fillColor(Color.RED));
		root.addComponent(new Box(0, 0, 100, 100, Color.GREEN));
		root.addComponent(new Box(0, 150, 50, 100, Color.BLUE));
		root.addComponent(new Box(150, 50, 50, 200, Color.YELLOW));
	}
	
	public void render() {
		if(background != null) {
			Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		
		Batch batch = GameCore.getBatch();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		root.render(batch);
		batch.end();
	}
	
	public void resize(int screenWidth, int screenHeight) {
		camera.setToOrtho(false, screenWidth, screenHeight);
		camera.position.set(screenWidth / 2, screenHeight / 2, 0);
		camera.update();
	}
}
