package miniventure.game.world.management;

import miniventure.game.GameCore;
import miniventure.game.client.*;
import miniventure.game.item.InventoryOverlay;
import miniventure.game.network.GameProtocol;
import miniventure.game.network.GameProtocol.DatalessRequest;
import miniventure.game.network.GameProtocol.LevelData;
import miniventure.game.network.GameProtocol.Login;
import miniventure.game.network.GameProtocol.SpawnData;
import miniventure.game.network.GameProtocol.WorldData;
import miniventure.game.network.PacketPipe;
import miniventure.game.network.PacketPipe.PacketPipeReader;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.util.function.Action;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.file.WorldDataSet;
import miniventure.game.world.level.ClientLevel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class ClientWorld extends LevelManager {
	
	// FIXME Implement the system below. Currently the GameView situation isn't as it says; do the thing about making the game view a Screen, and MenuScreen a screen, etc.
	
	/* This controls world management, on the client side.
		Only one instance is created, when the application starts up.
			- this does not initialize the game view or the network client, it's just basic construction.
		
		Most things are initialized when joinWorld or startLocalWorld is called.
			- a "network" client is created, made to handle either a local or remote connection
			- the server code is called to load the world with the given world data
			- client tries to connect to the server
				- assuming success, the client begins handling server packets.
		
		The server works as follows:
			- world loading and startup is standard; updates start immediately once the world has loaded successfully
			- when a player joins, they might not get the world info right away; if they are new to the server, then the server tells the client to play the intro sequence.
				- while the client plays the intro, the server doesn't add the player to the server world just yet. It waits for the client to finish the intro.
			- intro sequence may be skipped by the user, btw. Still counts as finishing the intro.
			- When the server hears the client finished the intro, it takes note of the fact in the user file. Then, THIS is when the player actually joins the server world itself.
				- no loading screen here. It's just normal chunk loading.
	 */
	
	private final ServerManager serverManager;
	private String ipAddress;
	private int lastPort;
	
	private GameScreen gameScreen;
	
	private GameClient client;
	
	private ClientPlayer mainPlayer;
	
	private boolean doDaylightCycle = true;
	
	// a ClientWorld is created 
	public ClientWorld(ServerManager serverManager) {
		this.serverManager = serverManager;
		
	}
	
	// Update method
	
	@Override
	public void update(float delta) {
		if(!worldLoaded() || mainPlayer == null) {
			super.update(delta);
			return;
		}
		
		MenuScreen menu = ClientCore.getScreen();
		
		ClientLevel level = mainPlayer.getLevel();
		if(level == null) return;
		
		if(menu == null)
			gameScreen.handleInput();
		
		level.update(delta);
		
		if(menu == null || !menu.usesWholeScreen())
			gameScreen.render(getLightingOverlay(), level);
		
		super.update(delta);
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	public void init(WorldData data) {
		gameTime = data.gameTime;
		daylightOffset = data.daylightOffset;
		this.doDaylightCycle = data.doDaylightCycle;
	}
	
	@Override protected boolean doDaylightCycle() { return doDaylightCycle; }
	
	
	public void rejoinWorld() { joinWorld(ipAddress, lastPort); }
	public void joinWorld(String ipAddress, int port) {
		ClientCore.stopMusic();
		LoadingScreen loadingScreen = new LoadingScreen();
		ClientCore.setScreen(loadingScreen);
		
		clearEntityIdMap();
		
		if(client != null)
			client.disconnect();
		
		NetworkClient netClient = new NetworkClient();
		this.client = netClient;
		
		this.ipAddress = ipAddress;
		this.lastPort = port;
		
		new Thread(() -> {
			// connect to an existing server.
			netClient.connectToServer(loadingScreen, null, ipAddress, port, success -> {
				if(!success)
					client = null;
			});
		}).start();
	}
	
	// loads the world as a multiplayer server without having to use the command line.
	public void startLocalServer(WorldDataSet worldInfo) {
		this.ipAddress = "localhost";
		
		// ClientCore.stopMusic();
		LoadingScreen loadingScreen = new LoadingScreen();
		// loadingScreen.pushMessage("Initializing local server");
		ClientCore.setScreen(loadingScreen);
		
		NetworkClient netClient = new NetworkClient();
		this.client = netClient;
		
		new Thread(() -> {
			
			serverManager.startMPServer(netClient, worldInfo, loadingScreen, success -> {
				if(!success)
					client = null;
			});
			
		}).start();
	}
	
	// given a pre-initialized WorldFile (ie the world has already been read from file and/or generated successfully), this method starts the process of loading a world in single player mode.
	// when a world is first loaded, the intro plays. After the intro is done, the game starts; however, the world still loads before the intro plays. It just doesn't get updated until the intro finishes and the game view appears.
	// this should be run in a separate thread from the rendering thread.
	public void startLocalWorld(WorldDataSet worldInfo, LoadingScreen loadingScreen) {
		// ClientCore.stopMusic();
		// loadingScreen.pushMessage("Initializing private server");
		
		PacketPipe pipe1 = new PacketPipe("LocalClient packet reader");
		PacketPipe pipe2 = new PacketPipe("LocalServer packet reader");
		
		PacketPipeWriter serverOut = pipe1.getPipeWriter();
		PacketPipeReader clientIn = pipe1.getPipeReader();
		
		PacketPipeWriter clientOut = pipe2.getPipeWriter();
		PacketPipeReader serverIn = pipe2.getPipeReader();
		
		LocalClient localClient = new LocalClient(clientIn, clientOut);
		this.client = localClient;
		
		// start a server, and attempt to connect the client. If successful, it will set the screen to null; if not, the new server will be closed.
		
		loadingScreen.pushMessage("creating mock server", true);
		
		if(!serverManager.startSPServer(worldInfo, serverIn, serverOut, loadingScreen))
			return; // failed.
		
		loadingScreen.pushMessage("adding player to world", true);
		clientIn.start();
		
		// loadingScreen.popMessage(); // matched with the "init private server" msg at the start of this method.
		
		localClient.send(new Login(GameProtocol.HOST, GameCore.VERSION));
		
		/*if(errorMessage != null) {
			Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("Error starting world: "+e.getMessage())));
			return;
		}*/
	}
	
	// returns to title screen; this ClientWorld instance is still capable of supporting future worlds.
	@Override
	public void exitWorld() {
		// set menu to main menu, and dispose of level/world resources
		client.disconnect();
		// ClientCore.getClient().disconnect();
		mainPlayer = null;
		clearEntityIdMap();
		// serverManager.closeServer();
		ClientCore.setScreen(new MainMenu());
		client = null;
	}
	
	// TODO add lighting overlays, based on level and/or time of day, depending on the level and perhaps other things.
	private Color getLightingOverlay() {
		//Array<Color> colors = new Array<>(TimeOfDay.getSkyColors(daylightOffset));
		return TimeOfDay.getSkyColor(daylightOffset);
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	public void setLevel(LevelData data, LoadingScreen loader) {
		mainPlayer = null;
		loader.pushMessage("Parsing new level data");
		setLevel(new ClientLevel(this, data.levelId, data.tiles));
		loader.editMessage("awaiting spawn data", true);
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
	
	
	/*@Override
	public void deregisterEntity(final int eid) {
		if(mainPlayer != null && eid == mainPlayer.getId()) {
			if(worldLoaded()) {
				Gdx.app.postRunnable(() -> {
					LoadingScreen loader = new LoadingScreen();
					loader.pushMessage("Loading level");
					ClientCore.setScreen(loader);
				});
			}
		}
		else
			super.deregisterEntity(eid);
	}*/
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	public void spawnPlayer(SpawnData data, Action callback) {
		// this has to come before making the new client player, because it has the same eid and so will overwrite some things.
		if(this.mainPlayer != null) {
			super.deregisterEntity(this.mainPlayer.getId());
		}
		
		Gdx.app.postRunnable(() -> {
			InventoryOverlay invScreen = new InventoryOverlay(new OrthographicCamera());
			this.mainPlayer = new ClientPlayer(data, invScreen);
			registerEntity(mainPlayer);
			gameScreen = ClientCore.newGameScreen(new GameScreen(mainPlayer, gameScreen, invScreen));
			callback.act();
		});
	}
	
	// libGDX thread only
	public void respawnPlayer() {
		LoadingScreen loader = new LoadingScreen();
		ClientCore.setScreen(loader);
		loader.pushMessage("Respawning");
		client.send(DatalessRequest.Respawn);
	}
	
	public RespawnScreen getRespawnScreen() { return new RespawnScreen(mainPlayer, getLightingOverlay(), gameScreen); }
	
	
	
	/*  --- GET METHODS --- */
	
	
	public GameClient getClient() { return client; }
	
	@Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public ClientLevel getLevel(int levelId) { return (ClientLevel) super.getLevel(levelId); }
	
	@Override
	public ClientLevel getEntityLevel(Entity e) { return (ClientLevel) super.getEntityLevel(e); }
	
	public ClientPlayer getMainPlayer() { return mainPlayer; }
	
	public boolean hasRenderableLevel() { return worldLoaded() && mainPlayer != null && mainPlayer.getLevel() != null; }
	
	public boolean isLocalWorld() { return serverManager.isHosting(); }
	
	public void saveWorld() { serverManager.save(); }
	
	@Override
	public String toString() { return "ClientWorld"; }
}
