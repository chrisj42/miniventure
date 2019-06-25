package miniventure.game.world.tile;

import miniventure.game.util.customenum.SerialEntry;
import miniventure.game.util.customenum.SerialMap;

import org.jetbrains.annotations.NotNull;

// used to package a tiletype with some initial data.
public class TileTypeInfo {
	
	@NotNull
	public final TileTypeEnum tileType;
	@NotNull
	public final SerialMap initialData;
	
	public TileTypeInfo(@NotNull TileType tileType, @NotNull SerialEntry... initialData) {
		this(tileType.getTypeEnum(), initialData);
	}
	public TileTypeInfo(@NotNull TileTypeEnum tileType, @NotNull SerialEntry... initialData) {
		this.tileType = tileType;
		this.initialData = new SerialMap(initialData);
	}
}
