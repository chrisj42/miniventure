package miniventure.game.ui2;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;

import org.jetbrains.annotations.Nullable;

public class SceneRoot extends Container {
	
	// a root container.
	
	private static final OrthographicCamera camera = new OrthographicCamera(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT);
	
	private final Color background;
	private SceneRoot parentScreen;
	
	public SceneRoot() { this((Color)null); }
	public SceneRoot(@Nullable Color background) { this.background = background; }
	/*public SceneRoot(Layout layout) { this(layout, null); }
	public SceneRoot(Layout layout, @Nullable Color background) {
		super(layout);
		this.background = background;
	}*/
	
	@Override
	public void update() { super.update(); }
	
	public void setParentScreen(SceneRoot parent) { this.parentScreen = parent; }
	public SceneRoot getParentScreen() { return parentScreen; }
	
	public void render() {
		// System.out.println("rendering scene");
		if(background != null) {
			Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		
		Batch batch = GameCore.getBatch();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		render(batch);
		batch.end();
	}
	
	public void resize(int screenWidth, int screenHeight) {
		camera.setToOrtho(false, screenWidth, screenHeight);
		camera.position.set(screenWidth / 2, screenHeight / 2, 0);
		camera.update();
		invalidate();
	}
}
