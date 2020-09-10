package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;

import org.jetbrains.annotations.Nullable;

public class TileTypeDataMap {
	
	// holds tile type data, data associated with a single tile type.
	// the data array is optimized so that it is no bigger than needed for the data that will be used
	
	// TODO pool this object; it's going to be allocated and removed a lot
	
	// FIXME these client-side maps have no good way of clearing data associations from tiles when the types are updated. Currently, the only used data is animation time and that sorta works for now. This should be better when stacks are reduced to 1 type per layer.
	
	private final TileTypeDataOrder dataOrder;
	private final Object[] data;
	
	TileTypeDataMap(TileTypeDataOrder dataOrder) {
		this.dataOrder = dataOrder;
		data = dataOrder.createDataArray();
	}
	
	TileTypeDataMap(TileTypeDataOrder dataOrder, String allData, @Nullable Version versionIfFile) {
		this(dataOrder);
		final boolean fromFile = versionIfFile != null;
		String[] dataAr = MyUtils.parseLayeredString(allData);
		for(int i = 0; i < dataAr.length; i++) {
			TileDataTag<?> tag = dataOrder.getTypeAt(i, fromFile);
			String data = dataAr[dataOrder.getDataIndex(tag, fromFile)];
			this.data[dataOrder.getDataIndex(tag)] = tag.deserialize(data, versionIfFile);
		}
	}
	
	public TileType getTileType() { return dataOrder.getTileType(); }
	public TileTypeEnum getTileTypeEnum() { return dataOrder.getTileType().getTypeEnum(); }
	
	// this doesn't need to know tile type because the enum type stack is saved parallel to this, so the type can still be determined and passed back to the constructor without having to provide anything but the ordered list of data here.
	// 
	public String serialize(boolean save) {
		String[] serial = new String[dataOrder.getDataSize(save)];
		for(int i = 0; i < serial.length; i++) {
			TileDataTag<?> tag = dataOrder.getTypeAt(i, save);
			serial[i] = tag.serializeCast(data[dataOrder.getDataIndex(tag)]);
		}
		return MyUtils.encodeStringArray(serial);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(TileDataTag<T> tag) {
		return (T) data[dataOrder.getDataIndex(tag)];
	}
	
	public <T> T getOrDefault(TileDataTag<T> tag, T defaultValue) {
		T value = get(tag);
		return value == null ? defaultValue : value;
	}
	
	public <T> T getOrDefaultAndPut(TileDataTag<T> tag, T defaultValue) {
		T value = get(tag);
		if(value == null) {
			put(tag, defaultValue);
			return defaultValue;
		}
		return value;
	}
	
	// returns previous value
	@Nullable
	public <T> T remove(TileDataTag<T> tag) {
		return put(tag, null);
	}
	
	// returns previous value
	@Nullable
	public <T> T put(TileDataTag<T> tag, T value) {
		T old = get(tag);
		data[dataOrder.getDataIndex(tag)] = value;
		return old;
	}
}
