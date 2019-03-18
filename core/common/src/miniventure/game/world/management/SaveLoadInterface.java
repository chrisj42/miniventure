package miniventure.game.world.management;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.IslandReference;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialDataMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.Point;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelFetcher;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.worldgen.island.IslandType;

import org.jetbrains.annotations.Nullable;

public class SaveLoadInterface {
	
	private SaveLoadInterface() {}
	
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
	
	public static Path getLocation(String worldname) {
		return GameCore.GAME_DIR.resolve("saves").resolve(worldname);
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
		long seed = Long.parseLong(b1+b2, 2);
		return createWorld(file, lockRef, seed);
	}
	public static WorldDataSet createWorld(Path file, RandomAccessFile lockRef, long seed) {
		
		try {
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
		}
		
		LevelCache[] levelCaches = new LevelCache[] {
			new LevelCache(0, new Point(0, 0), seed, IslandType.STARTER)/*,
			new LevelCache(1, new Point(2, 2), seed, IslandType.DESERT),
			new LevelCache(2, new Point(3, -5), seed, IslandType.ARCTIC),
			new LevelCache(3, new Point(-3, 5), seed, IslandType.JUNGLE),
			new LevelCache(4, new Point(10, 0), seed, IslandType.SWAMP)*/
		};
		
		WorldDataSet worldData = new WorldDataSet(file, lockRef, seed, 0, TimeOfDay.Morning.getStartOffsetSeconds(), GameCore.VERSION, new PlayerInfo[0], levelCaches);
		saveWorld(worldData);
		return worldData;
	}
	
	public static WorldDataSet loadWorld(Path folder, RandomAccessFile lockRef) {
		try {
			LinkedList<String> lines = new LinkedList<>();
			
			// read global data
			if(!readFile(folder.resolve("game.txt"), lines))
				return null;
			
			Version version = new Version(lines.pop());
			//noinspection MismatchedQueryAndUpdateOfCollection
			SerialDataMap map = new SerialDataMap(lines.pop());
			final long seed = Long.parseLong(map.get("seed"));
			final float gameTime = Float.parseFloat(map.get("gt"));
			final float daytime = Float.parseFloat(map.get("time"));
			
			// read terrain / entity / level data
			final String[] islandNames = MyUtils.parseLayeredString(map.get("islands"));
			LevelCache[] levels = new LevelCache[islandNames.length];
			for(int i = 0; i < levels.length; i++) {
				lines.clear();
				if(!readFile(folder.resolve(islandNames[i] + ".txt"), lines))
					return null;
				levels[i] = new LevelCache(version, lines);
			}
			
			// read player data
			lines.clear();
			if(!readFile(folder.resolve("players.txt"), lines))
				return null;
			PlayerInfo[] players = new PlayerInfo[lines.size() / 4];
			for(int i = 0; i < players.length; i++) {
				String name = lines.pop();
				String passhash = lines.pop();
				int level = Integer.parseInt(lines.pop());
				String data = lines.pop();
				players[i] = new PlayerInfo(name, passhash, data, level);
			}
			
			return new WorldDataSet(folder, lockRef, seed, gameTime, daytime, version, players, levels);
		} catch(NoSuchElementException e) {
			System.err.println("Save format of world '"+folder+"' is invalid.");
			e.printStackTrace();
			return null;
		}
	}
	
	static boolean saveWorld(WorldDataSet worldData) {
		Path main = worldData.worldFile;
		boolean good = true;
		String[] islandNames = new String[worldData.levelCaches.length];
		
		int i = 0;
		for(LevelCache level: worldData.levelCaches) {
			String name = "island-" + level.island.type.name().toLowerCase() + level.island.levelId;
			islandNames[i++] = name;
			good = writeFile(main.resolve(name+".txt"), level::serialize) && good;
		}
		
		good = writeFile(main.resolve("game.txt"), list -> {
			list.add(worldData.dataVersion.serialize());
			
			SerialDataMap map = new SerialDataMap();
			map.add("seed", worldData.seed);
			map.add("gt", (int)worldData.gameTime); // accuracy isn't worth the extra space
			map.add("time", (int)worldData.timeOfDay); // same here
			map.add("islands", MyUtils.encodeStringArray(islandNames));
			list.add(map.serialize());
			
		}) && good;
		
		good = writeFile(main.resolve("players.txt"), list -> {
			for(PlayerInfo p: worldData.playerInfo) {
				// every 4 is a new player; also the first one is generally regarded as the host
				list.add(p.name);
				list.add(p.passhash);
				list.add(String.valueOf(p.levelId));
				list.add(p.data);
			}
		}) && good;
		
		return good;
	}
	
	/** @noinspection BooleanMethodIsAlwaysInverted*/
	private static boolean readFile(Path path, LinkedList<String> list) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String s;
			while((s = reader.readLine()) != null)
				list.add(s);
			// actor.act(lines);
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static boolean writeFile(Path path, ValueFunction<LinkedList<String>> populator) {
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
	
	// contains a complete set of data needed to initialize a world (not suited to keep it running).
	public static class WorldDataSet {
		final Path worldFile;
		final RandomAccessFile lockRef;
		final long seed;
		final float gameTime;
		final float timeOfDay;
		final Version dataVersion;
		final PlayerInfo[] playerInfo;
		final LevelCache[] levelCaches;
		
		public WorldDataSet(Path worldFile, RandomAccessFile lockRef, long seed, float gameTime, float timeOfDay, Version dataVersion, PlayerInfo[] playerInfo, LevelCache[] levelCaches) {
			this.worldFile = worldFile;
			this.lockRef = lockRef;
			this.seed = seed;
			this.gameTime = gameTime;
			this.timeOfDay = timeOfDay;
			this.dataVersion = dataVersion;
			this.playerInfo = playerInfo;
			this.levelCaches = levelCaches;
		}
	}
	
	public static class PlayerInfo {
		final String name;
		final String passhash;
		final String data;
		final int levelId;
		
		PlayerInfo(String name, String passhash, String data, int levelId) {
			this.name = name;
			this.passhash = passhash;
			this.data = data;
			this.levelId = levelId;
		}
	}
	
	public static class LevelCache {
		
		/*
			There are two competing purposes for this class:
				- info sent to client about positioning and type of island
				- info used by server to generate a new island / load an existing one
			
			So, the server needs this type info... but also some extra, like seed, and entity/tile data.
			Client needs levelid, location, and levelConfig (which is the island type)
		 */
		
		final IslandReference island;
		
		// gen parameter
		private final long seed;
		
		// load parameters
		private Version dataVersion;
		private String[] entityData;
		private TileData[][] tileData;
		
		private LevelCache(int levelId, Point location, long seed, IslandType islandType) {
			this.island = new IslandReference(levelId, location, islandType);
			this.seed = seed;
			dataVersion = GameCore.VERSION;
		}
		
		private LevelCache(Version dataVersion, LinkedList<String> fileData) {
			//noinspection MismatchedQueryAndUpdateOfCollection
			SerialDataMap map = new SerialDataMap(fileData.pop());
			int id = Integer.parseInt(map.get("id"));
			int x = Integer.parseInt(map.get("x"));
			int y = Integer.parseInt(map.get("y"));
			String islandName = map.get("island");
			this.island = new IslandReference(id, new Point(x, y), IslandType.valueOf(islandName));
			seed = Long.parseLong(map.get("seed"));
			int ec = Integer.parseInt(map.get("ec"));
			int width = Integer.parseInt(map.get("w"));
			int height = Integer.parseInt(map.get("h"));
			
			if(ec < 0) {
				entityData = null;
				tileData = null;
				return;
			}
			
			this.entityData = new String[ec];
			this.tileData = new TileData[width][height];
			
			for(int i = 0; i < ec; i++)
				entityData[i] = fileData.pop();
			
			for(int xp = 0; xp < width; xp++)
				for(int yp = 0; yp < height; yp++)
					tileData[xp][yp] = new TileData(dataVersion, fileData.pop());
		}
		
		private List<String> serialize() { return serialize(new LinkedList<>()); }
		private List<String> serialize(LinkedList<String> data) {
			SerialDataMap map = new SerialDataMap();
			map.add("id", island.levelId);
			map.add("x", island.location.x);
			map.add("y", island.location.y);
			map.add("island", island.type.name());
			map.add("seed", seed);
			map.add("ec", entityData == null ? -1 : entityData.length);
			map.add("w", tileData == null ? 0 : tileData.length);
			map.add("h", tileData == null ? 0 : tileData[0].length);
			data.add(map.serialize());
			
			if(entityData != null)
				data.addAll(Arrays.asList(entityData));
			
			if(tileData != null)
				for(TileData[] col: tileData)
					for(TileData td: col)
						data.add(td.serialize());
			
			return data;
		}
		
		public void save(String[] entityData, TileData[][] tileData) {
			this.entityData = entityData;
			this.tileData = tileData;
			dataVersion = GameCore.VERSION;
		}
		
		public boolean generated() { return tileData != null; }
		
		public Level getLevel(LevelFetcher fetcher) {
			if(generated())
				return fetcher.loadLevel(dataVersion, island.levelId, tileData, entityData);
			
			return fetcher.makeLevel(island.levelId, seed, island.type);
		}
	}
}
