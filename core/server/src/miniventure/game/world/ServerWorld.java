package miniventure.game.world;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.GameProtocol.EntityRemoval;
import miniventure.game.GameProtocol.MapRequest;
import miniventure.game.GameProtocol.WorldData;
import miniventure.game.item.FoodType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolType;
import miniventure.game.server.GameServer;
import miniventure.game.util.SyncObj;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.SaveLoadInterface.LevelCache;
import miniventure.game.world.SaveLoadInterface.PlayerInfo;
import miniventure.game.world.SaveLoadInterface.WorldDataSet;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.NotNull;

public class ServerWorld extends WorldManager {
	
	/*
		The ServerWorld is the only WorldManager that can save a world to file; or load it technically,
		though ClientWorld has that method too... but it just sends a request to the ServerWorld. :P
		
		ServerWorld is also the only world that will ever have multiple islands loaded simultaneously.
	 */
	
	private static final LevelFetcher levelFetcher = new LevelFetcher() {
		@Override
		public Level makeLevel(int levelId, long seed, IslandType islandType) {
			return new ServerLevel(levelId, islandType.generateIsland(seed));
		}
		
		@Override
		public Level loadLevel(final Version version, int levelId, TileData[][] tileData, String[] entityData) {
			ServerLevel level = new ServerLevel(levelId, tileData);
			for(String e: entityData)
				level.addEntity(ServerEntity.deserialize(e, version));
			return level;
		}
	};
	
	private final SyncObj<Map<Integer, ServerLevel>> loadedLevels = new SyncObj<>(Collections.synchronizedMap(new HashMap<>()));
	
	private final EntityManager entityManager = new EntityManager();
	
	private Path worldPath;
	private RandomAccessFile lockRef;
	private LevelCache[] islandStores;
	private HashMap<String, PlayerInfo> knownPlayers;
	private long worldSeed;
	private GameServer server;
	
	private boolean worldLoaded = false;
	
	public ServerWorld(boolean standalone) throws IOException {
		server = new GameServer(standalone);
		server.startServer();
	}
	
	// Update method
	
	@Override
	public void update(float delta) {
		
		ServerLevel[] levels = loadedLevels.get(map -> map.values().toArray(new ServerLevel[0]));
		for(ServerLevel level: levels)
			level.update(delta);
		
		super.update(delta);
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	public WorldData getWorldUpdate() { return new WorldData(gameTime, this.daylightOffset, Config.DaylightCycle.get()); }
	public void broadcastWorldUpdate() { server.broadcast(getWorldUpdate()); }
	
	public void setTimeOfDay(float daylightOffset) {
		this.daylightOffset = daylightOffset % TimeOfDay.SECONDS_IN_DAY;
		broadcastWorldUpdate();
	}
	public float changeTimeOfDay(float deltaOffset) {
		float newOff = this.daylightOffset;
		if(deltaOffset < 0)
			newOff += TimeOfDay.SECONDS_IN_DAY;
		newOff = (newOff + deltaOffset) % TimeOfDay.SECONDS_IN_DAY;
		setTimeOfDay(newOff);
		return getDaylightOffset();
	}
	
	@Override protected boolean doDaylightCycle() { return Config.DaylightCycle.get(); }
	
	@Override
	public boolean worldLoaded() { return worldLoaded; }
	
	// this is called "load", but a world file does not necessarily mean it's data in a file; it's simply the intermediary that the world fetches non-live data from.
	public void loadWorld(WorldDataSet worldInfo) {
		worldLoaded = false;
		clearWorld();
		
		gameTime = worldInfo.gameTime;
		daylightOffset = worldInfo.timeOfDay;
		worldSeed = worldInfo.seed;
		knownPlayers = new HashMap<>(worldInfo.playerInfo.length*2);
		for(PlayerInfo info: worldInfo.playerInfo)
			knownPlayers.put(info.name, info);
		
		islandStores = worldInfo.levelCaches;
		worldPath = worldInfo.worldFile;
		lockRef = worldInfo.lockRef;
		
		worldLoaded = true;
	}
	
	/** Saves the world to file; specific to ServerWorld. */
	public void saveWorld() {
		
	}
	
	@Override
	protected void clearWorld() {
		super.clearWorld();
		Integer[] loadedIds = loadedLevels.get(map -> map.keySet().toArray(new Integer[0]));
		for(int id: loadedIds)
			unloadLevel(id);
		
		entityManager.clear();
	}
	
	// the ServerWorld cannot be reused after a call to exitWorld(); a new instance must be made.
	@Override
	public void exitWorld() {
		if(!worldLoaded) return;
		// dispose of level/world resources
		clearWorld();
		islandStores = null;
		server.stop();
		worldLoaded = false;
		if(lockRef != null) {
			try {
				lockRef.close();
			} catch(IOException e) {
				System.err.println("while closing world lock ref");
				e.printStackTrace();
			}
			lockRef = null;
			worldPath = null;
		}
		//spawnTile = null;
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	private boolean isLevelLoaded(int levelId) { return loadedLevels.get(map -> map.containsKey(levelId)); }
	
	@Override
	public ServerLevel getLevel(int levelId) { return loadedLevels.get(map -> map.get(levelId)); }
	
	@NotNull
	// player activator is required to ensure the level is not immediately pruned due to chance circumstances.
	// it is assumed that the player is not currently on a level.
	public synchronized ServerLevel loadLevel(int levelId, @NotNull ServerPlayer activator) {
		return loadLevel(levelId, activator, level -> {});
	}
	@NotNull
	// the ordering of "get/make level", "position player", "send level data", and finally "register world/add player" is important. Doing so is the most efficient, and prevents split-second frame changes like showing the player in the previous level position, as well as minimizing the time that the player may be in-game on the server, but still loading on the client.
	public synchronized ServerLevel loadLevel(int levelId, @NotNull ServerPlayer activator, ValueFunction<ServerLevel> playerPositioner) {
		
		ServerLevel level = getLevel(levelId);
		
		boolean put = level == null;
		if(put)
			level = (ServerLevel) islandStores[levelId].getLevel(levelFetcher);
		
		// position the activator.
		playerPositioner.act(level);
		
		/*
			there are a couple possible moves here.
			- the player is moving between islands in a boat
				- position set by server; level must exist first though
				- position would be on dock sprite.
			- the player is joining for the first time, or respawning (either way check player obj)
				- spawnmob placement, or using given vars
			- the player is rejoining (or console hax); position is already set
				- for console hax: would be set in Command class; player would be removed from current level, then position would be set, then this method gets called
				- for rejoin: position is from save file
			
		 */
		
		// send level data to client
		server.sendLevel(activator, level);
		
		// add player to level and register level if needed
		if(put) {
			final ServerLevel sl = level;
			loadedLevels.act(map -> {
				map.put(levelId, sl);
				setEntityLevel(activator, sl);
			});
		}
		else setEntityLevel(activator, level);
		
		return level;
	}
	
	protected void unloadLevel(int levelId) {
		// TODO save to file first
		
		ServerLevel level = getLevel(levelId);
		if(level == null) return; // already unloaded
		
		//System.out.println("unloading level "+levelId);
		
		for(Entity e: entityManager.removeLevel(level)) {
			super.deregisterEntity(e.getId());
		}
		
		loadedLevels.act(map -> map.remove(levelId));
	}
	
	protected void pruneLoadedLevels() {
		Integer[] levelids = loadedLevels.get(map -> map.keySet().toArray(new Integer[0]));
		HashSet<Integer> safeIds = new HashSet<>(levelids.length);
		
		// log which levels have a keep-alive
		for(ServerPlayer player: server.getPlayers()) {
			Level level = player.getLevel();
			if(level != null)
				safeIds.add(level.getLevelId());
		}
		
		// unload any loaded level that didn't have a keep-alive
		for(int id: levelids)
			if(!safeIds.contains(id))
				unloadLevel(id);
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	public int getEntityCount(ServerLevel level) {
		return entityManager.getEntityCount(level);
	}
	
	public ServerEntity[] getEntities(ServerLevel level) {
		return entityManager.getEntities(level);
	}
	
	@Override
	public void deregisterEntity(int eid) {
		ServerEntity e = getEntity(eid);
		if(e == null) {
			if(GameCore.debug)
				System.out.println("Server could not find entity "+eid+", ignoring deregister request.");
			return;
		}
		
		removeEntityLevel(e);
		super.deregisterEntity(eid);
	}
	
	private void removeEntityLevel(@NotNull ServerEntity e) {
		entityManager.removeEntity(e);
		
		server.broadcast(new EntityRemoval(e));
		
		if(e instanceof ServerPlayer)
			pruneLoadedLevels();
	}
	
	public void setEntityLevel(@NotNull ServerEntity e, @NotNull ServerLevel level) {
		boolean registered = isEntityRegistered(e);
		ServerLevel current = e.getLevel();
		boolean hasLevel = current != null;
		
		if(hasLevel && current.getLevelId() == level.getLevelId())
			return; // requests to add an entity to a level they are already on will be quietly ignored.
		
		boolean act = true;
		if(!registered) {
			act = false;
			if(GameCore.debug) {
				if(hasLevel)
					System.err.println("Unregistered server entity found on level "+current+" during request to set level to "+level+". Ignoring request and removing from current level.");
				else
					System.err.println("Server entity "+e+" is not registered (request to set level to "+level+"). Ignoring request.");
			}
			
			if(hasLevel) {
				entityManager.removeEntity(e);
				hasLevel = false;
			}
		}
		
		if(hasLevel) {
			act = false;
			if(GameCore.debug) System.out.println("Server entity "+e+" is already on level "+current+", will not set level to "+level);
		}
		
		if(!isLevelLoaded(level.getLevelId())) {
			act = false;
			if(GameCore.debug) System.err.println("Server level "+level+" should not be loaded; will not add entity "+e);
		}
		
		
		if(!act) return; // level set is not valid.
		
		
		entityManager.addEntity(e, level);
		
		server.broadcast(new EntityAddition(e), level, e);
	}
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	// called when switching levels and on player death (client won't show death screen if on map or loading screen
	public void despawnPlayer(@NotNull ServerPlayer player) {
		removeEntityLevel(player);
	}
	
	public void respawnPlayer(ServerPlayer player) {
		player.reset();
		
		// ServerLevel level = loadLevel(0);
		
		// if(level == null)
		// 	throw new NullPointerException("Surface level found to be null while attempting to spawn player.");
		
		// find a good spawn location near the middle of the map
		// Rectangle spawnBounds = levelGenerator.getSpawnArea(new Rectangle());
		
		loadLevel(player.getSpawnLevel(), player, player.respawnPositioning());
		// later, perhaps spawn player in specific location after first attempt, instead of randomly always.
	}
	
	@NotNull
	// registers a new player in the world with the given username. World data will be checked to see if this player has logged in before; if so, that player file is loaded, otherwise, a new player is created.
	public ServerPlayer addPlayer(String playerName) {
		// todo check files for player data
		
		ServerPlayer player = new ServerPlayer(playerName);
		
		// at this point, the player object has been initialized and registered, but it isn't on a particular level yet. This method doesn't attempt to position the player or set its level, beyond data stored from previous logins.
		
		// respawnPlayer(player);
		
		if(GameCore.debug) {
			player.getInventory().addItem(new ToolItem(ToolType.Shovel, Material.Ruby));
			player.getInventory().addItem(new ToolItem(ToolType.Shovel, Material.Ruby));
			player.getInventory().addItem(new ToolItem(ToolType.Shovel, Material.Ruby));
			player.getInventory().addItem(new ToolItem(ToolType.Pickaxe, Material.Iron));
			player.getInventory().addItem(new ToolItem(ToolType.Pickaxe, Material.Ruby));
			player.getInventory().addItem(ServerTileType.getItem(TileTypeEnum.CLOSED_DOOR));
			player.getInventory().addItem(ServerTileType.getItem(TileTypeEnum.TORCH));
			for(int i = 0; i < 7; i++)
				player.getInventory().addItem(ResourceType.Log.get());
			player.getInventory().addItem(ResourceType.Tungsten.get());
			player.getInventory().addItem(ResourceType.Flint.get());
			player.getInventory().addItem(ResourceType.Fabric.get());
			player.getInventory().addItem(ResourceType.Cotton.get());
			for(FoodType food: FoodType.values())
				player.getInventory().addItem(food.get());
		}
		return player;
	}
	
	
	/*  --- GET METHODS --- */
	
	
	@Override
	public ServerEntity getEntity(int eid) { return (ServerEntity) super.getEntity(eid); }
	
	@Override
	public ServerTileType getTileType(TileTypeEnum type) { return ServerTileType.get(type); }
	
	public GameServer getServer() { return server; }
	
	public MapRequest getMapData() {
		
		return new MapRequest();
	}
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return entityManager.getLevel((ServerEntity) e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
