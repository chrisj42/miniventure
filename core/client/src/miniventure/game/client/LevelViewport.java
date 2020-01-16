package miniventure.game.client;

import java.util.List;

import miniventure.game.GameCore;
import miniventure.game.item.CraftingScreen.ClientObjectRecipe;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelViewport {
	
	private static final float OFF_SCREEN_LIGHT_RADIUS = 5; // in tiles; used to render light halos when the object creating said halo could be off screen. any halos bigger than this in radius will appear to disappear suddenly when the object casting it goes too far off screen; but increasing this value means iterating through a lot more objects. Your average halo isn't going to bigger than 5 tiles in radius, though, so 5 is a good enough value.
	
	private static final float DEFAULT_VIEWPORT_SIZE = 32; // in tiles
	
	private static final int MIN_ZOOM = -4, MAX_ZOOM = 5;
	
	// these two values determine how much of the level to render in either dimension, and are also used to fit the viewport to the game window. Later, they should be customizable by the user, or the mapmaker; for now, they'll remain at 0, meaning it doesn't limit the number of tiles rendered, and the default viewport size will be used for fitting.
	private float maxWorldViewWidth = 0;
	private float maxWorldViewHeight = 0;
	
	private final SpriteBatch batch = ClientCore.getBatch();
	
	private final OrthographicCamera camera, lightingCamera;
	
	private int zoom = 0;
	private FrameBuffer lightingBuffer;
	
	private Vector2 cursorPos = new Vector2();
	private boolean cursorValid = true;
	
	@Nullable
	Vector2 getCursorPos() { return cursorValid ? cursorPos : null; }
	
	public LevelViewport() { this(new OrthographicCamera()); }
	public LevelViewport(OrthographicCamera lightingCamera) {
		camera = new OrthographicCamera();
		this.lightingCamera = lightingCamera;
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	void dispose() {
		if(lightingBuffer != null)
			lightingBuffer.dispose();
	}
	
	public void handleInput() {
		if(Gdx.input.isKeyJustPressed(Keys.MINUS))
			zoom(-1);
		if(Gdx.input.isKeyJustPressed(Keys.EQUALS) || Gdx.input.isKeyJustPressed(Keys.PLUS))
			zoom(1);
	}
	
	private void screenToWorld(Vector3 pos, Vector2 offset) {
		camera.unproject(pos); // screen to render coords
		pos.scl(1f/Tile.SIZE); // now in world coords
		pos.add(offset.x, offset.y, 0);
	}
	
	// if texture is null, an outline is drawn, otherwise the given texture is drawn
	private void drawOverTile(Tile tile, Vector2 offset, @Nullable TextureHolder texture, @Nullable Color tint) {
		if(tile != null) {
			Vector2 pos = tile.getPosition().sub(offset).scl(Tile.SIZE);
			if(texture == null)
				MyUtils.drawRect(pos.x, pos.y, Tile.SIZE, Tile.SIZE, Tile.SIZE / 16, tint == null ? Color.BLACK : tint, batch);
			else {
				Vector2 sizeDiff = new Vector2(Tile.SIZE, Tile.SIZE).sub(texture.width, texture.height);
				pos.add(sizeDiff.scl(0.5f));
				Color prev = batch.getColor();
				if(tint == null) batch.setColor(1, 1, 1, 0.5f);
				else batch.setColor(tint);
				batch.draw(texture.texture, pos.x, pos.y);
				batch.setColor(prev);
			}
		}
	}
	
	public void render(@NotNull Vector2 cameraCenter, Color ambientLighting, @NotNull RenderLevel level) {
		// get the size of the area of the game on screen by projecting the application window dimensions into world space.
		Vector3 screenSize = new Vector3(Gdx.graphics.getWidth(), 0, 0); // because unproject has origin at the top, so the upper right corner is at (width, 0).
		camera.unproject(screenSize); // screen to render coords
		screenSize.scl(1f/Tile.SIZE); // now in world coords
		
		// subtract half the view port height and width to get the offset.
		Vector2 offset = new Vector2(cameraCenter.x - screenSize.x/2, cameraCenter.y - screenSize.y/2); // world coords
		
		Rectangle renderSpace = new Rectangle(offset.x, offset.y, screenSize.x, screenSize.y); // world coords
		
		// trim the rendering space to not exceed the max in either direction.
		if(maxWorldViewWidth > 0)
			renderSpace.width = Math.min(renderSpace.width, maxWorldViewWidth);
		if(maxWorldViewHeight > 0)
			renderSpace.height = Math.min(renderSpace.height, maxWorldViewHeight);
		renderSpace.setCenter(cameraCenter);
		
		final float extra = OFF_SCREEN_LIGHT_RADIUS; // in world coords
		Rectangle lightRenderSpace = new Rectangle(renderSpace.x - extra, renderSpace.y - extra, renderSpace.width + extra*2, renderSpace.height + extra*2); // world coords
		
		lightingBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined); // batch will convert render coords to screen coords
		batch.begin();
		
		if (ambientLighting.a > 0)
			MyUtils.fillRect(0, 0, camera.viewportWidth, camera.viewportHeight, ambientLighting, batch); // render coords
		
		batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		Array<Vector3> lights = RenderLevel.renderLighting(level.getOverlappingObjects(lightRenderSpace));
		final TextureRegion lightTexture = GameCore.icons.get("light").texture;
		
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
		//System.out.println("rendering level in bounds "+renderSpace+" to camera at "+camera.position+" with offset "+offset);
		level.render(renderSpace, batch, GameCore.getDeltaTime(), offset); // renderSpace in world coords, but offset can give render coords
		
		final ClientPlayer player = ClientCore.getWorld().getMainPlayer();
		
		if(player != null) {
			// cursor management
			Vector3 cursor = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(cursor); // screen to render coords
			cursor.scl(1f / Tile.SIZE); // render coords to renderable world
			cursor.add(offset.x, offset.y, 0); // tile offset; renderable world to actual world coords
			cursorPos.set(cursor.x, cursor.y);
			// limit range
			CursorHighlight highlightMode = player.getCurrentHighlightMode();
			List<Tile> path = Player.computeCursorPos(cameraCenter, cursorPos, level, highlightMode);
			Tile cursorTile = level.getTile(cursorPos);
			Tile last = path.get(path.size()-1);
			Color invalidColor = new Color(1, 0, 0, .5f);
			if(ClientCore.debugInteract) {
				boolean invalid = false;
				invalidColor.mul(1, 1, 1, .5f);
				Color norm = Color.BLACK.cpy().mul(1, 1, 1, 0.5f);
				for(Tile t: path) {
					drawOverTile(t, offset, null, invalid ? invalidColor : norm);
					if(t == cursorTile)
						invalid = true;
				}
			}
			drawOverTile(cursorTile, offset, null, null);
			cursorValid = last == cursorTile;
			if(!cursorValid)
				drawOverTile(last, offset, null, invalidColor);
			
			// Vector2 dist = cursorPos.cpy().sub(cameraCenter);
			// dist.setLength(Math.min(dist.len(), Player.MAX_CURSOR_RANGE));
			// cursorPos.set(dist.add(cameraCenter));
			
			if(ClientCore.getScreen() == null) {
				// Tile cursorTile = level.getTile(cursorPos);
				
				if (cursorTile != null) {
					if(highlightMode != CursorHighlight.INVISIBLE) {
						// draw highlight for client cursor
						ClientObjectRecipe objectRecipe = player.getObjectRecipe();
						if (objectRecipe == null)
							drawOverTile(cursorTile, offset, null, null);
						else {
							// draw the item sprite instead
							drawOverTile(cursorTile, offset, objectRecipe.getTexture(), null);
						}
					}
					
					// interaction bounds debug
					/*if(ClientCore.debugInteract) {
						// render player interaction rect
						drawOutline(offset, player.getInteractionRect(cursorPos), batch);
					}*/
				}
			}
		}
		
		/*if(ClientCore.debugChunk) {
			// render chunk boundaries
			int minX = MathUtils.ceil(renderSpace.x) / Chunk.SIZE * Chunk.SIZE;
			int minY = MathUtils.ceil(renderSpace.y) / Chunk.SIZE * Chunk.SIZE;
			int maxX = MathUtils.ceil((renderSpace.x + renderSpace.width) / Chunk.SIZE) * Chunk.SIZE;
			int maxY = MathUtils.ceil((renderSpace.y + renderSpace.height) / Chunk.SIZE) * Chunk.SIZE;
			
			int lineThickness = (int) Math.pow(2, -zoom);
			
			for (int x = minX; x <= maxX; x += Chunk.SIZE) {
				MyUtils.fillRect((x - offset.x) * Tile.SIZE-lineThickness, (minY - offset.y) * Tile.SIZE, lineThickness*2+1, (maxY - minY) * Tile.SIZE, Color.PINK, batch);
			}
			for (int y = minY; y <= maxY; y += Chunk.SIZE) {
				MyUtils.fillRect((minX - offset.x) * Tile.SIZE, (y - offset.y) * Tile.SIZE-lineThickness, (maxX - minX) * Tile.SIZE, lineThickness*2+1, Color.PINK, batch);
			}
		}*/
		
		batch.setProjectionMatrix(lightingCamera.combined); // batch uses coords as is, screen to screen
		batch.draw(lightingBuffer.getColorBufferTexture(), 0, 0);
		
		batch.end();
	}
	
	/*private static void drawOutline(Vector2 offset, Rectangle rect, SpriteBatch batch) {
		rect.x = (rect.x - offset.x) * Tile.SIZE;
		rect.y = (rect.y - offset.y) * Tile.SIZE;
		rect.width *= Tile.SIZE;
		rect.height *= Tile.SIZE;
		MyUtils.drawRect(rect, 2, Color.BLACK, batch);
	}*/
	
	public void zoom(int dir) {
		zoom += dir;
		zoom = MathUtils.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
		resetCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	private void resetCamera(final int width, final int height) {
		float zoomFactor = (float) Math.pow(2, zoom);
		
		Rectangle window = new Rectangle(0, 0, width, height);
		Rectangle view = new Rectangle(0, 0, maxWorldViewWidth, maxWorldViewHeight);
		
		if(view.width <= 0)
			view.width = Math.max(view.height, DEFAULT_VIEWPORT_SIZE);
		if(view.height <= 0)
			view.height = Math.max(view.width, DEFAULT_VIEWPORT_SIZE);
		
		window.fitInside(view);
		
		float viewportWidth = window.width * Tile.SIZE / zoomFactor;
		float viewportHeight = window.height * Tile.SIZE / zoomFactor;
		
		camera.setToOrtho(false, viewportWidth, viewportHeight);
		// GameCore.debug("set level camera: "+width+'x'+height+" screen with "+viewportWidth+'x'+viewportHeight+" viewport ("+getZoomRatio(width/viewportWidth)+" actual px, "+getZoomRatio(width/viewportWidth*Tile.SCALE)+" apparent px)");
	}
	
	private static String getZoomRatio(float ratio) {
		if(ratio < 1)
			return "1:"+(1/ratio);
		else
			return ratio+":1";
	}
	
	public void resize(int width, int height) {
		resetCamera(width, height);
		
		lightingCamera.setToOrtho(false, width, height);
		
		if(lightingBuffer != null)
			lightingBuffer.dispose();
		lightingBuffer = new FrameBuffer(Format.RGBA8888, width, height, false);
	}
	
	public float getViewWidth() { return camera.viewportWidth/Tile.SIZE; }
	public float getViewHeight() { return camera.viewportHeight/Tile.SIZE; }
}
