package miniventure.game.client;

import miniventure.game.GameCore;
import miniventure.game.item.HotbarTable;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.ConfirmScreen;
import miniventure.game.screen.InputScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.util.DiscreteViewport;
import miniventure.game.util.RelPos;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.NotNull;

public class GameScreen {
	
	@NotNull private final LevelViewport levelView;
	
	private SpriteBatch batch = ClientCore.getBatch();
	private MenuScreen guiStage;
	
	private final OrthographicCamera uiCamera;
	private final OrthographicCamera noScaleCamera;
	
	final ChatScreen chatOverlay, chatScreen;
	private boolean showDebug = false;
	
	public GameScreen() {
		uiCamera = new OrthographicCamera();
		noScaleCamera = new OrthographicCamera();
		guiStage = new MenuScreen(false, new DiscreteViewport(uiCamera), batch) {
			{
				ProgressBar fillBar = new ProgressBar(0, 1, .01f, true, VisUI.getSkin()) {
					@Override
					public float getPrefHeight() {
						return uiCamera.viewportHeight * 2 / 5;
					}
					
					/*@Override
					public float getPrefWidth() {
						return 20f;
					}*/
				};
				fillBar.pack();
				Container<ProgressBar> box = new Container<>(fillBar);
				box.fill().prefWidth(20f);
				box.pack();
				addMainGroup(box, RelPos.RIGHT);
				
				HotbarTable hotbar = new HotbarTable(fillBar);
				addMainGroup(hotbar, RelPos.BOTTOM_RIGHT);
			}
		};
		
		levelView = new LevelViewport(batch, noScaleCamera); // uses uiCamera for rendering lighting to the screen.
		
		chatOverlay = new ChatScreen(true);
		chatScreen = new ChatScreen(false);
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	void dispose() {
		levelView.dispose();
		guiStage.dispose();
		chatOverlay.dispose();
		chatScreen.dispose();
	}
	
	public void handleInput(@NotNull ClientPlayer player) {
		player.handleInput(getMouseInput());
		
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
			
			if(Gdx.input.isKeyJustPressed(Keys.T))
				ClientCore.debugTile = !ClientCore.debugTile;
			
			if(Gdx.input.isKeyJustPressed(Keys.B))
				ClientCore.debugBounds = !ClientCore.debugBounds;
			
			if(Gdx.input.isKeyJustPressed(Keys.I))
				ClientCore.debugInteract = !ClientCore.debugInteract;
		}
		// else if(Gdx.input.isKeyJustPressed(Keys.B))
		// 	ClientCore.debugChunk = !ClientCore.debugChunk;
		
		if(!ClientCore.hasMenu()) {
			if(!shift && ClientCore.input.pressingKey(Keys.T))
				chatScreen.focus("");
			
			else if(ClientCore.input.pressingKey(Keys.SLASH))
				chatScreen.focus("/");
			
			else if(ClientCore.input.pressingKey(Keys.ESCAPE))
				ClientCore.setScreen(new ConfirmScreen("Leave Server?", () -> ClientCore.getWorld().exitWorld()));
			
			else if(GameCore.debug && ClientCore.input.pressingKey(Keys.M))
				ClientCore.setScreen(new MapScreen());
		}
	}
	
	public void render(@NotNull ClientPlayer mainPlayer, Color lightOverlay, @NotNull ClientLevel level) {
		render(mainPlayer, lightOverlay, level, true);
	}
	public void render(@NotNull ClientPlayer mainPlayer, Color lightOverlay, @NotNull ClientLevel level, boolean drawGui) {
		
		levelView.render(mainPlayer.getCenter(), lightOverlay, level);
		
		batch.setProjectionMatrix(uiCamera.combined);
		if(drawGui) {
			batch.begin();
			renderGui(mainPlayer, level);
			batch.end();
			guiStage.focus();
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
	
	private void renderGui(@NotNull ClientPlayer mainPlayer, @NotNull Level level) {
		batch.setProjectionMatrix(uiCamera.combined);
		
		// draw UI for stats
		mainPlayer.drawGui(new Rectangle(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight), batch);
		
		batch.setProjectionMatrix(noScaleCamera.combined);
		
		if(!ClientCore.debugInfo) {
			if(GameCore.debug) {
				BitmapFont f = ClientCore.getFont();
				f.setColor(Color.ORANGE);
				f.draw(batch, "Debug Mode ENABLED", 0, noScaleCamera.viewportHeight - 5);
				f.setColor(Color.WHITE);
			}
			return;
		}
		
		// a list of text to display in the upper left, for debug purposes
		Array<String> debugInfo = new Array<>();
		
		if(GameCore.debug)
			debugInfo.add("Debug Mode ENABLED");
		
		debugInfo.add("Version: " + GameCore.VERSION);
		
		// player coordinates, for debug
		Rectangle playerBounds = mainPlayer.getBounds();
		debugInfo.add("X = "+(playerBounds.x-level.getWidth()/2));
		debugInfo.add("Y = "+(playerBounds.y-level.getHeight()/2));
		
		Tile playerTile = level.getTile(playerBounds);
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = level.getTile(mainPlayer.getInteractionRect());
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
	}
	
	void resize(int width, int height) {
		levelView.resize(width, height);
		guiStage.getViewport().update(width, height, true);
		chatOverlay.resize(width, height);
		// noScaleCamera.setToOrtho(false, width, height);
	}
	
	public Stage getGuiStage() { return guiStage; }
}
