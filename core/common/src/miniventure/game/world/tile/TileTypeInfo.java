package miniventure.game.world.tile;

import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;

// used to package a tiletype with some initial data.
public class TileTypeInfo {
	
	@NotNull
	public final TileTypeEnum typeEnum;
	private final TileTypeDataMap data;
	
	public TileTypeInfo(@NotNull WorldManager world, @NotNull TileTypeEnum tileType) {
		this(world.getTileType(tileType));
	}
	public TileTypeInfo(@NotNull TileType tileType) {
		this.typeEnum = tileType.getTypeEnum();
		data = tileType.createDataMap();
	}
	public TileTypeInfo(@NotNull TileType tileType, @NotNull TileTypeDataMap dataMap) {
		this(tileType.getTypeEnum(), dataMap);
	}
	public TileTypeInfo(@NotNull TileTypeEnum tileType, @NotNull TileTypeDataMap dataMap) {
		this.typeEnum = tileType;
		this.data = dataMap;
	}
	
	@NotNull
	public TileTypeDataMap getData() {
		return data;
	}
}
