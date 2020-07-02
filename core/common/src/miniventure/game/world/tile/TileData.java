package miniventure.game.world.tile;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;

public class TileData {
	// public final TileTypeEnum groundType;
	// public final TileTypeEnum decorationType;
	// public final String groundData;
	// public final TileTypeEnum surfaceType;
	// public final String surfaceData;
	public final TileTypeEnum[] types;
	
	public static final TileData EMPTY = new TileData(TileTypeEnum.DIRT);
	
	private TileData() { this((TileTypeEnum[])null); }
	private TileData(TileTypeEnum... types) {
		this.types = types;
	}
	public TileData(Tile tile, boolean save) {
		types = new TileTypeEnum[TileLayer.values.length];
		tile.getTypeStack().forEach((layer, type) -> types[layer.ordinal()] = type.getTypeEnum());
	}
	public TileData(Version dataVersion, String allData) {
		String[] data = MyUtils.parseLayeredString(allData);
		types = ArrayUtils.mapArray(data, TileTypeEnum.class, str -> str.length() == 0 ? null : TileTypeEnum.valueOf(str));
	}
	
	public String serialize() {
		return MyUtils.encodeStringArray(ArrayUtils.mapArray(types, String.class, type -> type == null ? "" : type.name()));
	}
}
