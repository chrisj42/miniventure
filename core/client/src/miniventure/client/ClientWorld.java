package miniventure.client;

import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.DatalessRequest;
import miniventure.game.TimeOfDay;
import miniventure.game.WorldManager;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entitynew.Entity;
import miniventure.server.ServerCore;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientWorld implements WorldManager {
	
	/*
		This is what contains the world. GameCore will check if the world is loaded here, and if not it won't render the game screen. Though... perhaps this class should hold a reference to the game screen instead...? Because, if you don't have a world, you don't need a game screen...
		
		The world will be created with this.
		It holds references to the current game level.
		You use this class to start processes on the whole world, like saving, loading, creating.
			And accessing the main player... and respawning. Also changing the player level?
		
		Perhaps this instance can be fetched from GameCore.
		
		GameScreen... game screen won't do much, just do the rendering. 
	 */
	
	private final HashMap<Integer, Entity> entityIDMap = new HashMap<>();
	
	private final GameScreen gameScreen;
	
	private GameClient client;
	
	private Player mainPlayer;
	private float gameTime;
	
	ClientWorld(GameScreen gameScreen) {
		this.gameScreen = gameScreen;
		
		client = new GameClient(); // doesn't automatically connect
	}
	
	@Override
	public boolean worldLoaded() { return Level.hasLevels(); }
	
	@Override
	public void createWorld(int width, int height) {
		LoadingScreen loadingScreen = new LoadingScreen();
		GameCore.setScreen(loadingScreen);
		
		gameTime = 0;
		Level.clearLevels();
		
		new Thread(() -> {
			ServerCore.initServer(width, height);
			
			// server running, and world loaded; now, get the server world updating
			new Thread(ServerCore::run).start();
			
			// finally, attempt to connect the client. If successful, it will set the screen to null.
			client.connectToServer(loadingScreen, "localhost");
		}).start();
	}
	
	@Override
	public void update(float delta) {
		if(!worldLoaded() || mainPlayer == null) return;
		
		MenuScreen menu = GameCore.getScreen();
		
		Level level = mainPlayer.getLevel();
		if(level == null) {
			if(!(menu instanceof RespawnScreen))
				GameCore.setScreen(new RespawnScreen());
			return;
		}
		
		gameScreen.handleInput(mainPlayer);
		
		if(menu == null || !menu.usesWholeScreen())
			gameScreen.render(mainPlayer, TimeOfDay.getSkyColors(gameTime), level);
		
		gameTime += delta;
	}
	
	@Override
	public void exitWorld(boolean save) { // returns to title screen
		// set menu to main menu, and dispose of level/world resources
		mainPlayer = null;
		Level.clearLevels();
		GameCore.setScreen(new MainMenu(this));
	}
	
	public void spawnPlayer(float x, float y) {
		Level level = Level.getLevel(0);//mainPlayer == null ?  : mainPlayer.getLevel();
		if(mainPlayer != null)
			mainPlayer.remove();
		
		mainPlayer = new Player();
		
		if(level != null)
			level.addEntity(mainPlayer, x, y, false);
	}
	
	public void respawnPlayer() {
		LoadingScreen loader = new LoadingScreen();
		GameCore.setScreen(loader);
		loader.pushMessage("respawning...");
		client.send(DatalessRequest.Respawn);
	}
	
	@Override
	public boolean isKeepAlive(@NotNull WorldObject obj) { return obj.equals(mainPlayer); }
	
	@Override
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		Array<WorldObject> playerHolder = new Array<>(WorldObject.class);
		if(mainPlayer != null && mainPlayer.getLevel() == level)
			playerHolder.add(mainPlayer);
		
		return playerHolder;
	}
	
	@Override
	public float getGameTime() { return gameTime; }
	
	@Override
	public int generateEntityID(Entity entity) {
		int eid;
		do {
			eid = MathUtils.random.nextInt();
		} while(entityIDMap.containsKey(eid));
		entityIDMap.put(eid, entity);
		return eid;
	}
}
