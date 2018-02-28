package miniventure.game;

import java.io.File;
import java.util.HashMap;

import miniventure.game.util.Version;
import miniventure.game.world.entity.mob.PursuePattern.FleePattern;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.kryo.Kryo;

public interface GameProtocol {
	
	int PORT = 8405;
	
	static void registerClasses(Kryo kryo) {
		kryo.register(Login.class);
		registerClassesInPackage(kryo, "miniventure.game.world", true);
		registerClassesInPackage(kryo, "miniventure.game.util", true);
		registerClassesInPackage(kryo, "miniventure.game.item", true);
		kryo.register(FleePattern.class);
		kryo.register(String.class);
		kryo.register(HashMap.class);
		kryo.register(TextureRegion.class);
		kryo.register(Animation.class);
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
		
	}
	
	// level and player are already existing types
	
	class Move {
		// entity movement... though maybe RMI again!
	}
	
	class Request {
		// interact/attack request... or maybe I can do this through RMI.
	}
}
