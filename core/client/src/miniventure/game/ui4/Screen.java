package miniventure.game.ui4;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Screen extends Container {
	
	private final Batch batch;
	private final ScreenViewport viewport;
	
	public Screen() {
		OrthographicCamera camera = new OrthographicCamera();
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		viewport = new ScreenViewport(camera);
		batch = GameCore.getBatch();
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void render() {
		validate(); // does nothing if already valid
		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		render(batch);
		batch.end();
	}
	
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		invalidate();
	}
	
	@Override
	void setParent(Container parent) {
		System.err.println("ERROR: tried to set parent of screen "+this+" to container "+parent+"; screens cannot have parents.");
	}
	
	@Override
	public void layout() {
		getSizeCache().setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		super.layout();
	}
	
	@Override
	protected final Vector2 calcMinSize(Vector2 rt) { return calcPreferredSize(rt); }
	
	@Override
	protected final Vector2 calcPreferredSize(Vector2 rt) {
		return rt.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	@Override
	protected final Vector2 calcMaxSize(Vector2 rt) { return calcPreferredSize(rt); }
}
