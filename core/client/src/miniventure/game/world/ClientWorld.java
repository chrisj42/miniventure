package miniventure.game.world;

import java.util.Random;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.DatalessRequest;
import miniventure.game.GameProtocol.LevelData;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.GameProtocol.WorldData;
import miniventure.game.client.ClientCore;
import miniventure.game.client.GameClient;
import miniventure.game.client.GameScreen;
import miniventure.game.client.ServerManager;
import miniventure.game.file.WorldFile;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.InputScreen;
import miniventure.game.screen.InputScreen.CircularFunction;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.worldgen.WorldConfig;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

public class ClientWorld extends LevelManager {
	
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
	
	public ClientWorld(ServerManager serverManager, GameScreen gameScreen) {
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
		
		level.update(delta);
		
		if(menu == null || !menu.usesWholeScreen())
			gameScreen.render(mainPlayer, getLightingOverlay(), level);
		
		super.update(delta);
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	public void init(WorldData data) {
		gameTime = data.gameTime;
		daylightOffset = data.daylightOffset;
		this.doDaylightCycle = data.doDaylightCycle;
	}
	
	@Override protected boolean doDaylightCycle() { return doDaylightCycle; }
	
	
	public void rejoinWorld() { joinWorld(ipAddress); }
	public void joinWorld(String ipAddress) {
		ClientCore.stopMusic();
		LoadingScreen loadingScreen = new LoadingScreen();
		ClientCore.setScreen(loadingScreen);
		
		clearWorld();
		
		this.ipAddress = ipAddress;
		
		new Thread(() -> {
			// connect to an existing server.
			client.connectToServer(loadingScreen, ipAddress, success -> {
				if(!success)
					client = new GameClient();
			});
		}).start();
	}
	
	// todo replace this with an actual WorldSelectScreen; this screen checks for existing worlds and gets quick details on them, and also will be responsible for providing a WorldFile instance to startWorld().
	public InputScreen getNewWorldInput() {
		return new InputScreen("Name your new world:", worldname -> {
			ClientCore.setScreen(new InputScreen("Average island diameter (default "+GameCore.DEFAULT_WORLD_SIZE+"):", new CircularFunction<>((size, self) -> {
				boolean valid;
				int sizeVal = 0;
				try {
					sizeVal = Integer.parseInt(size);
					valid = sizeVal >= 10;
				} catch(NumberFormatException e) {
					valid = false;
				}
				
				if(!valid)
					ClientCore.setScreen(new InputScreen(new String[] {
						"Average island diameter (default "+GameCore.DEFAULT_WORLD_SIZE+"):",
						"Value must be an integer >= 10"
					}, self));
				else
					startWorld(new WorldConfig(worldname, sizeVal, sizeVal, new Random().nextLong()));
			})));
		});
	}
	
	// given a pre-initialized WorldFile (ie the world has already been read from file and/or generated successfully), this method starts up a local server on it, and logs in.
	// todo due to possibility of different world saves, more consideration towards port variability should be considered. Perhaps instead of giving an error, the server should try another port...? Only because this is local. Though maybe port *should* be considered, and saved even?
	public void startWorld(WorldFile worldFile) {
		ClientCore.stopMusic();
		LoadingScreen loadingScreen = new LoadingScreen();
		ClientCore.setScreen(loadingScreen);
		
		this.ipAddress = "localhost";
		
		new Thread(() -> {
			// start a server, and attempt to connect the client. If successful, it will set the screen to null; if not, the new server will be closed.
			
			serverManager.startServer(worldFile, serverSuccess -> {
				if(!serverSuccess)
					Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("Error starting local server; the port may already be in use.\nPress 'reconnect' to attempt to connect to the existing server.")));
				else
					client.connectToServer(loadingScreen, "localhost", success -> {
						if(!success) {
							serverManager.closeServer();
							client = new GameClient();
						}
					});
			});
			
		}).start();
	}
	
	// returns to title screen; this ClientWorld instance is still capable of supporting future worlds.
	@Override
	public void exitWorld() {
		// set menu to main menu, and dispose of level/world resources
		ClientCore.getClient().disconnect();
		mainPlayer = null;
		clearWorld();
		ClientCore.setScreen(new MainMenu());
		client = new GameClient();
	}
	
	// TODO add lighting overlays, based on level and/or time of day, depending on the level and perhaps other things.
	private Color getLightingOverlay() {
		//Array<Color> colors = new Array<>(TimeOfDay.getSkyColors(daylightOffset));
		return TimeOfDay.getSkyColor(daylightOffset);
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	public void setLevel(LevelData data) {
		// todo hold the player in a loading screen until a 
		setLevel(new ClientLevel(this, data.levelId, data.tiles));
	}
	
	/*public void loadChunk(ChunkData data) {
		ClientLevel level = getLevel(data.levelId);
		if(level == null) {
			System.err.println("client could not load chunk because level is null");
			return;
		}
		
		level.loadChunk(new Chunk(level, data, (x, y, types, dataMaps) -> new ClientTile(level, x, y, types, dataMaps)));
	}*/
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public void deregisterEntity(final int eid) {
		if(mainPlayer != null && eid == mainPlayer.getId()) {
			Gdx.app.postRunnable(() -> ClientCore.setScreen(new RespawnScreen(mainPlayer, getLightingOverlay(), gameScreen)));
		}
		else
			super.deregisterEntity(eid);
	}
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	public void spawnPlayer(SpawnData data) {
		// this has to come before making the new client player, because it has the same eid and so will overwrite some things.
		if(this.mainPlayer != null) {
			super.deregisterEntity(this.mainPlayer.getId());
			// gameScreen.getHudPanel().remove(this.mainPlayer.getHands().getHotbarTable());
		}
		
		this.mainPlayer = new ClientPlayer(data);
		
		// gameScreen.getHudPanel().add(mainPlayer.getHands().getHotbarTable());
	}
	
	public void respawnPlayer() {
		LoadingScreen loader = new LoadingScreen();
		ClientCore.setScreen(loader);
		loader.pushMessage("Respawning...");
		client.send(DatalessRequest.Respawn);
	}
	
	
	
	/*  --- GET METHODS --- */
	
	
	public GameClient getClient() { return client; }
	
	@Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public ClientLevel getLevel(int levelId) { return (ClientLevel) super.getLevel(levelId); }
	
	@Override
	public ClientLevel getEntityLevel(Entity e) { return (ClientLevel) super.getEntityLevel(e); }
	
	public ClientPlayer getMainPlayer() { return mainPlayer; }
	
	@Override
	public String toString() { return "ClientWorld"; }
}
