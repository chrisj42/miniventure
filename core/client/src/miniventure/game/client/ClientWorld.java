package miniventure.game.client;

import miniventure.game.GameProtocol.DatalessRequest;
import miniventure.game.GameProtocol.LevelData;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.GameProtocol.WorldData;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.util.Action;
import miniventure.game.world.*;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.ClientPlayer;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.TileEnumMapper;

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
	
	private final ServerStarter serverStarter;
	private String ipAddress;
	
	private final GameScreen gameScreen;
	
	private GameClient client;
	
	private ClientPlayer mainPlayer;
	
	private boolean doDaylightCycle = true;
	
	ClientWorld(ServerStarter serverStarter, GameScreen gameScreen) {
		super(new TileEnumMapper<>(tileType -> tileType));
		
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
		ClientCore.getClient().disconnect();
		mainPlayer = null;
		clearLevels();
		ClientCore.setScreen(new MainMenu());
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
		
		level.loadChunk(new Chunk(level, data, (p, types, dataMaps) -> new ClientTile(level, p.x, p.y, types, dataMaps)));
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public boolean isKeepAlive(@NotNull WorldObject obj) { return obj.equals(mainPlayer); }
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	public void spawnPlayer(SpawnData data) {
		// this has to come before making the new client player, because it has the same eid and so will overwrite some things.
		if(this.mainPlayer != null)
			this.mainPlayer.remove();
		
		ClientPlayer mainPlayer = new ClientPlayer(data);
		PositionUpdate newPos = data.playerData.positionUpdate;
		mainPlayer.moveTo(newPos.x, newPos.y, newPos.z);
		
		gameScreen.getGuiStage().clear();
		gameScreen.getGuiStage().addActor(mainPlayer.getHands().getHotbarTable());
		
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
