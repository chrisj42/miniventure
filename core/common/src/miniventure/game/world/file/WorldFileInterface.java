package miniventure.game.world.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import miniventure.game.core.GameCore;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.Version.VersionFormatException;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.management.TimeOfDay;
import miniventure.game.world.tile.TileStack.TileData;
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
	
	private static final int FIRST_ISLAND_ID = 0;
	
	private static final String VERSION_FILE = "version.txt";
	
	private static final String PLAYER_FILE = "players.txt";
	private static final String GAME_FILE = "game.txt";
	
	private static final String ISLAND_FILE_REGEX = "island-\\d+-(surface|caves)\\.txt";
	private static String getIslandFileName(LevelId id) {
		return getIslandFileName(id.getIslandId(), id.isSurface());
	}
	private static String getIslandFileName(int id, boolean surface) {
		return "island-"+(id<10?"0":"")+id+'-'+(surface?"surface":"caves")+".txt";
	}
	
	private static final String LOCK_FILE = "session.lock";
	
	private static Path dataImportSource; // stores the source location of auto-imported game data
	public static Path getDataImportSource() { return dataImportSource; }
	
	// ensure all the needed folders have been created.
	public static void initGameDir() {
		try {
			Files.createDirectories(getLocation(""));
		} catch(IOException e) {
			System.err.println("Error creating default game directory:");
			e.printStackTrace();
		}
	}
	
	public static Path getLocation(String worldname) {
		return GameCore.GAME_DIR.resolve("saves").resolve(worldname);
	}
	
	@NotNull
	static Version getWorldVersion(Path worldFolder) throws IOException, VersionFormatException {
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
	
	private static Path getPath(String path) {
		return new java.io.File(path).toPath();
	}
	
	// attempts to migrate data from an old game dir to the current one, assuming there is no current data.
	// if old data is found, the source folder is stored for later.
	public static void migrate(String... oldDirs) {
		final Path gameDir = getPath(GameCore.DEFAULT_GAME_DIR);
		
		if(Files.exists(gameDir))
			return;
		
		for(String pathName: oldDirs) {
			Path path = getPath(pathName);
			if(Files.exists(path)) {
				try {
					Files.createDirectories(gameDir.getParent());
					Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
							return visitFile(dir, attrs);
						}
						
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.copy(file, gameDir.resolve(path.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
							return FileVisitResult.CONTINUE;
						}
					});
					
					dataImportSource = path;
				} catch(IOException e) {
					System.err.println("Failure migrating old save data:");
					e.printStackTrace();
				}
				return;
			}
		}
	}
	
	public static boolean deleteRecursively(Path folder) {
		try {
			Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if(!dir.equals(folder))
						Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			MyUtils.sleep(2);
			Files.delete(folder);
			return true;
		} catch(IOException e) {
			System.err.println("Error deleting folder '"+folder+"':");
			e.printStackTrace();
			return false;
		}
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
	private static WorldDataSet createWorld(Path folder, RandomAccessFile lockRef, long seed) {
		int i = FIRST_ISLAND_ID;
		//noinspection UnusedAssignment
		IslandDataManager[] islandCaches = new IslandDataManager[] {
			new IslandDataManager(i++, IslandType.WOODLAND, seed, true),
			new IslandDataManager(i++, IslandType.DESERT, seed, false),
			new IslandDataManager(i++, IslandType.ARCTIC, seed, false)
		};
		
		WorldDataSet worldData = WorldDataSet.fromNew(folder, lockRef, seed, TimeOfDay.Morning.getStartOffsetSeconds(), islandCaches);
		saveWorld(worldData);
		return worldData;
	}
	
	public static boolean saveWorld(WorldDataSet worldData) {
		Path main = worldData.worldFile;
		boolean good;
		
		good = writeFile(main.resolve(VERSION_FILE), list -> list.add(Version.CURRENT.serialize()));
		
		good = writeFile(main.resolve(GAME_FILE), list -> {
			SerialHashMap map = new SerialHashMap();
			map.add("seed", worldData.seed);
			map.add("gt", (int)worldData.gameTime); // accuracy isn't worth the extra space
			map.add("time", (int)worldData.timeOfDay); // same here
			map.add("islands", worldData.islandManagers.length);
			
			list.add(map.serialize());
			
			for(IslandDataManager island: worldData.islandManagers)
				list.add(island.serialize());
			
		}) && good;
		
		for(LevelDataSet levelData: worldData.loadedLevels) {
			good = saveLevel(main, levelData) && good;
		}
		
		good = writeFile(main.resolve(PLAYER_FILE), list -> {
			for(PlayerData p: worldData.playerInfo) {
				p.serialize(list);
			}
		}) && good;
		
		return good;
	}
	
	// todo add a ProgressLogger parameter so load progress can be tracked... assuming the whole method takes more than half a second, at least. -- which I've confirmed it does.
	@NotNull
	public static WorldDataSet loadWorld(WorldReference worldRef, RandomAccessFile lockRef) throws WorldFormatException {
		final Path folder = worldRef.folder;
		
		final Version version = worldRef.version;
		if(version == null)
			throw new WorldFormatException("Unsupported version format", worldRef.versionError);
		
		try {
			LinkedList<String> lines = new LinkedList<>();
			
			// read global data
			readFile(folder.resolve(GAME_FILE), lines);
			
			SerialHashMap map = new SerialHashMap(lines.pop());
			final long seed = map.get("seed", Long::parseLong);
			final float gameTime = map.get("gt", Float::parseFloat);
			final float daytime = map.get("time", Float::parseFloat);
			final int islandCount = map.get("islands", Integer::parseInt);
			
			IslandDataManager[] islands = new IslandDataManager[islandCount];
			for(int i = 0; i < islands.length; i++)
				islands[i] = new IslandDataManager(version, lines.pop());
			
			// read terrain / entity / level data
			/*Set<Path> islandFiles = Files.list(folder)
				.filter(path -> path.getFileName().toString().matches(ISLAND_FILE_REGEX))
				.collect(Collectors.toCollection(TreeSet::new));*/
			
			// NOTE: does not read info about generated levels, only which are generated.
			
			// for(int i = 0; i < islands.length; i++) {
				// readFile(folder.resolve(getIslandFileName(i, true)), lines);
				// LevelCacheFetcher surfaceCache = (island, isSurface) -> new LevelCache(island, isSurface, version, lines);
				// readFile(folder.resolve(getIslandFileName(i, false)), lines, false);
				// LevelCacheFetcher cavernCache = (island, isSurface) -> new LevelCache(island, isSurface, version, lines);
			// }
			
			// read player data
			readFile(folder.resolve(PLAYER_FILE), lines);
			PlayerData[] players = new PlayerData[lines.size() / 5];
			
			for(int i = 0; i < players.length; i++) {
				String name = lines.pop();
				String passhash = lines.pop();
				LevelId level = LevelId.getId(Integer.parseInt(lines.pop()));
				String data = lines.pop();
				boolean op = Boolean.parseBoolean(lines.pop());
				players[i] = new PlayerData(name, passhash, data, level, op);
			}
			
			return WorldDataSet.fromFile(folder, lockRef, seed, gameTime, daytime, version, players, islands);
		} catch(FileNotFoundException e) {
			throw new WorldFormatException("Missing file", e);
		} catch(IOException e) {
			// throw new WorldFormatException("Save format of world '"+folder+"' is invalid", e);
			throw new WorldFormatException("Bad file", e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new WorldFormatException("Error loading world", e);
		} finally {
			if(lockRef != null) {
				try {
					lockRef.close();
				} catch(IOException ignored) {}
			}
		}
	}
	
	public static boolean saveLevel(Path worldFolder, LevelDataSet levelData) {
		return writeFile(worldFolder.resolve(getIslandFileName(levelData.levelId)), lines -> {
			SerialHashMap map = new SerialHashMap();
			map.add("w", levelData.width);
			map.add("h", levelData.height);
			map.add("ec", levelData.entityData.size());
			lines.add(map.serialize());
			
			lines.addAll(levelData.entityData);
			
			for(TileData[] tileCol: levelData.tileData)
				for(TileData td: tileCol)
					lines.add(td.serialize());
		});
	}
	
	// note, the version is only ever not the current version when updating old level files.
	public static LevelDataSet loadLevel(Path worldFolder, LevelId levelId, Version dataVersion) throws WorldFormatException {
		try {
			LinkedList<String> lines = new LinkedList<>();
			String filename = getIslandFileName(levelId);
			readFile(worldFolder.resolve(filename), lines);
			
			SerialHashMap map = new SerialHashMap(lines.pop());
			final int width = map.get("w", Integer::parseInt);
			final int height = map.get("h", Integer::parseInt);
			final int entityCount = map.get("ec", Integer::parseInt);
			
			ArrayList<String> entityData = new ArrayList<>(entityCount);
			for(int i = 0; i < entityCount; i++)
				entityData.add(lines.pop());
			
			TileData[][] tileData = new TileData[width][height];
			// TODO here is where I can check for compressed tiles
			for(int xp = 0; xp < width; xp++)
				for(int yp = 0; yp < height; yp++)
					tileData[xp][yp] = new TileData(dataVersion, lines.pop());
			
			return new LevelDataSet(dataVersion, levelId, width, height, tileData, entityData);
			
		} catch(IOException e) {
			throw new WorldFormatException("Error loading level", e);
		}
	}
	
	// removes all possibly useful files from the worldFiles set, so that remaining files are considered "extra".
	// returns a collection of the names of the missing files.
	static Collection<String> validateWorldFiles(Set<Path> worldFiles) {
		LinkedList<String> missing = new LinkedList<>();
		
		// todo add the surface and cavern files for every island in IslandType (except menu)
		for(String file: new String[] {
				VERSION_FILE, GAME_FILE, PLAYER_FILE,
				getIslandFileName(FIRST_ISLAND_ID, true)
		})
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
	/** @noinspection SameParameterValue*/
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
			System.err.println("error writing file");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
