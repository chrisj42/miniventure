package miniventure.game.util.customenum;

class EnumInitializationException extends RuntimeException {
	
	EnumInitializationException(Class<? extends GenericEnum> clazz, Throwable cause) {
		super("Error initializing names and constant data for generic enum class "+clazz, cause);
	}
	
}
