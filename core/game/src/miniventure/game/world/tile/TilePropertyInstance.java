package miniventure.game.world.tile;

import miniventure.game.util.property.Property;

public interface TilePropertyInstance extends Property {
	
	default String[] getInitialData() { return new String[0]; }
	
}
