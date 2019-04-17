package miniventure.game.world.file;

public class WorldFormatException extends Exception {
	
	public WorldFormatException(/*Path worldFolder, */String message) {
		super(message);
	}
	
	public WorldFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
