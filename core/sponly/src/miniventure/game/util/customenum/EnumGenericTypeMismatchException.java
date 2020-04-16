package miniventure.game.util.customenum;

class EnumGenericTypeMismatchException extends RuntimeException {
	
	EnumGenericTypeMismatchException(GenericEnum instance) {
		super("Error when casting generic enum instance "+instance+", likely cause is generic type of generic enum subclass; ensure that first generic type is itself.");
	}
	
	EnumGenericTypeMismatchException(GenericEnum instance, Throwable cause) {
		super("Error when casting generic enum instance "+instance+", likely cause is generic type of generic enum subclass; ensure that first generic type is itself.", cause);
	}
}
