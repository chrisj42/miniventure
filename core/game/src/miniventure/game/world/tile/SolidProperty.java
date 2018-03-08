package miniventure.game.world.tile;

import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface SolidProperty extends TileProperty {
	
	SolidProperty SOLID = (e) -> false;
	SolidProperty WALKABLE = (e) -> true;
	
	boolean isPermeableBy(Entity entity);
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return SolidProperty.class; }
}
