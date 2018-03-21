package miniventure.game.world.entitynew;

import miniventure.game.util.property.Property;

public interface EntityProperty extends Property {
	
	default String[] save() { return new String[0]; }
	default void load(String[] data) {}
	
}
