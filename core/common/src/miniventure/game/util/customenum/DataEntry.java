package miniventure.game.util.customenum;

public class DataEntry<T, ET extends GenericEnum<T, ET>> {
	
	final ET key;
	final T value;
	
	DataEntry(ET dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
}
