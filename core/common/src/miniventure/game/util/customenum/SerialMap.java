package miniventure.game.util.customenum;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileDataTag;

@SuppressWarnings("unchecked")
public class SerialMap extends SerialEnumMap<TileDataTag<?>> {
	public SerialMap() {
		super();
	}
	
	public SerialMap(DataEntry<?, ? extends TileDataTag<?>>... entries) {
		super(entries);
	}
	
	public SerialMap(GEnumMap<TileDataTag<?>> model) {
		super(model);
	}
	
	public static <ET extends TileDataTag<?>> SerialMap deserialize(String alldata, Class<TileDataTag> tagClass) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		SerialMap map = new SerialMap();
		
		for(String item: data) {
			String[] parts = item.split("=", 2);
			TileDataTag<?> tag = TileDataTag.valueOf(parts[0]);
			deserializeTag(tag, parts[1], map);
		}
		
		return map;
	}
	
	private static <T> void deserializeTag(TileDataTag<T> tag, String data, SerialMap map) {
		map.put(tag, tag.deserialize(data));
	}
}
