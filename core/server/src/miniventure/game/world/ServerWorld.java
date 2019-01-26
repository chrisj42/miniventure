package miniventure.game.world;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.GameProtocol.EntityRemoval;
import miniventure.game.GameProtocol.WorldData;
import miniventure.game.ProgressPrinter;
import miniventure.game.file.WorldFile;
import miniventure.game.item.FoodType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolType;
import miniventure.game.server.GameServer;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.LevelGenerator;
import miniventure.game.world.worldgen.ProtoIsland;
import miniventure.game.world.worldgen.WorldConfig;
import miniventure.game.world.worldgen.WorldConfig.CreationConfig;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ServerWorld extends WorldManager {
	
	/*
		The ServerWorld is the only WorldManager that can save a world to file; or load it technically,
		though ClientWorld has that method too... but it just sends a request to the ServerWorld. :P
		
		ServerWorld is also the only world that will ever have multiple islands loaded simultaneously.
	 */
	
	private final HashMap<Integer, ServerLevel> loadedLevels = new HashMap<>();
	private final HashMap<ServerLevel, Set<ServerEntity>> levelEntities = new HashMap<>();
	private final HashMap<ServerEntity, ServerLevel> entityLevels = new HashMap<>(INITIAL_ENTITY_BUFFER);
	
	private final Set<WorldObject> keepAlives = Collections.synchronizedSet(new HashSet<>()); // always keep islands with these objects loaded.
	
	private ProtoIsland[] islandGenerators;
	private WorldFile worldFile;
	private GameServer server;
	
	private boolean worldLoaded = false;
	
	public ServerWorld(boolean standalone) throws IOException {
		server = new GameServer(standalone);
		server.startServer();
	}
	
	// Update method
	
	@Override
	public void update(float delta) {
		
		HashSet<ServerLevel> loadedLevels = new HashSet<>();
		synchronized (keepAlives) {
			for(WorldObject obj : keepAlives) {
				ServerLevel level = (ServerLevel) obj.getLevel();
				if(level != null)
					loadedLevels.add(level);
			}
		}
		
		for(ServerLevel level: loadedLevels)
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
	
	@Override
	public boolean createWorld(WorldConfig config) {
		worldLoaded = false;
		
		ProgressPrinter logger = new ProgressPrinter();
		logger.pushMessage("");
		
		clearWorld();
		
		if(config instanceof CreationConfig) {
			CreationConfig genconfig = (CreationConfig) config; 
			islandGenerators = new ProtoIsland(genconfig.seed, genconfig.width, genconfig.height);
			// todo instantiate a new world file
		}
		else {
			// todo read world file to get data to make world generator
		}
		
		// fixme honestly I shouldn't have a method that both loads and creates. This was a bad idea. The whole "WorldConfig" thing was a bad idea, in terms of having Creation and Load there; Load should be a different setup entirely.
		
		// the below code is going to go
		
		int numLevels = 3;
		// int minId = 1;
		for(int i = 0; i < numLevels; i++) {
			logger.editMessage("Loading level "+(i+1)+'/'+numLevels+"...");
			loadLevel(i);
		}
		logger.popMessage();
		
		worldLoaded = true;
		return true;
	}
	
	/** Saves the world to file; specific to ServerWorld. */
	public void saveWorld() {
		
	}
	
	@Override
	public void exitWorld() {
		if(!worldLoaded) return;
		// dispose of level/world resources
		synchronized (keepAlives) { keepAlives.clear(); }
		clearWorld();
		islandGenerators = null;
		worldFile = null;
		server.stop();
		worldLoaded = false;
		//spawnTile = null;
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	private boolean isLevelLoaded(int levelId) { return loadedLevels.containsKey(levelId); }
	
	private void loadLevel(int levelId) {
		// TODO this will need to get redone when loading from file
		
		if(loadedLevels.containsKey(levelId)) return;
		if(levelId < 0 || levelId >= islandGenerators.length) {
			System.err.println("Server: ERROR: no level with id " + levelId + " exists; cannot load. Ignoring call.");
			return;
		}
		LevelGenerator levelGenerator = islandGenerators[levelId].getLevelGenerator(levelId);
		
		addLevel(new ServerLevel(this, levelId, levelGenerator));
	}
	
	protected void unloadLevel(int levelId) {
		// TODO save to file first
		
		ServerLevel level = loadedLevels.get(levelId);
		if(level == null) return; // already unloaded
		
		//System.out.println("unloading level "+levelId);
		
		for(Entity e: levelEntities.remove(level)) {
			entityLevels.remove(e);
			e.remove();
		}
		
		loadedLevels.remove(levelId);
	}
	
	@Override
	protected void addLevel(@NotNull Level level) {
		levelEntities.put((ServerLevel)level, Collections.synchronizedSet(new HashSet<>()));
		loadedLevels.put(level.getLevelId(), (ServerLevel)level);
	}
	
	protected void pruneLoadedLevels() {
		Integer[] levelids = loadedLevels.keySet().toArray(new Integer[0]);
		HashSet<Integer> safeIds = new HashSet<>(levelids.length);
		
		// log which levels have a keep-alive
		synchronized (keepAlives) {
			for(WorldObject obj : keepAlives) {
				Level level = obj.getLevel();
				if(level != null)
					safeIds.add(level.getLevelId());
			}
		}
		
		// unload any loaded level that didn't have a keep-alive
		for(int id: levelids)
			if(!safeIds.contains(id))
				unloadLevel(id);
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	/** @noinspection MismatchedQueryAndUpdateOfCollection*/
	private static final HashSet<ServerEntity> emptySet = new HashSet<>();
	
	private void actOnEntitySet(ServerLevel level, ValueFunction<Set<ServerEntity>> action) {
		Set<ServerEntity> entitySet = levelEntities.getOrDefault(level, emptySet);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (entitySet) {
			action.act(entitySet);
		}
	}
	
	private <T> T getFromEntitySet(ServerLevel level, MapFunction<Set<ServerEntity>, T> getter) {
		Set<ServerEntity> entitySet = levelEntities.getOrDefault(level, emptySet);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (entitySet) {
			return getter.get(entitySet);
		}
	}
	
	public int getEntityCount(ServerLevel level) {
		return getFromEntitySet(level, Set::size);
	}
	
	public ServerEntity[] getEntities(ServerLevel level) {
		return getFromEntitySet(level, set -> set.toArray(new ServerEntity[0]));
	}
	
	@Override
	public void deregisterEntity(int eid) {
		ServerEntity e = getEntity(eid);
		ServerLevel prev = getEntityLevel(e);
		
		actOnEntitySet(prev, set -> {
			entityLevels.remove(e);
			set.remove(e);
			super.deregisterEntity(eid);
		});
		
		if(e != null && prev != null)
			server.broadcast(new EntityRemoval(e), prev, (ServerEntity) e);
	}
	
	public void setEntityLevel(@NotNull ServerEntity e, @NotNull ServerLevel level) {
		if(!isEntityRegistered(e) || !loadedLevels.containsKey(level.getLevelId())) {
			System.err.println(this + ": couldn't set entity level, entity " + e + " or level " + level + " is not registered. Ignoring request.");
			return;
		}
		
		ServerLevel oldLevel = entityLevels.put(e, level);
		//if(e instanceof Player) // so it doesn't go too crazy
		//	System.out.println("for "+this+": setting level of entity " + e + " to " + level + " (removing from level "+oldLevel+") - entity location = " + e.getLocation(true));
		
		if(!level.equals(oldLevel)) {
			actOnEntitySet(level, set -> set.add(e));
			actOnEntitySet(oldLevel, set -> set.remove(e));
		}
		
		ServerLevel newLevel = getEntityLevel(e);
		
		if(!Objects.equals(oldLevel, newLevel)) {
			if(oldLevel != null)
				server.broadcast(new EntityRemoval(e), oldLevel, (ServerEntity) e);
			if(newLevel != null)
				server.broadcast(new EntityAddition(e), newLevel, (ServerEntity) e);
		}
	}
	
	// removes entity from levels, without deregistering it. Can only be done for keep alives.
	protected void removeFromLevels(@NotNull ServerEntity e) {
		if(!isKeepAlive(e))
			System.err.println(this+": entity "+e+" is not a keep-alive; will not remove from levels without deregistering.");
		else {
			ServerLevel level = e.getLevel();
			if(level != null)
				actOnEntitySet(level, set -> {
					entityLevels.put(e, null);
					set.remove(e);
				});
		}
	}
	
	public boolean isKeepAlive(WorldObject obj) {
		synchronized (keepAlives) { return keepAlives.contains(obj); }
	}
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	@NotNull
	public ServerPlayer addPlayer(String playerName) {
		ServerPlayer player = new ServerPlayer(playerName);
		
		synchronized (keepAlives) {
			keepAlives.add(player);
		}
		
		respawnPlayer(player);
		
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
	
	// called on player death
	public void despawnPlayer(ServerPlayer player) {
		server.sendToPlayer(player, new EntityRemoval(player));
		removeFromLevels(player);
	}
	
	public void respawnPlayer(ServerPlayer player) {
		player.reset();
		
		ServerLevel level = getLevel(0);
		
		if(level == null)
			throw new NullPointerException("Surface level found to be null while attempting to spawn player.");
		
		// find a good spawn location near the middle of the map
		// Rectangle spawnBounds = levelGenerator.getSpawnArea(new Rectangle());
		
		level.spawnMob(player);
	}
	
	public void removePlayer(ServerPlayer player) {
		synchronized (keepAlives) {
			keepAlives.remove(player);
		}
	}
	
	
	/*  --- GET METHODS --- */
	
	
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		Array<WorldObject> keepAlives = new Array<>();
		synchronized (this.keepAlives) {
			for(WorldObject obj : this.keepAlives)
				if(level.equals(obj.getLevel()))
					keepAlives.add(obj);
		}
		
		return keepAlives;
	}
	
	@Override
	public ServerEntity getEntity(int eid) { return (ServerEntity) super.getEntity(eid); }
	
	@Override
	public ServerTileType getTileType(TileTypeEnum type) { return ServerTileType.get(type); }
	
	public GameServer getServer() { return server; }
	
	public ProtoIsland[] getIslandGenerators() { return islandGenerators; }
	
	public WorldFile getWorldFile() { return worldFile; }
	
	@Override
	public ServerLevel getLevel(int levelId) { return getLevel(levelId, false); }
	public ServerLevel getLevel(int levelId, boolean load) {
		ServerLevel level = loadedLevels.get(levelId);
		if(level == null && load) {
			loadLevel(levelId);
			level = getLevel(levelId);
		}
		return level;
	}
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return entityLevels.get((ServerEntity) e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
