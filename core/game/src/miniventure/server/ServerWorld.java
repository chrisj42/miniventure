package miniventure.server;

import java.util.HashSet;

import miniventure.game.TimeOfDay;
import miniventure.game.world.Chunk;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.levelgen.LevelGenerator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;

import org.jetbrains.annotations.NotNull;

public class ServerWorld {
	
	/*
		This is what contains the world. GameCore will check if the world is loaded here, and if not it won't render the game screen. Though... perhaps this class should hold a reference to the game screen instead...? Because, if you don't have a world, you don't need a game screen...
		
		The world will be created with this.
		It holds references to the current game level.
		You use this class to start processes on the whole world, like saving, loading, creating.
			And accessing the main player... and respawning. Also changing the player level?
		
		Perhaps this instance can be fetched from GameCore.
		
		GameScreen... game screen won't do much, just do the rendering. 
	 */
	
	public static final String doneMsg = "server ready";
	
	private LevelGenerator levelGenerator;
	private GameServer server;
	
	private float gameTime;
	
	private final HashSet<WorldObject> keepAlives = new HashSet<>(); // always keep chunks around these objects loaded.
	
	private static final long START_TIME = System.nanoTime();
	
	public ServerWorld() { this(0, 0); }
	public ServerWorld(int width, int height) {
		System.out.println("loading server world...");
		
		gameTime = 0;
		
		levelGenerator = new LevelGenerator(MathUtils.random.nextLong(), width, height, 32, 6);
		System.out.println("loading levels...");
		ServerLevel.resetLevels(levelGenerator);
		
		System.out.println("done!");
		
		System.out.println("starting server...");
		server = new GameServer(this);
		
		System.out.println("done!");
		
		System.out.println(doneMsg);
	}
	
	public void run() {
		long lastNow = System.nanoTime();
		
		//noinspection InfiniteLoopStatement
		while(true) {
			long now = System.nanoTime();
			update((now-lastNow)/1E9f);
			lastNow = now;
			
			try {
				Thread.sleep(10);
			} catch(InterruptedException ignored) {}
		}
	}
	
	private void update(float delta) {
		
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
	
	String getTimeOfDayString() {
		return TimeOfDay.getTimeOfDay(gameTime).getTimeString(gameTime);
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	public void exit() {
		// dispose of level/world resources
		keepAlives.clear();
		Level.clearLevels();
		levelGenerator = null;
		//spawnTile = null;
	}
	
	public Player spawnPlayer(Connection connection) {
		Player player = new Player();
		keepAlives.add(player);
		
		ServerLevel level = ServerLevel.getLevel(0);
		
		if(level == null)
			throw new NullPointerException("Surface level found to be null while attempting to spawn player.");
		
		// find a good spawn location near the middle of the map
		
		Rectangle spawnBounds = new Rectangle(0, 0, Math.min(level.getWidth(), 5*Chunk.SIZE), Math.min(level.getHeight(), 5*Chunk.SIZE));
		spawnBounds.setCenter(level.getWidth()/2, level.getHeight()/2);
		
		level.spawnMob(player, spawnBounds, false);
		return player;
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
	
	public static float getElapsedProgramTime() { return (System.nanoTime() - START_TIME)/1E9f; }
}
