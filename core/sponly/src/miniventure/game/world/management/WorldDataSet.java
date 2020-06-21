package miniventure.game.world.management;

import java.io.RandomAccessFile;
import java.nio.file.Path;

import miniventure.game.util.Version;

// contains a complete set of data needed to initialize a world (not suited to keep it running).
public class WorldDataSet {
	public final Path worldPath;
	public final RandomAccessFile lockRef;
	public final long seed;
	public final float gameTime;
	public final float timeOfDay;
	public final Version dataVersion;
	public final int playerLevel;
	public final String playerData;
	
	public WorldDataSet(Path worldPath, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, int playerLevel, String playerData) {
		this.worldPath = worldPath;
		this.lockRef = lockRef;
		this.seed = seed;
		this.gameTime = gameTime;
		this.timeOfDay = timeOfDay;
		this.dataVersion = dataVersion;
		this.playerLevel = playerLevel;
		this.playerData = playerData != null && playerData.equals("null") ? null : playerData;
	}
}
