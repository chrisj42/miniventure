package miniventure.game.client;

import miniventure.game.GameProtocol.DatalessRequest;
import miniventure.game.GameProtocol.LevelData;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.GameProtocol.WorldData;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.Level;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.ClientPlayer;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.TileEnumMapper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
	
	private final ServerManager serverManager;
	private String ipAddress;
	
	private final GameScreen gameScreen;
	
	private GameClient client;
	
	private ClientPlayer mainPlayer;
	
	private boolean doDaylightCycle = true;
	
	ClientWorld(ServerManager serverManager, GameScreen gameScreen) {
		super(new TileEnumMapper<>(tileType -> tileType));
		
		this.serverManager = serverManager;
		this.gameScreen = gameScreen;
		
		client = new GameClient(); // doesn't automatically connect
	}
	
	// Update method
	
	@Override
	public void update(float delta) {
		if(!worldLoaded() || mainPlayer == null) return;
		
		MenuScreen menu = ClientCore.getScreen();
		
		ClientLevel level = mainPlayer.getLevel();
		if(level == null) return;
		
		if(menu == null)
			gameScreen.handleInput(mainPlayer);
		
		level.updateEntities(getEntities(level), delta);
		
		if(menu == null || !menu.usesWholeScreen())
			gameScreen.render(mainPlayer, getLightingOverlay(), level);
		
		super.update(delta);
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	void init(WorldData data) {
		gameTime = data.gameTime;
		daylightOffset = data.daylightOffset;
		this.doDaylightCycle = data.doDaylightCycle;
	}
	
	@Override protected boolean doDaylightCycle() { return doDaylightCycle; }
	
	@Override
	public boolean worldLoaded() { return getLevelCount() > 0; }
	
	@Override
	public void createWorld(int width, int height) { createWorld(width, height, true, ""); }
	public void rejoinWorld() { createWorld(0, 0, false, ipAddress); }
	public void createWorld(String ipAddress) { createWorld(0, 0, false, ipAddress); }
	private void createWorld(int width, int height, boolean startServer, String ipAddress) {
		ClientCore.stopMusic();
		LoadingScreen loadingScreen = new LoadingScreen();
		ClientCore.setScreen(loadingScreen);
		
		gameTime = 0;
		daylightOffset = 0;
		clearLevels();
		
		this.ipAddress = ipAddress;
		
		new Thread(() -> {
			// with a server running, attempt to connect the client. If successful, it will set the screen to null.
			ValueFunction<Boolean> connect = serverSuccess -> {
				if(!serverSuccess)
					ClientCore.setScreen(new ErrorScreen("Error starting local server. The port may already be in use.<br>Press 'reconnect' to attempt to connect to the existing server."));
				else
					client.connectToServer(loadingScreen, "localhost", success -> {
						if(!success) {
							serverManager.closeServer();
							client = new GameClient();
						}
					});
			};
			
			if(startServer) // start server, then connect
				serverManager.startServer(width, height, connect);
			else // server should already be running; just connect
				client.connectToServer(loadingScreen, ipAddress, success -> {
					if(!success)
						client = new GameClient();
				});
			
		}).start();
	}
	
	@Override
	public void exitWorld(boolean save) { // returns to title screen
		// set menu to main menu, and dispose of level/world resources
		ClientCore.getClient().disconnect();
		mainPlayer = null;
		clearLevels();
		ClientCore.setScreen(new MainMenu());
		client = new GameClient();
	}
	
	// TODO add lighting overlays, based on level and/or time of day, depending on the level and perhaps other things.
	private Color getLightingOverlay() {
		//Array<Color> colors = new Array<>(TimeOfDay.getSkyColors(daylightOffset));
		return TimeOfDay.getSkyColor(daylightOffset);
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
		
		level.loadChunk(new Chunk(level, data, (x, y, types, dataMaps) -> new ClientTile(level, x, y, types, dataMaps)));
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public boolean isKeepAlive(@NotNull WorldObject obj) { return obj.equals(mainPlayer); }
	
	@Override
	public void deregisterEntity(final int eid) {
		if(mainPlayer != null && eid == mainPlayer.getId()) {
			Gdx.app.postRunnable(() -> ClientCore.setScreen(new RespawnScreen(mainPlayer.getCenter(), getLightingOverlay(), mainPlayer.getLevel(), gameScreen.getLevelView())));
		}
		else
			super.deregisterEntity(eid);
	}
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	public void spawnPlayer(SpawnData data) {
		// this has to come before making the new client player, because it has the same eid and so will overwrite some things.
		//fixme hotbar table
		if(this.mainPlayer != null) {
			super.deregisterEntity(this.mainPlayer.getId());
			// gameScreen.getHudPanel().remove(this.mainPlayer.getHands().getHotbarTable());
		}
		
		ClientPlayer mainPlayer = new ClientPlayer(data);
		PositionUpdate newPos = data.playerData.positionUpdate;
		mainPlayer.moveTo(newPos.x, newPos.y, newPos.z);
		
		// gameScreen.getHudPanel().add(mainPlayer.getHands().getHotbarTable());
		
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
		loader.pushMessage("Respawning...");
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
