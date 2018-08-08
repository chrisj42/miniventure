package miniventure.game.ui;

import miniventure.game.GameCore;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class Screen {
	
	private static final OrthographicCamera camera = new OrthographicCamera(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT);
	
	protected Container root;
	@NotNull
	private RelPos rootPos = RelPos.CENTER;
	private Color background = Color.ORANGE;
	
	/*
		The various screens will position containers in various positions:
		
		- title screen: custom background and components put in a vertical list (with custom spacing between options?)
		- pause screen: vertical list
		- informational screens: lists of labels (labels will have a set width)
		- inventory: grid? or maybe list? either way the grid will have equally-sized cells.
		
		all of these are fairly simple, and require a relpos to position the elements within the list.
	 */
	
	protected Screen() {
		root = new Container();
	}
	
	// protected Container getRoot() { return root; }
	
	public void render() {
		if(background != null) {
			Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		
		// position root
		Vector2 screenSize = new Vector2(camera.viewportWidth, camera.viewportHeight);
		root.setPosition(rootPos.positionRect(root.getSize(screenSize), new Rectangle(-screenSize.x/2, -screenSize.y/2, screenSize.x, screenSize.y)));
		
		Batch batch = GameCore.getBatch();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		root.render(batch, new Vector2(), screenSize);
		batch.end();
	}
	
	public void resize(int screenWidth, int screenHeight) {
		camera.setToOrtho(false, screenWidth, screenHeight);
		camera.position.set(screenWidth / 2, screenHeight / 2, 0);
		camera.update();
	}
	
	public void setRootPos(@NotNull RelPos rootPos) {
		this.rootPos = rootPos;
	}
}
