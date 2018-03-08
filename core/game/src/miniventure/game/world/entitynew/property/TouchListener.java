package miniventure.game.world.entitynew.property;

import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface TouchListener extends EntityProperty {
	// returns if the other object should have the touch method called.
	boolean entityTouched(Entity e, Entity toucher, boolean initial);
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return TouchListener.class; }
}
