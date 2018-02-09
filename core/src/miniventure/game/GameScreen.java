package miniventure.game;

import miniventure.game.util.MyUtils;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class GameScreen {
	
	private static final float OFF_SCREEN_LIGHT_RADIUS = 10;
	
	private SpriteBatch batch = GameCore.getBatch();
	private BitmapFont font = GameCore.getFont();
	
	private final OrthographicCamera camera, uiCamera;
	private int zoom = 0;
	private FrameBuffer lightingBuffer;
	
	public GameScreen() {
		camera = new OrthographicCamera();
		uiCamera = new OrthographicCamera();
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	void dispose() {
		if(lightingBuffer != null)
			lightingBuffer.dispose();
	}
	
	public void handleInput(@NotNull Player player) {
		player.checkInput(getMouseInput(player));
		
		if(Gdx.input.isKeyJustPressed(Keys.MINUS))
			zoom(-1);
		if(Gdx.input.isKeyJustPressed(Keys.EQUALS) || Gdx.input.isKeyJustPressed(Keys.PLUS))
			zoom(1);
		
		if(Gdx.input.isKeyJustPressed(Keys.R) && Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
			GameCore.getWorld().respawn();
	}
	
	// timeOfDay is 0 to 1.
	public void render(@NotNull Player mainPlayer, Color[] lightOverlays, @NotNull Level level) {
		
		float viewWidth = camera.viewportWidth;
		float viewHeight = camera.viewportHeight;
		
		// the camera position is always relative to the current chunk.
		camera.position.set(mainPlayer.getCenter(), camera.position.z);
		camera.update(); // updates the camera "matrices"
		uiCamera.update();
		
		Rectangle renderSpace = new Rectangle(camera.position.x - viewWidth/2, camera.position.y - viewHeight/2, viewWidth, viewHeight);
		
		final float extra = OFF_SCREEN_LIGHT_RADIUS;
		Rectangle lightRenderSpace = new Rectangle(renderSpace.x - extra, renderSpace.y - extra, renderSpace.width + extra*2, renderSpace.height + extra*2);
		
		lightingBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		
		for(Color color: lightOverlays)
			if (color.a > 0)
				MyUtils.fillRect(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight, color, batch);
		
		batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		Array<Vector3> lights = level.renderLighting(lightRenderSpace);
		final TextureRegion lightTexture = GameCore.icons.get("light");
		
		for(Vector3 light: lights) {
			float radius = light.z * (float) Math.pow(2, zoom);
			uiCamera.unproject(camera.project(light));
			Vector2 screenPos = new Vector2(light.x, light.y);
			batch.draw(lightTexture, screenPos.x - radius, screenPos.y - radius, radius*2, radius*2);
		}
		
		batch.end();
		lightingBuffer.end();
		
		// clears the screen with a green color.
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // default
		level.render(renderSpace, batch, Gdx.graphics.getDeltaTime());
		
		Tile interactTile = level.getClosestTile(mainPlayer.getInteractionRect());
		if(interactTile != null) {
			Vector2 pos = interactTile.getPosition();
			batch.draw(GameCore.icons.get("tile-frame"), pos.x, pos.y);
		}
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.draw(lightingBuffer.getColorBufferTexture(), 0, 0);
		
		renderGui(mainPlayer, level);
		batch.end();
	}
	
	@NotNull
	private Vector2 getMouseInput(@NotNull Player mainPlayer) {
		if(Gdx.input.isTouched()) {
			
			Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
			mousePos.y = uiCamera.viewportHeight - mousePos.y; // origin is top left corner, so reverse Y dir
			
			Vector2 playerPos = mainPlayer.getCenter();
			
			// Note: the camera pos is in the center of the screen.
			
			Vector3 playerScreenPos = camera.project(new Vector3(playerPos, 0));
			
			Vector2 mouseMove = new Vector2();
			mouseMove.x = mousePos.x - playerScreenPos.x;
			mouseMove.y = mousePos.y - playerScreenPos.y;
			mouseMove.nor();
			
			return mouseMove;
		}
		
		return new Vector2();
	}
	
	private void renderGui(@NotNull Player mainPlayer, @NotNull Level level) {
		batch.setProjectionMatrix(uiCamera.combined);
		
		// draw UI for current item, and stats
		mainPlayer.drawGui(new Rectangle(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight), batch, font);
		
		
		// a list of text to display in the upper left, for debug purposes
		Array<String> debugInfo = new Array<>(); 
		debugInfo.add("Version " + GameCore.VERSION);
		
		// player coordinates, for debug
		float x = mainPlayer.getBounds().x;
		float y = mainPlayer.getBounds().y;
		debugInfo.add("X = "+((int)(x/Tile.SIZE))+" - "+x);
		debugInfo.add("Y = "+((int)(y/Tile.SIZE))+" - "+y);
		
		Tile playerTile = level.getClosestTile(mainPlayer.getBounds());
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = level.getClosestTile(mainPlayer.getInteractionRect());
		debugInfo.add("Looking at: " + (interactTile == null ? "Null" : interactTile.toLocString()));
		
		debugInfo.add("Entities in level: " + level.getEntityCount()+"/"+level.getEntityCap());
		
		debugInfo.add("Time: " + GameCore.getWorld().getTimeOfDayString());
		
		for(int i = 0; i < debugInfo.size; i++)
			MyUtils.writeOutlinedText(font, batch, debugInfo.get(i), 0, uiCamera.viewportHeight-5-15*i);
	}
	
	private void zoom(int dir) {
		zoom += dir;
		
		double zoomFactor = Math.pow(2, dir);
		camera.viewportHeight /= zoomFactor;
		camera.viewportWidth /= zoomFactor;
	}
	
	void resize(int width, int height) {
		float zoomFactor = (float) Math.pow(2, zoom);
		camera.setToOrtho(false, width/zoomFactor/Tile.SIZE, height/zoomFactor/Tile.SIZE);
		uiCamera.setToOrtho(false, width, height);
		
		if(lightingBuffer != null)
			lightingBuffer.dispose();
		lightingBuffer = FrameBuffer.createFrameBuffer(Format.RGBA8888, (int)uiCamera.viewportWidth, (int)uiCamera.viewportHeight, false);
	}
}
