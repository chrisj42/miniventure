package miniventure.game.client;

import javax.swing.JOptionPane;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.DatalessRequest;
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
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class GameScreen {
	
	@NotNull private final LevelViewport levelView;
	
	private SpriteBatch batch = GameCore.getBatch();
	
	private final OrthographicCamera uiCamera;
	
	final ChatScreen chatOverlay, chatScreen;
	
	public GameScreen() {
		uiCamera = new OrthographicCamera();
		
		levelView = new LevelViewport(batch, uiCamera);
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		chatOverlay = new ChatScreen(true);
		chatScreen = new ChatScreen(false);
	}
	
	void dispose() { levelView.dispose(); }
	
	private boolean dialog = false;
	public void handleInput(@NotNull ClientPlayer player) {
		if(dialog) return;
		
		player.handleInput(getMouseInput());
		
		levelView.handleInput();
		
		//if(Gdx.input.isKeyJustPressed(Keys.R) && Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
		//	GameCore.getWorld().createWorld(0, 0);
		
		if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Keys.T))
			ClientCore.getClient().send(DatalessRequest.Tile); // debug
		
		else if(Gdx.input.isKeyJustPressed(Keys.T)) {
			chatScreen.focus();
		}
		else if(Gdx.input.isKeyJustPressed(Keys.SLASH)) {
			chatScreen.focus("/");
		}
		
		else if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			dialog = true;
			new Thread(() -> {
				int choice = JOptionPane.showConfirmDialog(null, "Leave Server?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(choice == JOptionPane.YES_OPTION)
					Gdx.app.postRunnable(() -> ClientCore.getWorld().exitWorld());
				dialog = false;
			}).start();
		}
	}
	
	public void render(@NotNull ClientPlayer mainPlayer, Color[] lightOverlays, @NotNull ClientLevel level) {
		
		levelView.render(mainPlayer.getCenter(), lightOverlays, level);
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		renderGui(mainPlayer, level);
		batch.end();
		
		if(!(ClientCore.getScreen() instanceof ChatScreen)) {
			chatOverlay.act(Gdx.graphics.getDeltaTime());
			chatOverlay.draw();
		}
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
	
	private void renderGui(@NotNull ClientPlayer mainPlayer, @NotNull Level level) {
		batch.setProjectionMatrix(uiCamera.combined);
		
		// draw UI for current item, and stats
		mainPlayer.drawGui(new Rectangle(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight), batch);
		
		
		// a list of text to display in the upper left, for debug purposes
		Array<String> debugInfo = new Array<>(); 
		debugInfo.add("Version " + GameCore.VERSION);
		
		// player coordinates, for debug
		Rectangle playerBounds = mainPlayer.getBounds();
		debugInfo.add("X = "+(playerBounds.x-level.getWidth()/2));
		debugInfo.add("Y = "+(playerBounds.y-level.getHeight()/2));
		
		Tile playerTile = level.getClosestTile(playerBounds);
		debugInfo.add("Tile = " + (playerTile == null ? "Null" : playerTile.getType()));
		Tile interactTile = level.getClosestTile(mainPlayer.getInteractionRect());
		debugInfo.add("Looking at: " + (interactTile == null ? "Null" : interactTile.toLocString().replace("Client", "")));
		
		debugInfo.add("Entities in level: " + level.getEntityCount()+"/"+level.getEntityCap());
		
		debugInfo.add("Time: " + TimeOfDay.getTimeString(ClientCore.getWorld().getDaylightOffset()));
		
		BitmapFont font = GameCore.getFont();
		for(int i = 0; i < debugInfo.size; i++)
			font.draw(batch, debugInfo.get(i), 0, uiCamera.viewportHeight-5-15*i);
	}
	
	void resize(int width, int height) { levelView.resize(width, height); }
}
