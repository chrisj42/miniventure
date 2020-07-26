package miniventure.game.util.customenum;

public class SerialEntry<T, ET extends SerialEnum<T, ET>> {
	
	final ET key;
	final T value;
	
	SerialEntry(ET dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
}
