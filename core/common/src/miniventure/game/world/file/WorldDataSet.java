package miniventure.game.world.file;

import java.io.RandomAccessFile;
import java.nio.file.Path;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.Version;

// contains a complete set of data needed to initialize a world (not suited to keep it running).
public class WorldDataSet {
	
	public final Path worldFile;
	public final RandomAccessFile lockRef;
	public final long seed;
	public final float gameTime;
	public final float timeOfDay;
	public final Version dataVersion;
	public final PlayerData[] playerInfo;
	public final IslandDataManager[] islandManagers;
	public final LevelDataSet[] loadedLevels;
	public final boolean create;
	
	// for a freshly generated world
	public static WorldDataSet fromNew(Path worldFile, RandomAccessFile lockRef, long seed, float timeOfDay, IslandDataManager[] islandCaches) {
		return new WorldDataSet(worldFile, lockRef, seed, 0, timeOfDay, Version.CURRENT, new PlayerData[0], islandCaches, ArrayUtils.empty(LevelDataSet.class), true);
	}
	
	// for a currently loaded world
	public static WorldDataSet fromLoaded(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, PlayerData[] playerInfo, IslandDataManager[] islandCaches, LevelDataSet[] loadedLevels) {
		return new WorldDataSet(worldFile, lockRef, seed, gameTime, timeOfDay, Version.CURRENT, playerInfo, islandCaches, loadedLevels, false);
	}
	
	// for a previously saved world
	public static WorldDataSet fromFile(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerData[] playerInfo, IslandDataManager[] islandCaches) {
		return new WorldDataSet(worldFile, lockRef, seed, gameTime, timeOfDay, dataVersion, playerInfo, islandCaches, ArrayUtils.empty(LevelDataSet.class), false);
	}
	
	private WorldDataSet(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerData[] playerInfo, IslandDataManager[] islandManagers, LevelDataSet[] loadedLevels, boolean create) {
		this.worldFile = worldFile;
		this.lockRef = lockRef;
		this.seed = seed;
		this.gameTime = gameTime;
		this.timeOfDay = timeOfDay;
		this.dataVersion = dataVersion;
		this.playerInfo = playerInfo;
		this.islandManagers = islandManagers;
		this.loadedLevels = loadedLevels;
		this.create = create;
	}
}
