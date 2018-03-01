package miniventure.server;

import java.util.HashSet;

import miniventure.game.GameCore;
import miniventure.game.TimeOfDay;
import miniventure.game.WorldManager;
import miniventure.game.world.Chunk;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.levelgen.LevelGenerator;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;

import org.jetbrains.annotations.NotNull;

public class ServerWorld implements WorldManager {
	
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
	
	private float gameTime = 0;
	private boolean worldLoaded = false;
	
	private final HashSet<WorldObject> keepAlives = new HashSet<>(); // always keep chunks around these objects loaded.
	
	public ServerWorld() {
		server = new GameServer();
		server.startServer();
	}
	
	@Override
	public boolean worldLoaded() { return worldLoaded; }
	
	@Override
	public void createWorld(int width, int height) {
		worldLoaded = false;
		levelGenerator = new LevelGenerator(MathUtils.random.nextLong(), width, height, 32, 6);
		ServerLevel.resetLevels(this, levelGenerator);
	}
	
	@Override
	public void update(float delta) {
		
		HashSet<ServerLevel> loadedLevels = new HashSet<>();
		for(WorldObject obj: keepAlives) {
			ServerLevel level = obj.getServerLevel();
			if(level != null)
				loadedLevels.add(level);
		}
		
		for(ServerLevel level: loadedLevels)
			level.update(delta);
		
		gameTime += delta;
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	@Override
	public void exitWorld(boolean save) {
		// dispose of level/world resources
		keepAlives.clear();
		Level.clearLevels();
		levelGenerator = null;
		//spawnTile = null;
	}
	
	public Player addPlayer() {
		Player player = new Player();
		keepAlives.add(player);
		
		respawnPlayer(player);
		
		return player;
	}
	
	public void respawnPlayer(Player player) {
		player.reset();
		
		ServerLevel level = ServerLevel.getLevel(0);
		
		if(level == null)
			throw new NullPointerException("Surface level found to be null while attempting to spawn player.");
		
		// find a good spawn location near the middle of the map
		
		Rectangle spawnBounds = new Rectangle(0, 0, Math.min(level.getWidth(), 5*Chunk.SIZE), Math.min(level.getHeight(), 5*Chunk.SIZE));
		spawnBounds.setCenter(level.getWidth()/2, level.getHeight()/2);
		
		level.spawnMob(player, spawnBounds, false);
	}
	
	public boolean isKeepAlive(WorldObject obj) {
		return keepAlives.contains(obj);
	}
	
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		Array<WorldObject> keepAlives = new Array<>();
		for(WorldObject obj: this.keepAlives)
			if(obj.getLevel() == level)
				keepAlives.add(obj);
		
		return keepAlives;
	}
	
	@Override
	public float getGameTime() { return gameTime; }
}
