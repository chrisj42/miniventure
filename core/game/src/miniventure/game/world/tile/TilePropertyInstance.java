package miniventure.game.world.tile;

import miniventure.game.util.property.Property;

public interface TilePropertyInstance extends Property {
	
	default String[] getInitialData() { return new String[0]; }
	
	// these methods are used to do and transformations on the tile data that is needed to save it to file, or load it from file (or to/from serialized form)
	//default void configureDataForLoad(Tile tile) {}
	//default void configureDataForSave(Tile tile) {}
}
