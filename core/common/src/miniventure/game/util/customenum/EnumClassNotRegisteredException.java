package miniventure.game.util.customenum;

class EnumClassNotRegisteredException extends RuntimeException {
	
	EnumClassNotRegisteredException(Class<? extends GenericEnum> clazz) {
		super("Generic Enum Class "+clazz+" was not registered prior to field initialization, or had ; ensure a static call to GenericEnum.registerEnum(Class clazz, int constants) is made above the static enum fields.");
	}
	
	EnumClassNotRegisteredException(Class<? extends GenericEnum> clazz, Throwable cause) {
		super("Generic Enum Class "+clazz+" was not registered prior to field initialization, or had ; ensure a static call to GenericEnum.registerEnum(Class clazz, int constants) is made above the static enum fields.", cause);
	}
}
