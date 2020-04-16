package miniventure.game.util.customenum;

public class DataEntry<T, ET extends GenericEnum<T, ?>> {
	
	final ET key;
	final Object value;
	
	<T, E extends GenericEnum<T, ET>> DataEntry(ET dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
}
