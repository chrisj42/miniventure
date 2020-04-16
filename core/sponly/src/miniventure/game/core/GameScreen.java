package miniventure.game.core;

import miniventure.game.core.InputHandler.Control;
import miniventure.game.item.InventoryOverlay;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.InputScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.PauseScreen;
import miniventure.game.util.Version;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.TimeOfDay;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameScreen {
	
	@NotNull private final LevelViewport levelView;
	
	private SpriteBatch batch = ClientCore.getBatch();
	private InventoryOverlay guiStage;
	
	private final OrthographicCamera uiCamera;
	private final OrthographicCamera noScaleCamera;
	
	final ChatScreen chatOverlay, chatScreen;
	// private boolean showDebug = false;
	
	@NotNull
	private final ClientPlayer player;
	
	public GameScreen(@NotNull ClientPlayer player, @Nullable GameScreen prev, InventoryOverlay invScreen) {
		this.player = player;
		
		uiCamera = new OrthographicCamera();
		noScaleCamera = new OrthographicCamera();
		guiStage = invScreen;
		invScreen.getViewport().setCamera(uiCamera);
		levelView = new LevelViewport(noScaleCamera); // uses uiCamera for rendering lighting to the screen.
		
		chatOverlay = new ChatScreen(true);
		chatScreen = prev == null ? new ChatScreen(false) : prev.chatScreen;
		if(prev != null)
			prev.dispose(true);
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	void dispose() { dispose(false); }
	private void dispose(boolean recycle) {
		levelView.dispose();
		guiStage.dispose();
		chatOverlay.dispose();
		if(!recycle)
			chatScreen.dispose();
		// super.dispose();
	}
	
	public void handleInput() {
		player.handleInput(getMouseInput(), levelView.getCursorPos());
		
		boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		if(shift && GameCore.debug && Gdx.input.isKeyJustPressed(Keys.S)) {
			ClientCore.setScreen(new InputScreen("Enter new Player Speed:", newSpeed -> {
				try {
					float value = Float.parseFloat(newSpeed);
					player.setSpeed(value);
				} catch(NumberFormatException ignored) {}
				ClientCore.setScreen(null);
			}));
		}
		
		levelView.handleInput();
		
		if(shift) {
			if(Gdx.input.isKeyJustPressed(Keys.D) && !Gdx.input.isKeyPressed(Keys.TAB))
				ClientCore.debugInfo = !ClientCore.debugInfo;
			
			// if(Gdx.input.isKeyJustPressed(Keys.T))
			// 	ClientCore.debugTile = !ClientCore.debugTile;
			
			if(Gdx.input.isKeyJustPressed(Keys.B))
				ClientCore.debugBounds = !ClientCore.debugBounds;
			
			if(Gdx.input.isKeyJustPressed(Keys.I))
				ClientCore.debugInteract = !ClientCore.debugInteract;
		}
		// else if(Gdx.input.isKeyJustPressed(Keys.B))
		// 	ClientCore.debugChunk = !ClientCore.debugChunk;
		
		if(!ClientCore.hasMenu()) {
			if(ClientCore.input.pressingControl(Control.CHAT))
				chatScreen.focus("");
			
			else if(ClientCore.input.pressingKey(Keys.SLASH))
				chatScreen.focus("/");
			
			else if(ClientCore.input.pressingControl(Control.PAUSE))
				ClientCore.setScreen(new PauseScreen());
			
			else if(GameCore.debug && ClientCore.input.pressingKey(Keys.M))
				ClientCore.setScreen(new MapScreen());
		}
	}
	
	public void render(Color lightOverlay, @NotNull ClientLevel level) {
		render(lightOverlay, level, true);
	}
	public void render(Color lightOverlay, @NotNull ClientLevel level, boolean drawGui) {
		
		levelView.render(player.getCenter(), lightOverlay, level);
		
		// batch.setProjectionMatrix(uiCamera.combined);
		if(drawGui) {
			renderGui(level);
			// guiStage.focus();
			guiStage.act();
			guiStage.draw();
		}
		
		chatOverlay.act();
		if(!(ClientCore.getScreen() instanceof ChatScreen)) {
			chatOverlay.draw();
		}
	}
	
	private static Vector2 getMouseInput() {
		if(Gdx.input.isTouched()) {
			Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
			mousePos.y = Gdx.graphics.getHeight() - mousePos.y; // origin is top left corner, so reverse Y dir
			
			// player is always in the center of the screen.
			Vector2 screenCenter = new Vector2(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
			
			Vector2 mouseMove = mousePos.cpy().sub(screenCenter);
			mouseMove.nor();
			
			return mouseMove;
		}
		
		return new Vector2();
	}
	
	private void renderGui(@NotNull Level level) {
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		// draw UI for stats
		// System.out.println("ui viewport: "+uiCamera.viewportWidth+"x"+uiCamera.viewportHeight);
		player.drawGui(new Rectangle(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight), batch);
		batch.end();
		
		batch.setProjectionMatrix(noScaleCamera.combined);
		batch.begin();
		
		if(!ClientCore.debugInfo) {
			if(GameCore.debug) {
				BitmapFont f = ClientCore.getFont();
				f.setColor(Color.ORANGE);
				f.draw(batch, "Debug Mode ENABLED", 0, noScaleCamera.viewportHeight - 5);
				f.setColor(Color.WHITE);
			}
			batch.end();
			return;
		}
		
		// a list of text to display in the upper left, for debug purposes
		Array<String> debugInfo = new Array<>();
		
		if(GameCore.debug)
			debugInfo.add("Debug Mode ENABLED");
		
		debugInfo.add("Version: " + Version.CURRENT);
		
		// player coordinates, for debug
		Rectangle playerBounds = player.getBounds();
		debugInfo.add("X = "+playerBounds.x);
		debugInfo.add("Y = "+playerBounds.y);
		
		Tile playerTile = level.getTile(playerBounds);
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = level.getTile(player.getInteractionRect(levelView.getCursorPos()));
		debugInfo.add("Looking at: " + (interactTile == null ? "Null" : interactTile.toLocString().replace("Client", "")));
		
		debugInfo.add("Mobs in level: " + level.getMobCount()+"/"+level.getMobCap());
		debugInfo.add("Total Entities: " + level.getEntityCount());
		
		debugInfo.add("Time: " + TimeOfDay.getTimeString(ClientCore.getWorld().getDaylightOffset()));
		
		BitmapFont font = ClientCore.getFont();
		if(GameCore.debug) font.setColor(Color.ORANGE);
		else font.setColor(Color.WHITE);
		for(int i = 0; i < debugInfo.size; i++) {
			if(GameCore.debug && i == 1)
				font.setColor(Color.WHITE);
			font.draw(batch, debugInfo.get(i), 0, noScaleCamera.viewportHeight - 5 - font.getLineHeight() * i);
		}
		
		batch.end();
	}
	
	void resize(int width, int height) {
		levelView.resize(width, height);
		guiStage.getViewport().update(width, height, true);
		chatOverlay.resize(width, height);
		// noScaleCamera.setToOrtho(false, width, height);
	}
	
	public InventoryOverlay getGuiStage() { return guiStage; }
}
