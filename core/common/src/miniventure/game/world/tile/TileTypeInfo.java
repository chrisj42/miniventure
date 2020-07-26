package miniventure.game.world.tile;

import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// used to package a tiletype with some initial data.
public class TileTypeInfo {
	
	@NotNull
	public final TileTypeEnum tileType;
	
	private final TileTypeDataMap initialData;
	
	public TileTypeInfo(@NotNull TileType tileType) { this(tileType, null); }
	public TileTypeInfo(@NotNull TileType tileType, @Nullable TileTypeDataMap initialData) {
		this(tileType.getTypeEnum(), initialData);
	}
	public TileTypeInfo(@NotNull TileTypeEnum tileType) { this(tileType, null); }
	public TileTypeInfo(@NotNull TileTypeEnum tileType, @Nullable TileTypeDataMap initialData) {
		this.tileType = tileType;
		this.initialData = initialData;
	}
	
	@NotNull
	public TileTypeDataMap getInitialData(WorldManager world) {
		return initialData == null ? world.getTileType(tileType).createDataMap() : initialData;
	}
}
