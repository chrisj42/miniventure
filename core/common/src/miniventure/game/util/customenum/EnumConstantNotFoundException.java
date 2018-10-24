package miniventure.game.util.customenum;

class EnumConstantNotFoundException extends RuntimeException {
	
	EnumConstantNotFoundException(Class<? extends GenericEnum> clazz, String name) {
		super("constant '"+name+"' not found in generic enum "+clazz);
	}
	
}
