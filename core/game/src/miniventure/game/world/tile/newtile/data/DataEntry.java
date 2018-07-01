package miniventure.game.world.tile.newtile.data;

public class DataEntry<T> {
	
	public final DataTag<T> key;
	public final T value;
	
	public DataEntry(DataTag<T> dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
}
