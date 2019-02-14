package miniventure.game.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.MapRequest;

import org.jetbrains.annotations.Nullable;

public class WorldFile {
	
	/*
		some notes on my plans for world gen/load:
			- created worlds are immediately saved to file, so there only has to be one interface for the main code to load a world. The world generator does generate with objects first, so it seems kinda redundant, but I think it will be worth it, especially since new worlds are not created often.
			- worlds will probably be saved to multiple files...in a folder. Though I still like the idea of one file for the sake of a write-lock. Anyway, one file is kept in read/write lock to make sure a world is not loaded by two instances of the game simultaneously. This seems a little silly in that you almost certainly won't have multiple instances of the game open... maybe. But it's still good to put just in case, since otherwise weird things could happen.
			- I have yet to figure out how world gen works specifically...
			- I'm at this stalemate where I'm not sure what to do with world gen before I have a save format, but I'm not sure of the save format until I have world gen....
				- I should probably do world gen first, then make a matching save format.
	 */
	
	/*
		This class will be used to maintain a file lock for the world, and get file paths of files based on the various save sections.
	 */
	
	public static final String LOCK_FILE = "session.lock";
	
	
	
	
	
	public static File getLocation(String worldname) {
		return GameCore.GAME_DIR.resolve("saves").resolve(worldname).toFile();
	}
	
	@Nullable
	public static RandomAccessFile tryLockWorld(File worldFolder) {
		File lockFile = worldFolder.toPath().resolve(LOCK_FILE).toFile();
		try {
			lockFile.createNewFile();
		} catch(IOException e) {
			System.err.println("lock file I/O error: "+e.getMessage());
			return null;
		}
		
		RandomAccessFile rf;
		try {
			rf = new RandomAccessFile(lockFile, "rws");
		} catch(FileNotFoundException e) {
			System.err.println("somehow lock file managed to not be found right after creating it...: "+e.getMessage());
			return null;
		}
		
		try {
			if(rf.getChannel().tryLock() != null)
				return rf;
			else
				return null;
		} catch(IOException e) {
			System.err.println("error acquiring file lock: "+e.getMessage());
			return null;
		}
	}
	
	public static WorldFile createWorld(File file, RandomAccessFile lockRef) { return createWorld(file, lockRef, new Random().nextLong()); }
	public static WorldFile createWorld(File file, RandomAccessFile lockRef, String seedString) {
		if(seedString == null) return createWorld(file, lockRef);
		
		if(seedString.length() > 2)
			return createWorld(file, lockRef, seedString.hashCode());
		String s1 = seedString.substring(0, seedString.length()/2);
		String s2 = seedString.substring(seedString.length()/2);
		
		String b1 = Integer.toBinaryString(s1.hashCode());
		String b2 = Integer.toBinaryString(s2.hashCode());
		long seed = Long.parseLong(b1+b2, 2);
		return createWorld(file, lockRef, seed);
	}
	public static WorldFile createWorld(File file, RandomAccessFile lockRef, long seed) {
		return null;
	}
	
	public static WorldFile loadWorld(File file, RandomAccessFile lockRef) {
		return null;
	}
	
	
	// a file that contains data about the world
	
	// private final LevelCache[] levelCaches;
	
	
	private WorldFile() {
		
	}
	
	public MapRequest getMapData() {
		return null;
	}
	
	
}
