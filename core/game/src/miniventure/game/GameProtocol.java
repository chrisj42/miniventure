package miniventure.game;

import java.io.File;

import miniventure.game.util.Version;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.tile.Tile.TileData;

import com.esotericsoftware.kryo.Kryo;

public interface GameProtocol {
	
	int PORT = 8405;
	
	static void registerClasses(Kryo kryo) {
		Class<?>[] classes = GameProtocol.class.getDeclaredClasses();
		for(Class<?> clazz: classes)
			kryo.register(clazz);
		
		kryo.register(TileData.class);
		kryo.register(TileData[].class);
		kryo.register(TileData[][].class);
		kryo.register(ChunkData.class);
		kryo.register(ChunkData[].class);
		kryo.register(Version.class);
		
		kryo.register(String[].class);
		kryo.register(int[].class);
	}
	
	enum DatalessRequest {
		Respawn
	}
	
	class Login {
		public final String username;
		public final Version version;
		
		public Login() { this(null, null); }
		public Login(String username, Version version) {
			this.username = username;
			this.version = version;
		}
	}
	
	class LevelData {
		public final int width;
		public final int height;
		public final ChunkData[] chunkData;
		
		public LevelData(int width, int height, ChunkData[] data) {
			this.width = width;
			this.height = height;
			chunkData = data;
		}
	}
	
	class SpawnData {
		public final float x, y;
		
		public SpawnData(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
	
	// level and player are already existing types
	
	class Move {
		// entity movement... though maybe RMI again!
	}
	
	class Request {
		// interact/attack request... or maybe I can do this through RMI.
	}
	
	
	static void registerClassesInPackage(Kryo kryo, String packageName, boolean recursive) {
		String sep = File.separator;
		String path = new File("").getAbsolutePath() + sep + ".."+sep+"src"+sep;
		path += packageName.replaceAll("\\.", sep);
		registerClassesInPackage(kryo, packageName, new File(path), recursive); // should now point to the folder.
	}
	static void registerClassesInPackage(Kryo kryo, String packageName, File parent, boolean recursive) {
		File[] classes = parent.listFiles((dir, name) -> name.endsWith(".java"));
		File[] subpackages = parent.listFiles(File::isDirectory);
		if(classes == null)
			return;
		
		for(File file: classes) {
			String className = file.getName();
			className = className.substring(0, className.indexOf("."));
			try {
				Class<?> clazz = Class.forName(packageName+"."+className);
				kryo.register(clazz);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if(recursive && subpackages != null)
			for(File subpack: subpackages)
				registerClassesInPackage(kryo, packageName+"."+subpack.getName(), subpack, true);
	}
}
