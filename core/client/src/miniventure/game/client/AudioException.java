package miniventure.game.client;

public class AudioException extends Exception {
	
	public AudioException() {
		super();
	}
	
	public AudioException(String message) {
		super(message);
	}
	
	public AudioException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AudioException(Throwable cause) {
		super(cause);
	}
	
	protected AudioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
