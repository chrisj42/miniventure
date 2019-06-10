package miniventure.game.world.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import miniventure.game.GameCore;
import miniventure.game.util.SerialDataMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.Point;
import miniventure.game.world.management.TimeOfDay;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldFileInterface {
	
	private WorldFileInterface() {}
	
	/*
		some notes on my plans for world gen/load:
			- created worlds are immediately saved to file, so there only has to be one interface for the main code to load a world. The world generator does generate with objects first, so it seems kinda redundant, but I think it will be worth it, especially since new worlds are not created often.
			- worlds will probably be saved to multiple files...in a folder. Though I still like the idea of one file for the sake of a write-lock. Anyway, one file is kept in read/write lock to make sure a world is not loaded by two instances of the game simultaneously. This seems a little silly in that you almost certainly won't have multiple instances of the game open... maybe. But it's still good to put just in case, since otherwise weird things could happen.
			- I have yet to figure out how world gen works specifically...
			- I'm at this stalemate where I'm not sure what to do with world gen before I have a save format, but I'm not sure of the save format until I have world gen....
				- I should probably do world gen first, then make a matching save format.
		
		
		I've considered having the file lock just be a file that is checked for existence/nonexistence rather than establishing a filesystem lock, but if I do that then an unexpected close would leave the file there and break the system, as opposed to a write lock which can only exist while the program runs and so always is dealt with properly.
	 */
	
	/*
		This class will be used to maintain a file lock for the world, and get file paths of files based on the various save sections.
	 */
	
	private static final String VERSION_FILE = "version.txt";
	
	private static final String PLAYER_FILE = "players.txt";
	private static final String GAME_FILE = "game.txt";
	
	private static final String ISLAND_FILE_REGEX = "island-\\d+\\.txt";
	private static String getIslandFileName(int id) { return "island-"+(id<10?"0":"")+id+".txt"; }
	
	private static final String LOCK_FILE = "session.lock";
	
	public static Path getLocation(String worldname) {
		return GameCore.GAME_DIR.resolve("saves").resolve(worldname);
	}
	
	static Version getWorldVersion(Path worldFolder) throws IOException {
		LinkedList<String> lines = new LinkedList<>();
		
		readFile(worldFolder.resolve(VERSION_FILE), lines);
		
		return new Version(lines.pop());
	}
	
	static long getTimestamp(Path worldFolder) throws IOException {
		Path gameFile = worldFolder.resolve(GAME_FILE);
		if(!Files.exists(gameFile))
			return -1;
		
		FileTime time = Files.getLastModifiedTime(gameFile);
		return time.toMillis();
	}
	
	@Nullable
	public static RandomAccessFile tryLockWorld(Path worldFolder) throws IOException {
		Path lockFile = worldFolder.resolve(LOCK_FILE);
		if(!Files.exists(worldFolder))
			Files.createDirectories(worldFolder);
		if(!Files.exists(lockFile)) {
			try {
				Files.createFile(lockFile);
			} catch(IOException e) {
				throw new IOException("Error creating world lock file.", e);
			}
		}
		
		RandomAccessFile rf;
		try {
			rf = new RandomAccessFile(lockFile.toFile(), "rws");
		} catch(FileNotFoundException e) {
			throw new IOException("Error opening world lock file.", e);
		}
		
		try {
			if(rf.getChannel().tryLock() != null)
				return rf;
			else
				return null;
		} catch(IOException e) {
			throw new IOException("Error acquiring world lock.", e);
		}
	}
	
	@NotNull
	public static WorldDataSet createWorld(Path file, RandomAccessFile lockRef, String seedString) {
		if(seedString == null || seedString.length() == 0)
			return createWorld(file, lockRef, new Random().nextLong());
		
		try {
			long seed = Long.parseLong(seedString, 16);
			return createWorld(file, lockRef, seed);
		} catch(NumberFormatException ignored) {}
		
		if(seedString.length() < 2)
			return createWorld(file, lockRef, seedString.hashCode());
		
		String s1 = seedString.substring(0, seedString.length()/2);
		String s2 = seedString.substring(seedString.length()/2);
		
		String b1 = Integer.toBinaryString(s1.hashCode());
		String b2 = Integer.toBinaryString(s2.hashCode());
		if(b2.length() < 32) {
			// toBinaryString does not put leading zeros, but we need those.
			StringBuilder b = new StringBuilder(32 - b2.length());
			for(int i = b2.length(); i < 32; i++)
				b.append('0');
			b2 = b+b2;
		}
		long seed = Long.parseLong(b1+b2, 2);
		return createWorld(file, lockRef, seed);
	}
	
	// this method doesn't actually generate levels; the ServerWorld does that when it's created. So very little work is actually done here.
	@NotNull
	private static WorldDataSet createWorld(Path file, RandomAccessFile lockRef, long seed) {
		
		/*try {
			Files.walkFileTree(file, EnumSet.noneOf(FileVisitOption.class), 1, new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if(!dir.equals(file))
						return FileVisitResult.SKIP_SUBTREE;
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(!file.getFileName().toString().equals(LOCK_FILE))
						Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch(IOException e) {
			System.err.println("Error deleting existing files");
			e.printStackTrace();
		}*/
		
		LevelCache[] levelCaches = new LevelCache[] {
			new LevelCache(0, new Point(0, 0), seed, IslandType.STARTER),
			new LevelCache(1, new Point(2, 2), seed, IslandType.DESERT),
			new LevelCache(2, new Point(10, 0), seed, IslandType.SWAMP)
			// new LevelCache(3, new Point(3, -5), seed, IslandType.ARCTIC),
			// new LevelCache(4, new Point(-3, 5), seed, IslandType.JUNGLE),
		};
		
		WorldDataSet worldData = new WorldDataSet(file, lockRef, seed, 0, TimeOfDay.Morning.getStartOffsetSeconds(), GameCore.VERSION, new PlayerData[0], levelCaches, true);
		saveWorld(worldData);
		return worldData;
	}
	
	public static boolean saveWorld(WorldDataSet worldData) {
		Path main = worldData.worldFile;
		boolean good;
		
		good = writeFile(main.resolve(VERSION_FILE), list -> list.add(GameCore.VERSION.serialize()));
		
		good = writeFile(main.resolve(GAME_FILE), list -> {
			SerialDataMap map = new SerialDataMap();
			map.add("seed", worldData.seed);
			map.add("gt", (int)worldData.gameTime); // accuracy isn't worth the extra space
			map.add("time", (int)worldData.timeOfDay); // same here
			map.add("islands", worldData.levelCaches.length); // same here
			list.add(map.serialize());
		}) && good;
		
		int i = 0;
		for(LevelCache level: worldData.levelCaches) {
			good = writeFile(main.resolve(getIslandFileName(i++)), level::save) && good;
		}
		
		good = writeFile(main.resolve(PLAYER_FILE), list -> {
			for(PlayerData p: worldData.playerInfo) {
				p.serialize(list);
			}
		}) && good;
		
		return good;
	}
	
	// todo add a ProgressLogger parameter so load progress can be tracked... assuming the whole method takes more than half a second, at least.
	@NotNull
	public static WorldDataSet loadWorld(WorldReference worldRef, RandomAccessFile lockRef) throws WorldFormatException {
		final Path folder = worldRef.folder;
		
		try {
			LinkedList<String> lines = new LinkedList<>();
			
			final Version version = getWorldVersion(folder);
			
			// read global data
			readFile(folder.resolve(GAME_FILE), lines);
			
			//noinspection MismatchedQueryAndUpdateOfCollection
			SerialDataMap map = new SerialDataMap(lines.pop());
			final long seed = Long.parseLong(map.get("seed"));
			final float gameTime = Float.parseFloat(map.get("gt"));
			final float daytime = Float.parseFloat(map.get("time"));
			final int islandCount = Integer.parseInt(map.get("islands"));
			
			// read terrain / entity / level data
			/*Set<Path> islandFiles = Files.list(folder)
				.filter(path -> path.getFileName().toString().matches(ISLAND_FILE_REGEX))
				.collect(Collectors.toCollection(TreeSet::new));*/
			
			LevelCache[] levels = new LevelCache[islandCount];
			for(int i = 0; i < levels.length; i++) {
				readFile(folder.resolve(getIslandFileName(i)), lines);
				
				levels[i] = new LevelCache(version, lines);
			}
			
			// read player data
			readFile(folder.resolve(PLAYER_FILE), lines);
			PlayerData[] players = new PlayerData[lines.size() / 5];
			
			for(int i = 0; i < players.length; i++) {
				String name = lines.pop();
				String passhash = lines.pop();
				int level = Integer.parseInt(lines.pop());
				String data = lines.pop();
				boolean op = Boolean.parseBoolean(lines.pop());
				players[i] = new PlayerData(name, passhash, data, level, op);
			}
			
			return new WorldDataSet(folder, lockRef, seed, gameTime, daytime, version, players, levels);
		} catch(FileNotFoundException e) {
			throw new WorldFormatException("Missing file", e);
		} catch(IOException e) {
			// throw new WorldFormatException("Save format of world '"+folder+"' is invalid", e);
			throw new WorldFormatException("Bad file", e);
		} catch(Exception e) {
			throw new WorldFormatException("Error loading world", e);
		} finally {
			if(lockRef != null) {
				try {
					lockRef.close();
				} catch(IOException ignored) {}
			}
		}
	}
	
	// removes all possibly useful files from the worldFiles set, so that remaining files are considered "extra".
	// returns a collection of the names of the missing files.
	static Collection<String> validateWorldFiles(Set<Path> worldFiles) {
		LinkedList<String> missing = new LinkedList<>();
		
		for(String file: new String[] {VERSION_FILE, GAME_FILE, PLAYER_FILE, getIslandFileName(0)})
			if(!worldFiles.removeIf(path -> path.getFileName().toString().equals(file)))
				// this executes if the above command doesn't remove the file, i.e. the file wasn't found
				missing.add(file);
		
		// remove all files that look like an island file so they aren't considered "extra"
		worldFiles.removeIf(path -> path.getFileName().toString().matches(ISLAND_FILE_REGEX));
		
		return missing;
	}
	
	private static void readFile(Path path, LinkedList<String> list) throws IOException {
		readFile(path, list, true);
	}
	private static void readFile(Path path, LinkedList<String> list, boolean clearList) throws IOException {
		if(clearList)
			list.clear();
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String s;
			while((s = reader.readLine()) != null)
				list.add(s);
			// actor.act(lines);
		}/* catch(IOException e) {
			// e.printStackTrace();
			return false;
		}
		return true;*/
	}
	
	private static boolean writeFile(Path path, @NotNull ValueAction<LinkedList<String>> populator) {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			
			LinkedList<String> data = new LinkedList<>();
			populator.act(data);
			for(String s: data) {
				writer.write(s);
				writer.newLine();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
