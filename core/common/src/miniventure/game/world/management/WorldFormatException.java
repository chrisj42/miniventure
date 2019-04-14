package miniventure.game.world.management;

import java.nio.file.Path;

public class WorldFormatException extends Exception {
	
	public WorldFormatException(/*Path worldFolder, */String message) {
		super(message);
	}
	
	public WorldFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
