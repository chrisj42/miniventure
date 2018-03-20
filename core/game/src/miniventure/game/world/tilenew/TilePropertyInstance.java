package miniventure.game.world.tilenew;

import miniventure.game.util.property.Property;

import org.jetbrains.annotations.NotNull;

public interface TilePropertyInstance extends Property {
	
	default String[] getInitialData() { return new String[0]; }
	
}
