package miniventure.game.core;

import miniventure.game.core.InputHandler.Control;
import miniventure.game.item.inventory.InventoryOverlay;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.InputScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.PauseScreen;
import miniventure.game.screen.util.DiscreteViewport;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.PlayerInventory;
import miniventure.game.world.management.Level;
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
import com.badlogic.gdx.utils.viewport.Viewport;

import org.jetbrains.annotations.NotNull;

public class GameScreen {
	
	private SpriteBatch batch = GdxCore.getBatch();
	
	private final OrthographicCamera noScaleCamera = new OrthographicCamera();
	
	private final LevelViewport levelView;
	private final ChatScreen chatScreen, chatOverlay;
	
	private final Viewport uiViewport;
	private InventoryOverlay inventoryGui;
	
	public GameScreen() {
		levelView = new LevelViewport();
		
		chatScreen = new ChatScreen(false);
		chatOverlay = new ChatScreen(true);
		chatScreen.connect(chatOverlay);
		chatOverlay.connect(chatScreen);
		
		uiViewport = new DiscreteViewport();
		inventoryGui = null;
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void dispose() {
		levelView.dispose();
		chatScreen.dispose();
		chatOverlay.dispose();
		
		if(inventoryGui != null)
			inventoryGui.dispose();
	}
	
	public void setInventoryGui(@NotNull PlayerInventory inv) {
		if(inventoryGui != null)
			inventoryGui.dispose();
		
		inventoryGui = new InventoryOverlay(inv, uiViewport);
		inventoryGui.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void handleInput(@NotNull Player player) {
		player.handleInput(levelView.getCursorPos());
		
		boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		if(shift && GameCore.debug && Gdx.input.isKeyJustPressed(Keys.S)) {
			GdxCore.setScreen(new InputScreen("Enter new Player Speed:", newSpeed -> {
				try {
					float value = Float.parseFloat(newSpeed);
					player.setSpeed(value);
				} catch(NumberFormatException ignored) {}
				GdxCore.setScreen(null);
			}));
		}
		
		levelView.handleInput();
		
		if(shift) {
			if(Gdx.input.isKeyJustPressed(Keys.D) && !Gdx.input.isKeyPressed(Keys.TAB))
				GameCore.debugInfo = !GameCore.debugInfo;
			
			// if(Gdx.input.isKeyJustPressed(Keys.T))
			// 	ClientCore.debugTile = !ClientCore.debugTile;
			
			if(Gdx.input.isKeyJustPressed(Keys.B))
				GameCore.debugBounds = !GameCore.debugBounds;
			
			if(Gdx.input.isKeyJustPressed(Keys.I))
				GameCore.debugInteract = !GameCore.debugInteract;
		}
		// else if(Gdx.input.isKeyJustPressed(Keys.B))
		// 	ClientCore.debugChunk = !ClientCore.debugChunk;
		
		if(!GdxCore.hasMenu()) {
			if(GdxCore.input.pressingControl(Control.CHAT, true))
				chatScreen.focus("");
			
			else if(Gdx.input.isKeyJustPressed(Keys.SLASH))
				chatScreen.focus("/");
			
			else if(GdxCore.input.pressingControl(Control.PAUSE, true))
				GdxCore.setScreen(new PauseScreen(player.getWorld()));
			
			else if(GameCore.debug && Gdx.input.isKeyJustPressed(Keys.M))
				GdxCore.setScreen(new MapScreen(player.getLevel()));
		}
	}
	
	public void render(@NotNull Level level, Color lightOverlay) {
		render(level, lightOverlay, true);
	}
	public void render(@NotNull Level level, Color lightOverlay, boolean drawGui) {
		
		Vector2 v = MyUtils.getV2();
		levelView.render(level, level.getPlayer().getCenter(v), lightOverlay);
		MyUtils.freeV2(v);
		
		// batch.setProjectionMatrix(uiCamera.combined);
		if(drawGui) {
			renderGui(level);
			
			if(inventoryGui != null) {
				inventoryGui.act();
				inventoryGui.draw();
			}
		}
		
		chatOverlay.act();
		if(!(GdxCore.getScreen() instanceof ChatScreen)) {
			chatOverlay.draw();
		}
	}
	
	private void renderGui(@NotNull Level level) {
		batch.setProjectionMatrix(uiViewport.getCamera().combined);
		batch.begin();
		// draw UI for stats
		// System.out.println("ui viewport: "+uiCamera.viewportWidth+"x"+uiCamera.viewportHeight);
		level.getPlayer().drawStatGui(new Rectangle(0, 0, uiViewport.getWorldWidth(), uiViewport.getWorldHeight()), batch);
		batch.end();
		
		batch.setProjectionMatrix(noScaleCamera.combined);
		batch.begin();
		
		if(!GameCore.debugInfo) {
			if(GameCore.debug) {
				BitmapFont f = GdxCore.getFont();
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
		Rectangle playerBounds = level.getPlayer().getBounds();
		debugInfo.add("X = "+playerBounds.x);
		debugInfo.add("Y = "+playerBounds.y);
		
		Tile playerTile = level.getTile(playerBounds);
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = level.getTile(level.getPlayer().getInteractionRect(levelView.getCursorPos()));
		debugInfo.add("Looking at: " + (interactTile == null ? "Null" : interactTile.toLocString().replace("Client", "")));
		
		debugInfo.add("Mobs in level: " + level.getMobCount()+"/"+level.getMobCap());
		debugInfo.add("Total Entities: " + level.getEntityCount());
		
		debugInfo.add("Time: " + TimeOfDay.getTimeString(level.getWorld().getDaylightOffset()));
		
		BitmapFont font = GdxCore.getFont();
		if(GameCore.debug) font.setColor(Color.ORANGE);
		else font.setColor(Color.WHITE);
		for(int i = 0; i < debugInfo.size; i++) {
			if(GameCore.debug && i == 1)
				font.setColor(Color.WHITE);
			font.draw(batch, debugInfo.get(i), 0, noScaleCamera.viewportHeight - 5 - font.getLineHeight() * i);
		}
		
		batch.end();
	}
	
	public void resize(int width, int height) {
		levelView.resize(width, height);
		noScaleCamera.setToOrtho(false, width, height);
		
		if(inventoryGui == null)
			uiViewport.update(width, height, true);
		else
			inventoryGui.resize(width, height);
	}
	
	public InventoryOverlay getGuiStage() { return inventoryGui; }
}
