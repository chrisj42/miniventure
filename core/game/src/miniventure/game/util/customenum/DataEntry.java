package miniventure.game.util.customenum;

class DataEntry<T, D extends DataEnum<T>> {
	
	final D key;
	final T value;
	
	DataEntry(D dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
}
