package miniventure.game.world.tile.newtile.data;

public class DataEntry<T> {
	
	public final DataTag<T> key;
	public final T value;
	
	public DataEntry(DataTag<T> dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
	
	public String serialize() {
		return key.name()+','+key.serialize(value);
	}
	
	public static DataEntry<?> deserialize(String data) {
		String key = data.substring(0, data.indexOf(','));
		String value = data.substring(data.indexOf(',')+1);
		
		DataTag<?> tag = DataTag.valueOf(key);
		
		return tag.asSerial(value);
	}
}
