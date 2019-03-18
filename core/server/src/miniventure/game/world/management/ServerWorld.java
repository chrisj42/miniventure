package miniventure.game.world.management;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.server.GameServer;
import miniventure.game.server.ServerCore;
import miniventure.game.util.SyncObj;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelFetcher;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.SaveLoadInterface.LevelCache;
import miniventure.game.world.management.SaveLoadInterface.PlayerInfo;
import miniventure.game.world.management.SaveLoadInterface.WorldDataSet;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerWorld extends WorldManager {
	
	/*
		The ServerWorld is the only WorldManager that can save a world to file; or load it technically,
		though ClientWorld has that method too... but it just sends a request to the ServerWorld. :P
		
		ServerWorld is also the only world that will ever have multiple islands loaded simultaneously.
	 */
	
	private final LevelFetcher levelFetcher = new LevelFetcher() {
		@Override
		public ServerLevel makeLevel(int levelId, long seed, IslandType islandType) {
			return new ServerLevel(ServerWorld.this, levelId, islandType.generateIsland(seed));
		}
		
		@Override
		public ServerLevel loadLevel(final Version version, int levelId, TileData[][] tileData, String[] entityData) {
			ServerLevel level = new ServerLevel(ServerWorld.this, levelId, tileData);
			
			for(String e: entityData)
				level.addEntity(ServerEntity.deserialize(ServerWorld.this, e, version));
			return level;
		}
	};
	
	private final ServerCore core;
	private final GameServer server;
	
	private final SyncObj<Map<Integer, ServerLevel>> loadedLevels = new SyncObj<>(Collections.synchronizedMap(new HashMap<>()));
	
	private final EntityManager entityManager = new EntityManager();
	
	private final Path worldPath;
	private final RandomAccessFile lockRef;
	private final LevelCache[] islandStores;
	private final Map<String, PlayerInfo> knownPlayers;
	private final long worldSeed;
	
	private boolean worldLoaded;
	
	private final List<Runnable> runnables = Collections.synchronizedList(new LinkedList<>());
	
	// locks world update frames so world loading, saving, and exiting occurs before or after a full step.
	private final Object updateLock = new Object();
	
	public ServerWorld(@NotNull ServerCore core, int port, boolean multiplayer, @NotNull WorldDataSet worldInfo) throws IOException {
		this.core = core;
		this.server = new GameServer(this, port, multiplayer); // start new server on given port
		
		final boolean old = worldInfo.dataVersion.compareTo(GameCore.VERSION) < 0;
		gameTime = worldInfo.gameTime;
		daylightOffset = worldInfo.timeOfDay;
		worldSeed = worldInfo.seed;
		knownPlayers = Collections.synchronizedMap(new HashMap<>(Math.max(4, worldInfo.playerInfo.length * 2)));
		for(int i = 0; i < worldInfo.playerInfo.length; i++) {
			PlayerInfo info = worldInfo.playerInfo[i];
			if(old) {
				// "refresh" the data
				String newData = ServerEntity.serialize(ServerEntity.deserialize(this, info.data, worldInfo.dataVersion));
				worldInfo.playerInfo[i] = info = new PlayerInfo(info.name, info.passhash, newData, info.levelId);
			}
			knownPlayers.put(info.name, info);
		}
		
		islandStores = worldInfo.levelCaches;
		if(old) {
			// refresh loaded levels
			for(int i = 0; i < islandStores.length; i++) {
				if(islandStores[i].generated()) {
					ServerLevel level = (ServerLevel) islandStores[i].getLevel(levelFetcher);
					level.save(islandStores[i]);
				}
			}
		}
		
		worldPath = worldInfo.worldFile;
		lockRef = worldInfo.lockRef;
		
		worldLoaded = true;
	}
	
	
	/*  --- UPDATE MANAGEMENT --- */
	
	
	public void postRunnable(Runnable r) { runnables.add(r); }
	
	@Override
	public void update(float delta) {
		if(!worldLoaded) return;
		
		ServerLevel[] levels = loadedLevels.get(map -> map.values().toArray(new ServerLevel[0]));
		for(ServerLevel level : levels)
			level.update(delta);
		
		// run any runnables that were posted during the above update
		Runnable[] lastRunnables;
		// synchronized extenerally to link the toArray and clear so they are not interrupted.
		synchronized (runnables) {
			lastRunnables = runnables.toArray(new Runnable[0]);
			runnables.clear();
		}
		
		for(Runnable r: lastRunnables)
			r.run(); // any runnables added here will be run next update
		
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
	public boolean worldLoaded() { synchronized (updateLock) { return worldLoaded; } }
	
	/** Saves the world to file; specific to ServerWorld. */
	public void saveWorld() {
		// update island store caches
		
		
		loadedLevels.act(map -> {
			for(ServerLevel level: map.values())
				level.save(islandStores[level.getLevelId()]);
		});
		
		// update player data
		for(ServerPlayer player: getServer().getPlayers()) {
			savePlayer(player);
		}
		
		synchronized (knownPlayers) {
			SaveLoadInterface.saveWorld(new WorldDataSet(worldPath, lockRef, worldSeed, gameTime, daylightOffset, GameCore.VERSION, knownPlayers.values().toArray(new PlayerInfo[0]), islandStores));
		}
	}
	
	public void savePlayer(@NotNull ServerPlayer player) {
		final String name = player.getName();
		final String data = player.serialize();
		Level level = player.getLevel();
		final int levelid = level == null ? player.getSpawnLevel() : level.getLevelId();
		
		PlayerInfo old = knownPlayers.get(name);
		PlayerInfo info = new PlayerInfo(name, old.passhash, data, levelid);
		knownPlayers.put(name, info);
	}
	
	/*@Override
	protected void clearWorld() {
		super.clearWorld();
		Integer[] loadedIds = loadedLevels.get(map -> map.keySet().toArray(new Integer[0]));
		for(int id: loadedIds)
			unloadLevel(id);
		
		entityManager.clear();
	}*/
	
	@Override
	public void exitWorld() {
		postRunnable(() -> {
			if(!worldLoaded) return;
			server.stop(true);
			worldLoaded = false;
			SaveLoadInterface.saveWorld(new WorldDataSet(worldPath, lockRef, worldSeed, gameTime, daylightOffset, GameCore.VERSION, knownPlayers.values().toArray(new PlayerInfo[0]), islandStores));
			// dispose of level/world resources
			saveWorld();
			clearEntityIdMap();
			entityManager.clear();
			// islandStores = null;
			if(lockRef != null) {
				try {
					lockRef.close();
				} catch(IOException e) {
					System.err.println("while closing world lock ref");
					e.printStackTrace();
				}
				// lockRef = null;
				// worldPath = null;
			}
		});
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	private boolean isLevelLoaded(int levelId) { return loadedLevels.get(map -> map.containsKey(levelId)); }
	
	@Override
	public ServerLevel getLevel(int levelId) { return loadedLevels.get(map -> map.get(levelId)); }
	
	@NotNull
	// player activator is required to ensure the level is not immediately pruned due to chance circumstances.
	// it is assumed that the player is not currently on a level.
	public ServerLevel loadLevel(int levelId, @NotNull ServerPlayer activator) {
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
	
	protected synchronized void unloadLevel(int levelId) {
		ServerLevel level = getLevel(levelId);
		if(level == null) return; // already unloaded
		
		//System.out.println("unloading level "+levelId);
		
		level.save(islandStores[levelId]);
		for(ServerEntity e: entityManager.removeLevel(level))
			super.deregisterEntity(e.getId());
		
		loadedLevels.act(map -> map.remove(levelId));
	}
	
	protected synchronized void pruneLoadedLevels() {
		ServerLevel[] levels = loadedLevels.get(map -> map.values().toArray(new ServerLevel[0]));
		HashSet<Integer> safeIds = new HashSet<>(levels.length);
		
		// log which levels have a keep-alive
		for(ServerPlayer player: server.getPlayers()) {
			Level level = player.getLevel();
			if(level != null)
				safeIds.add(level.getLevelId());
		}
		
		// unload any loaded level that didn't have a keep-alive
		for(ServerLevel level: levels)
			if(!safeIds.contains(level.getLevelId()) && !level.isPreload())
				unloadLevel(level.getLevelId());
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	public int getEntityCount(ServerLevel level) {
		return entityManager.getEntityCount(level);
	}
	
	public HashSet<ServerEntity> getEntities(ServerLevel level) {
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
		final boolean registered = isEntityRegistered(e);
		final ServerLevel current = e.getLevel();
		
		boolean hasLevel = current != null;
		boolean act = true;
		
		if(hasLevel) {
			if(!registered) {
				GameCore.debug("Unregistered server entity found on level " + current + " during request to set level to " + level + ". Removing from current level.");
				entityManager.removeEntity(e);
				hasLevel = false;
				act = false;
			}
			else if(current.getLevelId() != level.getLevelId())
				// levels are different
				GameCore.debug("Server entity "+e+" is already on level "+current+", will not set level to "+level);
			else
				return; // requests to add an entity to a level they are already on will be quietly ignored.
		}
		
		if(!isLevelLoaded(level.getLevelId())) {
			act = false;
			GameCore.debug("Server level "+level+" exists but is not loaded; will not add entity "+e);
		}
		
		if(!act) return; // level set is not valid.
		
		// this should be true the first time an entity is added to a level
		if(!registered)
			registerEntity(e);
		
		entityManager.addEntity(e, level);
		level.entityAdded(e);
		
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
	
	// called after the player has been registered to the GameServer.
	public void loadPlayer(ServerPlayer player, String passhash) {
		PlayerInfo info = knownPlayers.get(player.getName());
		if(info != null) {
			// if(GameCore.debug)
			// 	System.out.println("Player '"+player.getName()+"' is known.");
			loadLevel(info.levelId, player);
		}
		else {
			respawnPlayer(player);
			knownPlayers.put(player.getName(), new PlayerInfo(player.getName(), passhash, player.serialize(), player.getSpawnLevel()));
		}
	}
	
	// registers a new player in the world with the given username. World data will be checked to see if this player has logged in before; if so, that player file is loaded, otherwise, a new player is created.
	@Nullable
	public ServerPlayer addPlayer(String playerName, String passhash) {
		ServerPlayer player;
		// check for player data
		if(knownPlayers.containsKey(playerName)) {
			if(GameCore.debug)
				System.out.println("Player '"+playerName+"' is known.");
			PlayerInfo info = knownPlayers.get(playerName);
			if(!info.passhash.equals(passhash))
				return null; // incorrect password
			player = (ServerPlayer) ServerEntity.deserialize(this, knownPlayers.get(playerName).data, GameCore.VERSION);
		} else {
			player = new ServerPlayer(this, playerName);
			// knownPlayers.put(playerName, new PlayerInfo(playerName, passhash, player.serialize(), 0));
			
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
				for(FoodType food : FoodType.values())
					player.getInventory().addItem(food.get());
			}
		}
		
		// at this point, the player object has been initialized and registered, but it isn't on a particular level yet. This method doesn't attempt to position the player or set its level, beyond data stored from previous logins.
		
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
	
	public float getFPS() { return core.getFPS(); }
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return entityManager.getLevel((ServerEntity) e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
