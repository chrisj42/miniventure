package miniventure.game.world.tile;

import miniventure.game.world.tile.TileDataTag.TileDataEnumMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// used to package a tiletype with some initial data.
public class TileTypeInfo {
	
	@NotNull
	public final TileTypeEnum tileType;
	@NotNull
	public final TileDataTag.TileDataEnumMap initialData;
	
	public TileTypeInfo(@NotNull TileType tileType) { this(tileType, null); }
	public TileTypeInfo(@NotNull TileType tileType, @Nullable TileDataTag.TileDataEnumMap initialData) {
		this(tileType.getTypeEnum(), initialData);
	}
	public TileTypeInfo(@NotNull TileTypeEnum tileType) { this(tileType, null); }
	public TileTypeInfo(@NotNull TileTypeEnum tileType, @Nullable TileDataTag.TileDataEnumMap initialData) {
		this.tileType = tileType;
		this.initialData = initialData == null ? new TileDataEnumMap() : initialData;
	}
}
