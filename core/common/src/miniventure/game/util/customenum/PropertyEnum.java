package miniventure.game.util.customenum;

public abstract class PropertyEnum<T, E extends PropertyEnum<T, E>> extends GenericEnum<T, E> {
	
	private T defaultValue;
	
	protected PropertyEnum(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public static <T extends PropertyEnum
}
