package miniventure.game.client;

import javax.swing.JOptionPane;

import java.awt.EventQueue;

import miniventure.game.GameCore;
import miniventure.game.screen.ChatScreen;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.Level;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.entity.mob.ClientPlayer;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import org.jetbrains.annotations.NotNull;

public class GameScreen {
	
	@NotNull private final LevelViewport levelView;
	
	private SpriteBatch batch = GameCore.getBatch();
	private Stage guiStage;
	
	private final OrthographicCamera uiCamera;
	
	final ChatScreen chatOverlay, chatScreen;
	private boolean showDebug = false;
	
	public GameScreen() {
		uiCamera = new OrthographicCamera();
		guiStage = new Stage(new ExtendViewport(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT, uiCamera), batch);
		
		levelView = new LevelViewport(batch, uiCamera);
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		chatOverlay = new ChatScreen(true);
		chatScreen = new ChatScreen(false);
	}
	
	void dispose() {
		levelView.dispose();
		guiStage.dispose();
	}
	
	private boolean dialog = false;
	public void handleInput(@NotNull ClientPlayer player) {
		if(dialog) return;
		
		player.handleInput(getMouseInput());
		
		boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		if(shift && GameCore.debug && Gdx.input.isKeyJustPressed(Keys.S)) {
			EventQueue.invokeLater(() -> {
				String newSpeed = JOptionPane.showInputDialog("Enter new Player Speed:", player.getSpeed());
				try {
					float value = Float.parseFloat(newSpeed);
					player.setSpeed(value);
				} catch(NumberFormatException ignored) {}
			});
		}
		
		levelView.handleInput();
		
		if(shift && Gdx.input.isKeyJustPressed(Keys.D) && !Gdx.input.isKeyPressed(Keys.TAB))
			showDebug = !showDebug;
		
		if(shift && ClientCore.input.pressingKey(Keys.T)) {
			//ClientCore.getClient().send(DatalessRequest.Tile); // debug
			ClientPlayer p = ClientCore.getWorld().getMainPlayer();
			try {
				//noinspection ConstantConditions
				Tile t = p.getLevel().getClosestTile(p.getCenter());
				t.updateSprites();
				for(Tile o: t.getAdjacentTiles(true))
					o.updateSprites();
			} catch(NullPointerException ignored) {}
		}
		
		if(!ClientCore.hasMenu()) {
			if(!shift && ClientCore.input.pressingKey(Keys.T))
				chatScreen.focus("");
			
			else if(ClientCore.input.pressingKey(Keys.SLASH))
				chatScreen.focus("/");
			
			else if(ClientCore.input.pressingKey(Keys.ESCAPE)) {
				dialog = true;
				EventQueue.invokeLater(() -> {
					int choice = JOptionPane.showConfirmDialog(null, "Leave Server?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(choice == JOptionPane.YES_OPTION)
						Gdx.app.postRunnable(() -> ClientCore.getWorld().exitWorld());
					dialog = false;
				});
			}
		}
	}
	
	public void render(@NotNull ClientPlayer mainPlayer, Color lightOverlay, @NotNull ClientLevel level) {
		
		levelView.render(mainPlayer.getCenter(), lightOverlay, level);
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		renderGui(mainPlayer, level);
		batch.end();
		guiStage.act(Gdx.graphics.getDeltaTime());
		guiStage.draw();
		
		if(!(ClientCore.getScreen() instanceof ChatScreen)) {
			chatOverlay.act(Gdx.graphics.getDeltaTime());
			chatOverlay.draw();
		}
	}
	
	private static Vector2 getMouseInput() {
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
	
	private void renderGui(@NotNull ClientPlayer mainPlayer, @NotNull Level level) {
		batch.setProjectionMatrix(uiCamera.combined);
		
		// draw UI for current item, and stats
		mainPlayer.drawGui(new Rectangle(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight), batch);
		
		if(!showDebug) {
			if(GameCore.debug) {
				BitmapFont f = GameCore.getFont();
				f.setColor(Color.ORANGE);
				f.draw(batch, "Debug Mode ENABLED", 0, uiCamera.viewportHeight - 5);
			}
			return;
		}
		
		// a list of text to display in the upper left, for debug purposes
		Array<String> debugInfo = new Array<>();
		
		if(GameCore.debug)
			debugInfo.add("Debug Mode ENABLED");
		
		debugInfo.add("Version " + GameCore.VERSION);
		
		// player coordinates, for debug
		Rectangle playerBounds = mainPlayer.getBounds();
		debugInfo.add("X = "+(playerBounds.x-level.getWidth()/2));
		debugInfo.add("Y = "+(playerBounds.y-level.getHeight()/2));
		
		Tile playerTile = level.getClosestTile(playerBounds);
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = level.getClosestTile(mainPlayer.getInteractionRect());
		debugInfo.add("Looking at: " + (interactTile == null ? "Null" : interactTile.toLocString().replace("Client", "")));
		
		debugInfo.add("Mobs in level: " + level.getMobCount()+"/"+level.getMobCap());
		debugInfo.add("Total Entities: " + level.getEntityCount());
		
		debugInfo.add("Time: " + TimeOfDay.getTimeString(ClientCore.getWorld().getDaylightOffset()));
		
		BitmapFont font = GameCore.getFont();
		if(GameCore.debug) font.setColor(Color.ORANGE);
		for(int i = 0; i < debugInfo.size; i++) {
			if(GameCore.debug && i == 1)
				font.setColor(Color.WHITE);
			font.draw(batch, debugInfo.get(i), 0, uiCamera.viewportHeight - 5 - 15 * i);
		}
	}
	
	void resize(int width, int height) { levelView.resize(width, height); }
	
	public Stage getGuiStage() { return guiStage; }
}
