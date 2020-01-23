package miniventure.game.util.customenum;

class MissingEnumDataException extends RuntimeException {
	
	MissingEnumDataException(Class<? extends GenericEnum> clazz) {
		super("Generic Enum Class "+clazz+" does not have enum data.");
	}
	
}
