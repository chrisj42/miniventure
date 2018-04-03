package miniventure.game.server;

import java.util.HashSet;

import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.GameProtocol.EntityRemoval;
import miniventure.game.ProgressPrinter;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Chunk;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.ServerPlayer;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.DestructibleProperty;
import miniventure.game.world.tile.ServerDestructibleProperty;
import miniventure.game.world.tile.TilePropertyFetcher;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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
	
	private LevelGenerator levelGenerator;
	private GameServer server;
	
	private boolean worldLoaded = false;
	
	private final HashSet<WorldObject> keepAlives = new HashSet<>(); // always keep chunks around these objects loaded.
	
	public ServerWorld() {
		super(new TilePropertyFetcher((instanceTemplate -> {
			if(instanceTemplate instanceof DestructibleProperty)
				return new ServerDestructibleProperty((DestructibleProperty)instanceTemplate);
			
			return instanceTemplate;
		})));
		
		server = new GameServer();
		server.startServer();
	}
	
	// Update method
	
	@Override
	public void update(float delta) {
		
		HashSet<ServerLevel> loadedLevels = new HashSet<>();
		for(WorldObject obj: keepAlives) {
			ServerLevel level = (ServerLevel) obj.getLevel();
			if(level != null)
				loadedLevels.add(level);
		}
		
		for(ServerLevel level: loadedLevels)
			level.update(getEntities(level), delta);
		
		gameTime += delta;
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	@Override
	public boolean worldLoaded() { return worldLoaded; }
	
	@Override
	public void createWorld(int width, int height) {
		worldLoaded = false;
		levelGenerator = new LevelGenerator(MathUtils.random.nextLong(), width, height, 32, 6);
		
		ProgressPrinter logger = new ProgressPrinter();
		
		logger.pushMessage("");
		clearLevels();
		int numLevels = 1;
		int minDepth = 0;
		for(int i = 0; i < numLevels; i++) {
			logger.editMessage("Loading level "+(i+1)+"/"+numLevels+"...");
			addLevel(new ServerLevel(this, i + minDepth, levelGenerator));
		}
		logger.popMessage();
		
		worldLoaded = true;
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	@Override
	public void exitWorld(boolean save) {
		// dispose of level/world resources
		keepAlives.clear();
		clearLevels();
		levelGenerator = null;
		//spawnTile = null;
	}
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public void setEntityLevel(@NotNull Entity e, @NotNull Level level) {
		ServerLevel previous = getEntityLevel(e);
		super.setEntityLevel(e, level);
		ServerLevel newLevel = getEntityLevel(e);
		
		if(!MyUtils.nullablesAreEqual(previous, newLevel)) {
			if(previous != null)
				server.broadcast(new EntityRemoval(e), previous, (ServerEntity) e);
			if(newLevel != null)
				server.broadcast(new EntityAddition(e), newLevel, (ServerEntity) e);
		}
	}
	
	@Override
	public boolean isKeepAlive(WorldObject obj) {
		return keepAlives.contains(obj);
	}
	
	
	/*  --- PLAYER MANAGEMENT --- */
	
	
	public ServerPlayer addPlayer(String playerName) {
		ServerPlayer player = new ServerPlayer(playerName);
		
		keepAlives.add(player);
		
		respawnPlayer(player);
		
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
		
		Rectangle spawnBounds = new Rectangle(0, 0, Math.min(level.getWidth(), 5*Chunk.SIZE), Math.min(level.getHeight(), 5*Chunk.SIZE));
		spawnBounds.setCenter(level.getWidth()/2, level.getHeight()/2);
		
		level.spawnMob(player, spawnBounds);
	}
	
	public void removePlayer(ServerPlayer player) {
		keepAlives.remove(player);
	}
	
	
	/*  --- GET METHODS --- */
	
	
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		Array<WorldObject> keepAlives = new Array<>();
		for(WorldObject obj: this.keepAlives)
			if(obj.getLevel() == level)
				keepAlives.add(obj);
		
		return keepAlives;
	}
	
	public GameServer getServer() { return server; }
	
	@Override
	public ServerLevel getLevel(int depth) { return (ServerLevel) super.getLevel(depth); }
	
	@Override
	public ServerLevel getEntityLevel(Entity e) { return (ServerLevel) super.getEntityLevel(e); }
	
	@Override
	public String toString() { return "ServerWorld"; }
}
