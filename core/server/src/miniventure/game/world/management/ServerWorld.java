package miniventure.game.world.management;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.*;

import miniventure.game.GameCore;
import miniventure.game.network.GameProtocol.EntityAddition;
import miniventure.game.network.GameProtocol.EntityRemoval;
import miniventure.game.network.GameProtocol.IslandReference;
import miniventure.game.network.GameProtocol.MapRequest;
import miniventure.game.network.GameProtocol.WorldData;
import miniventure.game.server.GameServer;
import miniventure.game.server.ServerCore;
import miniventure.game.server.ServerFetcher;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.SyncObj;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.file.IslandCache;
import miniventure.game.world.file.LevelCache;
import miniventure.game.world.file.PlayerData;
import miniventure.game.world.file.WorldDataSet;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelFetcher;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.NotNull;

public class ServerWorld extends WorldManager {
	
	/*
		The ServerWorld is the only WorldManager that can save a world to file; or load it technically,
		though ClientWorld has that method too... but it just sends a request to the ServerWorld. :P
		
		ServerWorld is also the only world that will ever have multiple islands loaded simultaneously.
	 */
	
	private final LevelFetcher<ServerLevel> levelFetcher = new LevelFetcher<ServerLevel>() {
		@Override
		public ServerLevel makeLevel(LevelCache cache) {
			int levelId = cache.getId();
			IslandType islandType = cache.islandType;
			String mapType = levelId < 0 ? "underground" : "surface";
			GameCore.debug("Server generating "+islandType+' '+mapType+" map for level "+levelId);
			long seed = ServerWorld.this.worldSeed * (2 + levelId);
			return new ServerLevel(ServerWorld.this, cache,
				islandType.generateIsland(seed, levelId > 0)
			);
		}
		
		@Override
		public ServerLevel loadLevel(LevelCache cache, final Version version, TileData[][] tileData, String[] entityData) {
			GameCore.debug("Server loading level "+cache.getId()+" from data");
			ServerLevel level = new ServerLevel(ServerWorld.this, cache, tileData);
			
			for(String e: entityData)
				level.addEntity(ServerEntity.deserialize(ServerWorld.this, e, version));
			return level;
		}
	};
	
	@NotNull
	private final ServerCore core;
	@NotNull
	private final GameServer server;
	
	private final SyncObj<Map<Integer, ServerLevel>> loadedLevels = new SyncObj<>(Collections.synchronizedMap(new HashMap<>()));
	
	private final EntityManager entityManager = new EntityManager();
	
	private final Path worldPath;
	private final RandomAccessFile lockRef;
	private final IslandCache[] islandStores;
	private final long worldSeed;
	
	private boolean worldLoaded;
	
	private final List<Runnable> runnables = Collections.synchronizedList(new LinkedList<>());
	private final Object updateLock = new Object();
	
	// locks world update frames so world loading, saving, and exiting occurs before or after a full step.
	// private final Object updateLock = new Object();
	
	public ServerWorld(@NotNull ServerCore core, @NotNull ServerFetcher serverFetcher, @NotNull WorldDataSet worldInfo, ProgressLogger logger) throws IOException {
		this.core = core;
		logger.pushMessage("Parsing world parameters", true);
		final boolean old = worldInfo.dataVersion.compareTo(GameCore.VERSION) < 0;
		
		PlayerData[] pinfo = worldInfo.playerInfo;
		if(old) {
			// "refresh" the data
			pinfo = ArrayUtils.mapArray(pinfo, PlayerData.class, info -> {
				String newData = ServerEntity.serialize(ServerEntity.deserialize(this, info.data, worldInfo.dataVersion));
				return new PlayerData(info, newData);
			});
		}
		
		this.server = serverFetcher.get(this, pinfo);
		
		gameTime = worldInfo.gameTime;
		daylightOffset = worldInfo.timeOfDay;
		worldSeed = worldInfo.seed;
		
		islandStores = worldInfo.islandCaches;
		// generate levels with a new world, refresh loaded levels for an old one
		
		// boolean genIsland = true;
		if(worldInfo.create) {
			logger.pushMessage("Generating world");
			logger.pushMessage("");
		}
		// else if(old)
		// 	logger.pushMessage("Refreshing terrain data");
		else {
			// genIsland = false;
			logger.pushMessage("Checking for ungenerated terrain");
		}
		
		int loadIdx = 1;
		final int totalLevels = islandStores.length * 2;
		for(IslandCache island: islandStores) {
			for(LevelCache cache: Arrays.asList(island.surface, island.caverns)) {
				if(!cache.generated() || worldInfo.create/* || old*/) {
					if(!worldInfo.create)
						logger.editMessage("Generating missing terrain");
					else
						logger.editMessage("Generating level "+loadIdx+'/'+totalLevels);
					levelFetcher.makeLevel(cache).save();
					// genIsland = true;
				}
				loadIdx++;
			}
		}
		// logger.popMessage();
		
		worldPath = worldInfo.worldFile;
		lockRef = worldInfo.lockRef;
		
		if(worldInfo.create/*genIsland*/) {
			logger.editMessage("Saving initial generated world to file"/*"Saving refreshed world data"*/);
			saveWorld();
			logger.popMessage();
		}
		
		logger.editMessage("World Loaded.", true);
		worldLoaded = true;
	}
	
	
	/*  --- UPDATE MANAGEMENT --- */
	
	
	public void postRunnable(Runnable r) { postRunnable(true, r); }
	public void postRunnable(boolean allowAsync, Runnable r) {
		if(!core.isRunning() || core.isUpdateThread())
			r.run();
		else if(allowAsync)
			runnables.add(r);
		else {
			synchronized (updateLock) {
				r.run();
			}
		}
			
	}
	
	@Override
	public void update(float delta) {
		if(!worldLoaded) return;
		
		synchronized (updateLock) {
			ServerLevel[] levels = loadedLevels.get(map -> map.values().toArray(new ServerLevel[0]));
			for(ServerLevel level : levels)
				level.update(delta);
		}
		
		// run any runnables that were posted during the above update
		Runnable[] lastRunnables;
		// synchronized extenerally to link the toArray and clear so they are not interrupted.
		synchronized (runnables) {
			lastRunnables = runnables.toArray(new Runnable[0]);
			runnables.clear();
		}
		
		synchronized (updateLock) {
			for(Runnable r : lastRunnables)
				r.run(); // any runnables added here will be run next update
			
			super.update(delta);
		}
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
	
	/** Saves the world to file; specific to ServerWorld. */
	public void saveWorld() {
		// update island store caches
		postRunnable(() -> {
			loadedLevels.act(map -> {
				for(ServerLevel level: map.values())
					if(level != null) // I don't know if I should catch this, it shouldn't happen to begin with...
						level.save();
			});
			
			PlayerData[] pdata = server.updatePlayerData();
			// update player data
			// for(ServerPlayer player: getServer().getPlayers()) {
			// 	savePlayer(player);
			// }
			
			WorldFileInterface.saveWorld(new WorldDataSet(worldPath, lockRef, worldSeed, gameTime, daylightOffset, GameCore.VERSION, pdata, islandStores));
		});
	}
	
	/*public void savePlayer(@NotNull ServerPlayer player) {
		postRunnable(() -> {
			
			
			server.updatePlayerData(player);
		});
	}*/
	
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
			// WorldFileInterface.saveWorld(new WorldDataSet(worldPath, lockRef, worldSeed, gameTime, daylightOffset, GameCore.VERSION, knownPlayers.values().toArray(new PlayerInfo[0]), islandStores));
			// dispose of level/world resources
			saveWorld();
			clearEntityIdMap();
			entityManager.clear();
			// islandStores = null;
			if(lockRef != null) {
				try {
					lockRef.close();
				} catch(IOException e) {
					System.err.println("exception while closing world lock ref");
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
	public ServerLevel loadLevel(int levelId, @NotNull ServerPlayer activator, ValueAction<ServerLevel> playerPositioner) {
		
		ServerLevel level = getLevel(levelId);
		
		boolean put = level == null;
		if(put) {
			GameCore.debug("Fetching level "+levelId);
			IslandCache island = islandStores[Math.abs(levelId)-1];
			level = (ServerLevel) (levelId > 0 ? island.surface : island.caverns).getLevel(levelFetcher);
		}
		
		// synchronize on the level
		
		// position the activator.
		playerPositioner.act(level);
		
		// send level data to client
		server.sendLevel(activator, level);
		
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
		
		// add player to level and register level if needed
		if(put) {
			final ServerLevel sl = level;
			loadedLevels.act(map -> {
				map.put(levelId, sl);
				setEntityLevel(activator, sl);
			});
			GameCore.debug("level "+level.getLevelId()+" is now loaded.");
		}
		else setEntityLevel(activator, level);
		
		GameCore.debug("Player "+activator+" has been loaded to level "+level.getLevelId());
		
		return level;
	}
	
	protected void unloadLevel(int levelId) {
		ServerLevel level = getLevel(levelId);
		if(level == null) return; // already unloaded
		
		//System.out.println("unloading level "+levelId);
		
		level.save();
		for(ServerEntity e: entityManager.removeLevel(level))
			super.deregisterEntity(e.getId());
		
		loadedLevels.act(map -> map.remove(levelId));
		GameCore.debug("unloaded level "+levelId);
	}
	
	protected void pruneLoadedLevels() {
		ServerLevel[] levels = loadedLevels.get(map -> map.values().toArray(new ServerLevel[0]));
		HashSet<Integer> keepAliveIds = new HashSet<>(levels.length);
		
		// log which levels have a keep-alive
		for(ServerPlayer player: server.getPlayers()) {
			Level level = player.getLevel();
			if(level != null)
				keepAliveIds.add(level.getLevelId());
		}
		
		// unload any loaded level that didn't have a keep-alive
		for(ServerLevel level: levels)
			if(!keepAliveIds.contains(level.getLevelId()) && !level.isPreload())
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
			GameCore.debug("Server could not find entity "+eid+", ignoring deregister request.");
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
				GameCore.error("Unregistered server entity found on level " + current + " during request to set level to " + level + ". Removing from current level.");
				entityManager.removeEntity(e);
				hasLevel = false;
				act = false;
			}
			else if(current.getLevelId() != level.getLevelId())
				// levels are different
				GameCore.error("Server entity "+e+" is already on level "+current+", will not set level to "+level);
			else
				return; // requests to add an entity to a level they are already on will be quietly ignored.
		}
		
		if(!level.isPreload() && !isLevelLoaded(level.getLevelId())) {
			act = false;
			GameCore.error("Server level "+level+" exists but is not loaded; will not add entity "+e);
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
		server.updatePlayerData(player);
		removeEntityLevel(player);
	}
	
	public void respawnPlayer(ServerPlayer player) {
		player.reset();
		
		loadLevel(player.getSpawnLevel(), player, player.respawnPositioning());
	}
	
	// called after the player has been registered to the GameServer.
	/*public void addPlayer(ServerPlayer player*//*, String passhash*//*) {
		if(info != null) {
			GameCore.debug("existing player "+player+" loading into level "+info.levelId);
			loadLevel(info.levelId, player);
		}
		else {
			respawnPlayer(player);
			knownPlayers.put(player.getName(), new PlayerData(player.getName(), "", player.serialize(), player.getSpawnLevel()));
			// saveWorld();
		}
	}*/
	
	/*public boolean checkPassword(String playerName, String passhash) {
		
	}*/
	
	
	
	// registers a new player in the world with the given username. World data will be checked to see if this player has logged in before; if so, that player file is loaded, otherwise, a new player is created.
	/*public ServerPlayer addPlayer(String playerName*//*, String passhash*//*) {
		ServerPlayer player;
		// check for player data; I don't expect players to get removed from this so synchronization shouldn't be necessary.
		
		
		// at this point, the player object has been initialized, but not necessarily registered or on a particular level yet. This method doesn't attempt to position the player or set its level, beyond data stored from previous logins.
		
		return player;
	}*/
	
	
	/*  --- GET METHODS --- */
	
	public int getDefaultLevel() { return islandStores[0].surface.getId(); }
	
	@Override
	public ServerEntity getEntity(int eid) { return (ServerEntity) super.getEntity(eid); }
	
	@Override
	public ServerTileType getTileType(TileTypeEnum type) { return ServerTileType.get(type); }
	
	@NotNull
	public GameServer getServer() { return server; }
	
	public MapRequest getMapData() {
		LinkedList<IslandReference> refs = new LinkedList<>();
		for(IslandCache cache: islandStores) { // todo only add islands that have been unlocked
			refs.add(cache.ref);
		}
		return new MapRequest(refs.toArray(new IslandReference[0]));
	}
	
	public float getFPS() { return core.getFPS(); }
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return entityManager.getLevel((ServerEntity) e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
