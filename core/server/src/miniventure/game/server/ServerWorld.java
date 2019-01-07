package miniventure.game.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.GameProtocol.EntityRemoval;
import miniventure.game.GameProtocol.WorldData;
import miniventure.game.ProgressPrinter;
import miniventure.game.item.FoodType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolType;
import miniventure.game.world.Config;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.mapgen.WorldGenerator;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ServerWorld extends WorldManager {
	
	/*
		This is what contains the world. GameCore will check if the world is loaded here, and if not it won't render the game screen. Though... perhaps this class should hold a reference to the game screen instead...? Because, if you don't have a world, you don't need a game screen...
		
		The world will be created with this.
		It holds references to the current game level.
		You use this class to start processes on the whole world, like saving, loading, creating.
			And accessing the main player... and respawning. Also changing the player level?
		
		Perhaps this instance can be fetched from GameCore.
		
		GameScreen... game screen won't do much, just do the rendering. 
	 */
	
	private WorldGenerator worldGenerator;
	private GameServer server;
	
	private boolean worldLoaded = false;
	
	private final Set<WorldObject> keepAlives = Collections.synchronizedSet(new HashSet<>()); // always keep chunks around these objects loaded.
	
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
			level.update(getEntities(level), delta);
		
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
	public void createWorld(int width, int height) {
		worldLoaded = false;
		
		worldGenerator = new WorldGenerator(MathUtils.random.nextLong(), 200, 200);
		ProgressPrinter logger = new ProgressPrinter();
		
		logger.pushMessage("");
		clearLevels();
		int numLevels = 1;
		// int minId = 1;
		for(int i = 0; i < numLevels; i++) {
			logger.editMessage("Loading level "+(i+1)+'/'+numLevels+"...");
			loadLevel(i);
		}
		logger.popMessage();
		
		worldLoaded = true;
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	@Override
	public void exitWorld(boolean save) {
		if(!worldLoaded) return;
		// dispose of level/world resources
		synchronized (keepAlives) { keepAlives.clear(); }
		clearLevels();
		worldGenerator = null;
		server.stop();
		worldLoaded = false;
		//spawnTile = null;
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	protected void loadLevel(int levelId) {
		// TODO this will need to get redone when loading from file
		
		if(isLevelLoaded(levelId)) return;
		LevelGenerator levelGenerator = worldGenerator.getLevelGenerator(levelId);
		
		addLevel(new ServerLevel(this, levelId, levelGenerator));
	}
	
	@Override
	protected void unloadLevel(int levelId) {
		// TODO save to file before calling super
		super.unloadLevel(levelId);
	}
	
	@Override
	protected void pruneLoadedLevels() {
		HashSet<Integer> safeIds = new HashSet<>(getLoadedLevelCount());
		
		// log which levels have a keep-alive
		synchronized (keepAlives) {
			for(WorldObject obj : keepAlives) {
				Level level = obj.getLevel();
				if(level != null)
					safeIds.add(level.getLevelId());
			}
		}
		
		// unload any loaded level that didn't have a keep-alive
		for(int id: getLoadedLevelIds())
			if(!safeIds.contains(id))
				unloadLevel(id);
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public void deregisterEntity(int eid) {
		Entity e = getEntity(eid);
		ServerLevel prev = getEntityLevel(e);
		super.deregisterEntity(eid);
		if(e != null && prev != null)
			server.broadcast(new EntityRemoval(e), prev, (ServerEntity) e);
	}
	
	@Override
	public void setEntityLevel(@NotNull Entity e, @NotNull Level level) {
		ServerLevel previous = getEntityLevel(e);
		super.setEntityLevel(e, level);
		ServerLevel newLevel = getEntityLevel(e);
		
		if(!Objects.equals(previous, newLevel)) {
			if(previous != null)
				server.broadcast(new EntityRemoval(e), previous, (ServerEntity) e);
			if(newLevel != null)
				server.broadcast(new EntityAddition(e), newLevel, (ServerEntity) e);
		}
	}
	
	@Override
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
			player.getInventory().addItem(TileItem.get(TileTypeEnum.CLOSED_DOOR));
			player.getInventory().addItem(TileItem.get(TileTypeEnum.TORCH));
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
	
	
	@Override
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
	public TileType getTileType(TileTypeEnum type) { return ServerTileType.get(type); }
	
	public GameServer getServer() { return server; }
	
	@Override
	public ServerLevel getLevel(int levelId) { return (ServerLevel) super.getLevel(levelId); }
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return (ServerLevel) super.getEntityLevel(e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
