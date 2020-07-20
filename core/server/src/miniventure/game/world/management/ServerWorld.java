package miniventure.game.world.management;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.*;

import miniventure.game.network.GameProtocol.DatalessRequest;
import miniventure.game.network.GameProtocol.EntityAddition;
import miniventure.game.network.GameProtocol.EntityRemoval;
import miniventure.game.network.GameProtocol.IslandReference;
import miniventure.game.network.GameProtocol.MapRequest;
import miniventure.game.network.GameProtocol.WorldData;
import miniventure.game.network.GameServer;
import miniventure.game.core.ServerCore;
import miniventure.game.network.ServerFetcher;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.file.*;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class ServerWorld extends WorldManager {
	
	/*
		The ServerWorld is the only WorldManager that can save a world to file; or load it technically,
		though ClientWorld has that method too... but it just sends a request to the ServerWorld. :P
		
		ServerWorld is also the only world that will ever have multiple islands loaded simultaneously.
	 */
	
	@NotNull
	private final ServerCore core;
	@NotNull
	private final GameServer server;
	
	private final ServerLevelManager levelManager;
	private final ServerEntityManager entityManager = new ServerEntityManager();
	
	private final Path worldPath;
	private final RandomAccessFile lockRef;
	private final long worldSeed;
	
	private boolean worldLoaded;
	
	private final List<Runnable> runnables = Collections.synchronizedList(new LinkedList<>());
	private final Object updateLock = new Object();
	
	// locks world update frames so world loading, saving, and exiting occurs before or after a full step.
	// private final Object updateLock = new Object();
	
	public ServerWorld(@NotNull ServerCore core, @NotNull ServerFetcher serverFetcher, @NotNull WorldDataSet worldInfo, ProgressLogger logger) throws IOException {
		this.core = core;
		logger.pushMessage("Initializing world", false);
		
		logger.pushMessage("Parsing world parameters");
		
		updateTime(worldInfo.gameTime, worldInfo.timeOfDay);
		worldSeed = worldInfo.seed;
		
		// islandManagers = worldInfo.islandManagers;
		
		worldPath = worldInfo.worldFile;
		lockRef = worldInfo.lockRef;
		
		PlayerData[] pinfo = worldInfo.playerInfo;
		
		this.levelManager = new ServerLevelManager(this, worldPath, worldInfo.islandManagers);
		
		final boolean old = !Version.matchesCurrentFormat(worldInfo.dataVersion);
		if(old) {
			logger.editMessage("Updating save format to current version");
			
			// "refresh" the data
			logger.pushMessage("updating player data");
			pinfo = ArrayUtils.mapArray(pinfo, PlayerData.class, info -> {
				String newData = ServerEntity.serialize(ServerEntity.deserialize(this, info.data, worldInfo.dataVersion));
				return new PlayerData(info, newData);
			});
			
			for(IslandDataManager island: worldInfo.islandManagers) {
				// refresh surface
				if(island.isGenerated(true)) {
					LevelDataSet data = levelManager.loadLevel(
							LevelId.getId(island.getIslandId(), true),
							worldInfo.dataVersion
					).save();
					WorldFileInterface.saveLevel(worldPath, data);
				}
				// refresh caverns
				if(island.isGenerated(false)) {
					LevelDataSet data = levelManager.loadLevel(
							LevelId.getId(island.getIslandId(), false),
							worldInfo.dataVersion
					).save();
					WorldFileInterface.saveLevel(worldPath, data);
				}
			}
		}
		
		this.server = serverFetcher.get(this, pinfo);
		
		if(worldInfo.create) {
			logger.editMessage("Generating starter island");
			LevelDataSet levelData = levelManager.generateLevel(
					worldInfo.islandManagers[0], true
			).save();
			logger.editMessage("Saving generated terrain to file");
			WorldFileInterface.saveLevel(worldPath, levelData);
		}
		
		logger.popMessage();
		logger.editMessage("World initialized.", true);
		
		worldLoaded = true;
	}
	
	
	/*  --- UPDATE MANAGEMENT --- */
	
	
	public void postRunnable(Runnable r) { postRunnable(true, r); }
	public void postRunnable(boolean allowAsync, Runnable r) {
		if(!core.isRunning() || core.isUpdateThread())
			r.run();
		else if(allowAsync)
			synchronized (runnables) {
				runnables.add(r);
			}
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
			for(ServerLevel level: levelManager.getLoadedLevels())
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
	
	
	public WorldData getWorldUpdate() {
		return new WorldData(getGameTime(), getDaylightOffset(), Config.DaylightCycle.get());
	}
	public void broadcastWorldUpdate() { server.broadcastGlobal(getWorldUpdate()); }
	
	public void setTimeOfDay(float daylightOffset) {
		updateTime(getGameTime(), daylightOffset % TimeOfDay.SECONDS_IN_DAY);
		broadcastWorldUpdate();
	}
	public float changeTimeOfDay(float deltaOffset) {
		float newOff = getDaylightOffset();
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
		synchronized (updateLock) {
			// save loaded levels
			ServerLevel[] levels = levelManager.getLoadedLevels();
			LevelDataSet[] levelData = ArrayUtils.mapArray(levels, LevelDataSet.class, ServerLevel::save);
			
			// save player data
			PlayerData[] pdata = server.updatePlayerData();
			
			WorldFileInterface.saveWorld(
					WorldDataSet.fromLoaded(worldPath, lockRef, worldSeed, getGameTime(), getDaylightOffset(), pdata, levelManager.getIslandManagers(), levelData)
			);
		}
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
		// postRunnable(() -> {
			if(!worldLoaded) return;
			server.stop(true);
			worldLoaded = false;
			// WorldFileInterface.saveWorld(WorldDataSet.fromLoaded(worldPath, lockRef, worldSeed, gameTime, daylightOffset, GameCore.VERSION, knownPlayers.values().toArray(new PlayerInfo[0]), islandStores));
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
		// });
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	@Override
	public ServerLevel getLevel(LevelId levelId) { return levelManager.getLoadedLevel(levelId); }
	
	@NotNull
	// player activator is required to ensure the level is not immediately pruned due to chance circumstances.
	// it is assumed that the player is not currently on a level.
	public ServerLevel loadLevel(LevelId levelId, @NotNull ServerPlayer activator) {
		return loadLevel(levelId, activator, level -> {});
	}
	@NotNull
	// the ordering of "get/make level", "position player", "send level data", and finally "register world/add player" is important. Doing so is the most efficient, and prevents split-second frame changes like showing the player in the previous level position, as well as minimizing the time that the player may be in-game on the server, but still loading on the client.
	public ServerLevel loadLevel(LevelId levelId, @NotNull ServerPlayer activator, ValueAction<ServerLevel> playerPositioner) {
		
		ServerLevel level = getLevel(levelId);
		
		boolean put = level == null;
		if(put) {
			MyUtils.debug("Fetching level "+levelId);
			server.sendToPlayer(activator, DatalessRequest.Level_Loading);
			level = levelManager.fetchLevel(levelId);
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
			levelManager.addLevel(level, activator);
		}
		else setEntityLevel(activator, level);
		
		MyUtils.debug("Player "+activator+" has been loaded to level "+level.getLevelId());
		
		return level;
	}
	
	protected void unloadLevel(LevelId levelId) {
		ServerLevel level = getLevel(levelId);
		if(level == null) return; // already unloaded
		
		//System.out.println("unloading level "+levelId);
		
		level.save();
		for(ServerEntity e: entityManager.removeLevel(level))
			super.deregisterEntity(e.getId());
		
		levelManager.removeLevel(levelId);
		MyUtils.debug("unloaded level "+levelId);
	}
	
	protected void pruneLoadedLevels() {
		ServerLevel[] levels = levelManager.getLoadedLevels();
		HashSet<LevelId> keepAliveIds = new HashSet<>(levels.length);
		
		// log which levels have a keep-alive
		for(ServerPlayer player: server.getPlayers()) {
			ServerLevel level = player.getLevel();
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
			MyUtils.debug("Server could not find entity "+eid+", ignoring deregister request.");
			return;
		}
		
		removeEntityLevel(e);
		super.deregisterEntity(eid);
	}
	
	private void removeEntityLevel(@NotNull ServerEntity e) {
		entityManager.removeEntity(e);
		
		server.broadcastGlobal(new EntityRemoval(e));
		
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
				MyUtils.error("Unregistered server entity found on level " + current + " during request to set level to " + level + ". Removing from current level.");
				entityManager.removeEntity(e);
				hasLevel = false;
				act = false;
			}
			else if(current.getLevelId() != level.getLevelId())
				// levels are different
				MyUtils.error("Server entity "+e+" is already on level "+current+", will not set level to "+level);
			else
				return; // requests to add an entity to a level they are already on will be quietly ignored.
		}
		
		if(!level.isPreload() && !levelManager.isLevelLoaded(level.getLevelId())) {
			act = false;
			MyUtils.error("Server level "+level+" exists but is not loaded; will not add entity "+e);
		}
		
		if(!act) return; // level set is not valid.
		
		// this should be true the first time an entity is added to a level
		if(!registered)
			registerEntity(e);
		
		entityManager.addEntity(e, level);
		level.entityAdded(e);
		
		server.broadcastLocal(level, e, new EntityAddition(e));
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
	
	
	public long getWorldSeed() { return worldSeed; }
	
	public LevelId getDefaultLevel() { return LevelId.getId(0); }
	
	@Override
	public ServerEntity getEntity(int eid) { return (ServerEntity) super.getEntity(eid); }
	
	@Override
	public ServerTileType getTileType(TileTypeEnum type) { return ServerTileType.get(type); }
	
	@NotNull
	public GameServer getServer() { return server; }
	
	public MapRequest getMapData() {
		LinkedList<IslandReference> refs = new LinkedList<>();
		for(IslandDataManager island: levelManager.getIslandManagers()) {
			if(island.isUnlocked())
				refs.add(island.getRef());
		}
		return new MapRequest(refs.toArray(new IslandReference[0]));
	}
	
	public float getFPS() { return core.getFPS(); }
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return entityManager.getLevel((ServerEntity) e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
