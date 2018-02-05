package miniventure.game;

import miniventure.game.screen.RespawnScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class GameScreen {
	
	private SpriteBatch batch = GameCore.getBatch();
	private BitmapFont font = GameCore.getFont();
	
	private final OrthographicCamera camera, uiCamera;
	private int zoom = 0;
	private FrameBuffer lightingBuffer;
	
	public GameScreen() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		uiCamera = new OrthographicCamera();
		uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		lightingBuffer = FrameBuffer.createFrameBuffer(Format.RGBA8888, (int)camera.viewportWidth, (int)camera.viewportHeight, false);
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
	}
	
	public void update(@NotNull Player mainPlayer, @NotNull Level level) {
		if(mainPlayer.getLevel() == null) {
			System.out.println("main player level is null");
			GameCore.setScreen(new RespawnScreen());
			return;
		}
		
		level.update(Gdx.graphics.getDeltaTime());
	}
	
	// timeOfDay is 0 to 1.
	public void render(@NotNull Player mainPlayer, float alphaOverlay, @NotNull Level level, boolean updateCamera) {
		
		float viewWidth = camera.viewportWidth;
		float viewHeight = camera.viewportHeight;
		
		if(updateCamera) {
			Vector2 playerPos = new Vector2();
			mainPlayer.getBounds().getCenter(playerPos);
			int lvlWidth = level.getWidth() * Tile.SIZE;
			int lvlHeight = level.getHeight() * Tile.SIZE;
			playerPos.x = MathUtils.clamp(playerPos.x, Math.min(viewWidth/2, lvlWidth/2), Math.max(lvlWidth/2, lvlWidth-viewWidth/2));
			playerPos.y = MathUtils.clamp(playerPos.y, Math.min(viewHeight/2, lvlHeight/2), Math.max(lvlHeight/2, lvlHeight-viewHeight/2));
			camera.position.set(playerPos, camera.position.z);
			camera.update(); // updates the camera "matrices"
			uiCamera.update();
		}
		
		Rectangle renderSpace = new Rectangle(camera.position.x - viewWidth/2, camera.position.y - viewHeight/2, viewWidth, viewHeight);
		
		
		lightingBuffer.begin();
		//Gdx.gl.glClearColor(0, 0, 0, alphaOverlay);
		//Color c = new Color(0, 0, 0, 0.63f);
		//java.awt.Color c = new java.awt.Color(0, 72, 225, 255*7/10);
		//Gdx.gl.glClearColor(0, 72f/255, 225f/255, 0.2f);
		Gdx.gl.glClearColor(0, 0.03f, 0.278f, alphaOverlay);
		//Gdx.gl.glClearColor(0, 72f/255, 225f/255, 20f/255);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		Array<Vector3> lights = level.renderLighting(renderSpace);
		final TextureRegion lightTexture = GameCore.icons.get("light");
		
		for(Vector3 light: lights) {
			float radius = light.z * (float) Math.pow(2, zoom);
			uiCamera.unproject(camera.project(light));
			Vector2 screenPos = new Vector2(light.x, light.y);
			//System.out.println("drawing light at " + light + ", radius " + radius);
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
		if(interactTile != null)
			batch.draw(GameCore.icons.get("tile-frame"), interactTile.getX(), interactTile.getY());
		
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
			
			Vector2 playerPos = mainPlayer.getBounds().getCenter(new Vector2());
			
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
		camera.setToOrtho(false, width/zoomFactor, height/zoomFactor);
		uiCamera.setToOrtho(false, width, height);
		
		lightingBuffer.dispose();
		lightingBuffer = FrameBuffer.createFrameBuffer(Format.RGBA8888, (int)uiCamera.viewportWidth, (int)uiCamera.viewportHeight, false);
	}
}
