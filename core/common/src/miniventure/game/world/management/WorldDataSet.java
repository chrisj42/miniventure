package miniventure.game.world.management;

import java.io.RandomAccessFile;
import java.nio.file.Path;

import miniventure.game.util.Version;

// contains a complete set of data needed to initialize a world (not suited to keep it running).
public class WorldDataSet {
	final Path worldFile;
	final RandomAccessFile lockRef;
	final long seed;
	final float gameTime;
	final float timeOfDay;
	final Version dataVersion;
	final PlayerInfo[] playerInfo;
	final LevelCache[] levelCaches;
	final boolean create;
	
	WorldDataSet(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerInfo[] playerInfo, LevelCache[] levelCaches) {
		this(worldFile, lockRef, seed, gameTime, timeOfDay, dataVersion, playerInfo, levelCaches, false);
	}
	WorldDataSet(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerInfo[] playerInfo, LevelCache[] levelCaches, boolean create) {
		this.worldFile = worldFile;
		this.lockRef = lockRef;
		this.seed = seed;
		this.gameTime = gameTime;
		this.timeOfDay = timeOfDay;
		this.dataVersion = dataVersion;
		this.playerInfo = playerInfo;
		this.levelCaches = levelCaches;
		this.create = create;
	}
}
