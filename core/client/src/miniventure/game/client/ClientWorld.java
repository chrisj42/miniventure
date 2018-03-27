package miniventure.game.client;

import miniventure.game.GameProtocol.DatalessRequest;
import miniventure.game.GameProtocol.LevelData;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.util.Action;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.Level;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.ClientPlayer;
import miniventure.game.world.tile.TilePropertyInstanceFetcher;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientWorld extends WorldManager {
	
	/*
		This is what contains the world. GameCore will check if the world is loaded here, and if not it won't render the game screen. Though... perhaps this class should hold a reference to the game screen instead...? Because, if you don't have a world, you don't need a game screen...
		
		The world will be created with this.
		It holds references to the current game level.
		You use this class to start processes on the whole world, like saving, loading, creating.
			And accessing the main player... and respawning. Also changing the player level?
		
		Perhaps this instance can be fetched from GameCore.
		
		GameScreen... game screen won't do much, just do the rendering. 
	 */
	
	private final ServerStarter serverStarter;
	
	private final GameScreen gameScreen;
	
	private GameClient client;
	
	private ClientPlayer mainPlayer;
	
	ClientWorld(ServerStarter serverStarter, GameScreen gameScreen) {
		super(new TilePropertyInstanceFetcher(instanceTemplate -> instanceTemplate));
		
		this.serverStarter = serverStarter;
		this.gameScreen = gameScreen;
		
		client = new GameClient(); // doesn't automatically connect
	}
	
	// Update method
	
	@Override
	public void update(float delta) {
		if(!worldLoaded() || mainPlayer == null) return;
		
		MenuScreen menu = ClientCore.getScreen();
		
		ClientLevel level = mainPlayer.getLevel();
		if(level == null) {
			if(!(menu instanceof RespawnScreen))
				ClientCore.setScreen(new RespawnScreen());
			return;
		}
		
		if(menu == null)
			gameScreen.handleInput(mainPlayer);
		//mainPlayer.updateStats(delta);
		
		level.updateEntities(getEntities(level), delta);
		
		if(menu == null || !menu.usesWholeScreen())
			gameScreen.render(mainPlayer, TimeOfDay.getSkyColors(gameTime), level);
		
		gameTime += delta;
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	@Override
	public boolean worldLoaded() { return getLevelCount() > 0; }
	
	@Override
	public void createWorld(int width, int height) { createWorld(width, height, true, ""); }
	public void createWorld(String ipAddress) { createWorld(0, 0, false, ipAddress); }
	private void createWorld(int width, int height, boolean startServer, String ipAddress) {
		LoadingScreen loadingScreen = new LoadingScreen();
		ClientCore.setScreen(loadingScreen);
		
		gameTime = 0;
		clearLevels();
		
		new Thread(() -> {
			// with a server running, attempt to connect the client. If successful, it will set the screen to null.
			Action connect = () -> client.connectToServer(loadingScreen, "localhost");
			
			if(startServer) // start server, then connect
				serverStarter.startServer(width, height, connect);
			else // server should already be running; just connect
				client.connectToServer(loadingScreen, ipAddress);
			
		}).start();
	}
	
	@Override
	public void exitWorld(boolean save) { // returns to title screen
		// set menu to main menu, and dispose of level/world resources
		mainPlayer = null;
		clearLevels();
		ClientCore.setScreen(new MainMenu());
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	public void addLevel(LevelData data) {
		addLevel(new ClientLevel(this, data.depth, data.width, data.height));
	}
	
	public void loadChunk(ChunkData data) {
		ClientLevel level = getLevel(data.levelDepth);
		if(level == null) {
			System.err.println("client could not load chunk because level is null");
			return;
		}
		
		level.loadChunk(new Chunk(level, data));
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public boolean isKeepAlive(@NotNull WorldObject obj) { return obj.equals(mainPlayer); }
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	public void spawnPlayer(SpawnData data) {
		ClientPlayer mainPlayer = new ClientPlayer(data);
		PositionUpdate newPos = data.playerData.positionUpdate;
		mainPlayer.moveTo(newPos.x, newPos.y, newPos.z);
		
		if(this.mainPlayer != null)
			this.mainPlayer.remove();
		
		this.mainPlayer = mainPlayer;
		
		Level level = getLevel(newPos.levelDepth);
		if(level != null)
			setEntityLevel(mainPlayer, level);
		else
			System.err.println("could not add main player, default level is null");
	}
	
	public void respawnPlayer() {
		LoadingScreen loader = new LoadingScreen();
		ClientCore.setScreen(loader);
		loader.pushMessage("respawning...");
		client.send(DatalessRequest.Respawn);
	}
	
	
	
	/*  --- GET METHODS --- */
	
	
	@Override
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		Array<WorldObject> playerHolder = new Array<>(WorldObject.class);
		if(mainPlayer != null && mainPlayer.getLevel() == level)
			playerHolder.add(mainPlayer);
		
		return playerHolder;
	}
	
	public GameClient getClient() { return client; }
	
	@Override
	public ClientLevel getLevel(int depth) { return (ClientLevel) super.getLevel(depth); }
	
	@Override
	public ClientLevel getEntityLevel(Entity e) { return (ClientLevel) super.getEntityLevel(e); }
	
	public ClientPlayer getMainPlayer() { return mainPlayer; }
	
	@Override
	public String toString() { return "ClientWorld"; }
}
