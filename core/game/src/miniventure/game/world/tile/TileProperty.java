package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public abstract class TileProperty {
	
	protected final TileType tileType;
	
	TileProperty(@NotNull TileType tileType) {
		this.tileType = tileType;
	}
	
	public String[] getInitialData() { return new String[0]; }
	
	// these methods are used to do and transformations on the tile data that is needed to save it to file, or load it from file (or to/from serialized form)
	//default void configureDataForLoad(Tile tile) {}
	//default void configureDataForSave(Tile tile) {}
}
