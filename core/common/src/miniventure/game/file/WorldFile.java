package miniventure.game.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import miniventure.game.GameCore;

import org.jetbrains.annotations.Nullable;

public class WorldFile {
	
	public static final String LOCK_FILE = "session.lock";
	
	// info on previously loaded islands
	
	public static File getLocation(String worldname) {
		return GameCore.GAME_DIR.resolve("saves").resolve(worldname).toFile();
	}
	
	@Nullable
	public static RandomAccessFile tryLockWorld(File worldFolder) {
		File lockFile = worldFolder.toPath().resolve(LOCK_FILE).toFile();
		try {
			lockFile.createNewFile();
		} catch(IOException e) {
			System.out.println("lock file I/O error: "+e.getMessage());
			return null;
		}
		
		RandomAccessFile rf;
		try {
			rf = new RandomAccessFile(lockFile, "rws");
		} catch(FileNotFoundException e) {
			System.out.println("somehow lock file managed to not be found right after creating it...: "+e.getMessage());
			return null;
		}
		
		try {
			if(rf.getChannel().tryLock() != null)
				return rf;
			else
				return null;
		} catch(IOException e) {
			System.out.println("error acquiring file lock: "+e.getMessage());
			return null;
		}
	}
	
	// a file that contains data about the world
	
	private WorldFile() {
		
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
		
	}
	
	public static WorldFile loadWorld(File file, RandomAccessFile lockRef) {
		
	}
	
	
}
