package miniventure.game.screen;

import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {
	
	private OrthographicCamera camera, uiCamera;
	private int zoom = 0;
	private SpriteBatch batch;
	
	private Player mainPlayer;
	private int curLevel;
	
	private final GameCore game;
	
	private Screen screen = null;
	
	public GameScreen(GameCore game) {
		this.game = game;
		batch = game.getBatch();
		game.setGameScreen(this);
		
		createWorld();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
		uiCamera = new OrthographicCamera();
		uiCamera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
	}
	
	private void createWorld() {
		Level.resetLevels();
		curLevel = 0;
		
		mainPlayer = new Player();
		
		Level level = Level.getLevel(curLevel);
		level.addEntity(mainPlayer);
		
		Tile spawnTile;
		do spawnTile = level.getTile(
			MathUtils.random(level.getWidth()-1),
			MathUtils.random(level.getHeight()-1)
		);
		while(spawnTile == null || !spawnTile.getType().maySpawn);
		
		mainPlayer.moveTo(spawnTile);
	}
	
	@Override
	public void dispose() {}
	
	@Override
	public void render(float delta) {
		// clears the screen with a green color.
		Gdx.gl.glClearColor(0.1f, 0.5f, 0.1f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		mainPlayer.checkInput(delta);
		Level.getLevel(curLevel).update(delta);
		
		if(Gdx.input.isKeyJustPressed(Keys.MINUS))
			zoom(-1);
		if(Gdx.input.isKeyJustPressed(Keys.EQUALS) || Gdx.input.isKeyJustPressed(Keys.PLUS))
			zoom(1);
		
		Level level = Level.getLevel(curLevel);
		
		Vector2 playerPos = new Vector2();
		mainPlayer.getBounds().getCenter(playerPos);
		float viewWidth = camera.viewportWidth;
		float viewHeight = camera.viewportHeight;
		int lvlWidth = level.getWidth() * Tile.SIZE;
		int lvlHeight = level.getHeight() * Tile.SIZE;
		playerPos.x = MathUtils.clamp(playerPos.x, Math.min(viewWidth/2, lvlWidth/2), Math.max(lvlWidth/2, lvlWidth-viewWidth/2));
		playerPos.y = MathUtils.clamp(playerPos.y, Math.min(viewHeight/2, lvlHeight/2), Math.max(lvlHeight/2, lvlHeight-viewHeight/2));
		camera.position.set(playerPos, camera.position.z);
		camera.update(); // updates the camera "matrices"
		
		Rectangle renderSpace = new Rectangle(camera.position.x - viewWidth/2, camera.position.y - viewHeight/2, viewWidth, viewHeight);
		//Rectangle renderSpace = new Rectangle(0, 0, lvlWidth, lvlHeight);
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		level.render(renderSpace, batch, delta);
		renderGui();
		batch.end();
	}
	
	private void zoom(int dir) {
		zoom += dir;
		
		double zoomFactor = Math.pow(2, dir);
		camera.viewportHeight /= zoomFactor;
		camera.viewportWidth /= zoomFactor;
	}
	
	@Override public void resize(int width, int height) {
		float zoomFactor = (float) Math.pow(2, zoom);
		camera.setToOrtho(false, width/zoomFactor, height/zoomFactor);
		uiCamera.setToOrtho(false, width/zoomFactor, height/zoomFactor);
	}
	
	@Override public void pause() {}
	@Override public void resume() {}
	
	@Override public void show() {}
	@Override public void hide() {}
	
	private static HashMap<Boolean, TextureRegion[]> heartSprites = new HashMap<>();
	static {
		TextureRegion[] fullHearts = {
			GameCore.icons.get("heart-left"),
			GameCore.icons.get("heart-right")
		};
		heartSprites.put(true, fullHearts);
		
		TextureRegion[] deadHearts = {
			GameCore.icons.get("heart-dead-left"),
			GameCore.icons.get("heart-dead-right")
		};
		heartSprites.put(false, deadHearts);
	}
	
	private void renderGui() {
		//System.out.println("rendering GUI");
		batch.setProjectionMatrix(uiCamera.combined);
		
		
		
		// render health
		for(int i = 0; i < Player.Stat.Health.max; i++) {
			TextureRegion heart = heartSprites.get(i < mainPlayer.getStat(Player.Stat.Health))[i % 2];
			batch.draw(heart, i*heart.getRegionWidth(), heart.getRegionHeight() * 0.25f);
		}
		// TODO other stats will be rendered in the exact same fashion, with the same sprites. So make a method for it. Maybe I should instantiate it in the Player class, or even Stat enum? 
		
		
		// draw UI for current item
		Item heldItem = mainPlayer.getHeldItemClone();
		if(heldItem != null) {
			float x = camera.viewportWidth / 3;
			batch.draw(heldItem.getTexture(), x, 5);
			GameCore.writeOutlinedText(game.getFont(), batch, heldItem.getName(), x+heldItem.getTexture().getRegionWidth()+10, 5);
		}
		
		// a list of text to display in the upper left, for debug purposes
		Array<String> debugInfo = new Array<>(); 
		debugInfo.add("Version " + GameCore.VERSION);
		
		// player coordinates, for debug
		float x = mainPlayer.getBounds().x;
		float y = mainPlayer.getBounds().y;
		debugInfo.add("X = "+((int)(x/Tile.SIZE))+" - "+x);
		debugInfo.add("Y = "+((int)(y/Tile.SIZE))+" - "+y);
		
		Tile playerTile = Level.getLevel(curLevel).getClosestTile(mainPlayer.getBounds());
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = Level.getLevel(curLevel).getClosestTile(mainPlayer.getInteractionRect());
		debugInfo.add("Looking at: " + (interactTile == null ? "Null" : interactTile.toLocString()));
		
		for(int i = 0; i < debugInfo.size; i++)
			GameCore.writeOutlinedText(game.getFont(), batch, debugInfo.get(i), 0, uiCamera.viewportHeight-5-15*i);
	}
}
