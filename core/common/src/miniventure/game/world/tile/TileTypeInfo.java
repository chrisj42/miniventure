package miniventure.game.world.tile;

import miniventure.game.world.tile.TileDataTag.TileDataMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// used to package a tiletype with some initial data.
public class TileTypeInfo {
	
	@NotNull
	public final TileTypeEnum tileType;
	@NotNull
	public final TileDataMap initialData;
	
	public TileTypeInfo(@NotNull TileType tileType) { this(tileType, null); }
	public TileTypeInfo(@NotNull TileType tileType, @Nullable TileDataMap initialData) {
		this(tileType.getTypeEnum(), initialData);
	}
	public TileTypeInfo(@NotNull TileTypeEnum tileType) { this(tileType, null); }
	public TileTypeInfo(@NotNull TileTypeEnum tileType, @Nullable TileDataMap initialData) {
		this.tileType = tileType;
		this.initialData = initialData == null ? new TileDataMap() : initialData;
	}
}
