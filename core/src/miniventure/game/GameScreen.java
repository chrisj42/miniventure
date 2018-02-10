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
	
	private static final float OFF_SCREEN_LIGHT_RADIUS = 5; // in tiles
	
	private static final float MAX_SCREEN_WIDTH_TILES = 20;
	private static final float MAX_SCREEN_HEIGHT_TILES = 20;
	
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
		player.checkInput(getMouseInput());
		
		if(Gdx.input.isKeyJustPressed(Keys.MINUS))
			zoom(-1);
		if(Gdx.input.isKeyJustPressed(Keys.EQUALS) || Gdx.input.isKeyJustPressed(Keys.PLUS))
			zoom(1);
		
		if(Gdx.input.isKeyJustPressed(Keys.R) && Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
			GameCore.getWorld().createWorld(0, 0);
	}
	
	public void render(@NotNull Player mainPlayer, Color[] lightOverlays, @NotNull Level level) {
		
		// get the size of the area of the game on screen by projecting the application window dimensions into world space.
		Vector3 screenSize = new Vector3(Gdx.graphics.getWidth(), 0, 0); // because unproject has origin at the top, so the upper right corner is at (width, 0).
		camera.unproject(screenSize); // screen to render coords
		screenSize.scl(1f/Tile.SIZE); // now in world coords
		
		Vector2 playerCenter = mainPlayer.getCenter(); // world coords
		
		// subtract half the view port height and width to get the offset.
		Vector2 offset = new Vector2(playerCenter.x - screenSize.x/2, playerCenter.y - screenSize.y/2); // world coords
		
		Rectangle renderSpace = new Rectangle(offset.x, offset.y, screenSize.x, screenSize.y); // world coords
		
		final float extra = OFF_SCREEN_LIGHT_RADIUS; // in world coords
		Rectangle lightRenderSpace = new Rectangle(renderSpace.x - extra, renderSpace.y - extra, renderSpace.width + extra*2, renderSpace.height + extra*2); // world coords
		
		lightingBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined); // batch will convert render coords to screen coords
		batch.begin();
		
		for(Color color: lightOverlays)
			if (color.a > 0)
				MyUtils.fillRect(0, 0, camera.viewportWidth, camera.viewportHeight, color, batch); // render coords
		
		batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		Array<Vector3> lights = level.renderLighting(lightRenderSpace);
		final TextureRegion lightTexture = GameCore.icons.get("light");
		
		for(Vector3 light: lights) {
			light.sub(offset.x, offset.y, 0).scl(Tile.SIZE); // world to render coords
			float radius = light.z;
			light.y = camera.viewportHeight - light.y;
			batch.draw(lightTexture, light.x - radius, light.y - radius, radius*2, radius*2);
		}
		
		batch.end();
		lightingBuffer.end();
		
		// clears the screen with a green color.
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // default
		level.render(renderSpace, batch, Gdx.graphics.getDeltaTime(), offset); // renderSpace in world coords, but offset can give render coords
		
		Tile interactTile = level.getClosestTile(mainPlayer.getInteractionRect());
		if(interactTile != null) {
			Vector2 pos = interactTile.getPosition().sub(offset).scl(Tile.SIZE); // world to render coords
			batch.draw(GameCore.icons.get("tile-frame"), pos.x, pos.y);
		}
		
		batch.setProjectionMatrix(uiCamera.combined); // batch uses coords as is, screen to screen
		batch.draw(lightingBuffer.getColorBufferTexture(), 0, 0);
		
		renderGui(mainPlayer, level);
		batch.end();
	}
	
	private Vector2 getMouseInput() {
		if(Gdx.input.isTouched()) {
			
			Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
			mousePos.y = Gdx.graphics.getHeight() - mousePos.y; // origin is top left corner, so reverse Y dir
			
			// player is always in the center of the screen.
			Vector2 screenCenter = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			
			Vector2 mouseMove = mousePos.cpy().sub(screenCenter);
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
		Rectangle playerBounds = mainPlayer.getBounds();
		debugInfo.add("X = "+playerBounds.x);
		debugInfo.add("Y = "+playerBounds.y);
		
		Tile playerTile = level.getClosestTile(playerBounds);
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
		resetCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	private void resetCamera(int width, int height) {
		float viewWidth = MAX_SCREEN_WIDTH_TILES;
		float viewHeight = MAX_SCREEN_HEIGHT_TILES;
		float wh = width*1f/height;
		if(wh > 1)
			viewHeight /= wh;
		if(wh < 1)
			viewWidth *= wh;
		
		float zoomFactor = (float) Math.pow(2, zoom);
		
		camera.setToOrtho(false, viewWidth*Tile.SIZE / zoomFactor, viewHeight*Tile.SIZE / zoomFactor);
	}
	
	void resize(int width, int height) {
		resetCamera(width, height);
		
		uiCamera.setToOrtho(false, width, height);
		
		if(lightingBuffer != null)
			lightingBuffer.dispose();
		lightingBuffer = FrameBuffer.createFrameBuffer(Format.RGBA8888, width, height, false);
	}
}
