package miniventure.game.world.tile;

import miniventure.game.world.entity.Entity;

@FunctionalInterface
public interface TouchListener extends TileProperty {
	
	//TouchListener DO_NOTHING = entity -> {};
	
	void touchedBy(Entity entity, Tile tile);
	//void stillTouchedBy(Entity entity);
	//void steppedOff(Entity entity); // idk, this will be much later.
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return TouchListener.class; }
}
