package miniventure.game.util.customenum;

public class DataEntry<T> {
	
	final DataEnum<T> key;
	final T value;
	
	public DataEntry(DataEnum<T> dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
}
