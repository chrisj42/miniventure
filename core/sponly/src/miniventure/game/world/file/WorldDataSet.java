package miniventure.game.world.file;

import java.io.RandomAccessFile;
import java.nio.file.Path;

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
	public final IslandCache[] islandCaches;
	public final boolean create;
	
	public WorldDataSet(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerData[] playerInfo, IslandCache[] islandCaches) {
		this(worldFile, lockRef, seed, gameTime, timeOfDay, dataVersion, playerInfo, islandCaches, false);
	}
	WorldDataSet(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerData[] playerInfo, IslandCache[] islandCaches, boolean create) {
		this.worldFile = worldFile;
		this.lockRef = lockRef;
		this.seed = seed;
		this.gameTime = gameTime;
		this.timeOfDay = timeOfDay;
		this.dataVersion = dataVersion;
		this.playerInfo = playerInfo;
		this.islandCaches = islandCaches;
		this.create = create;
	}
}
