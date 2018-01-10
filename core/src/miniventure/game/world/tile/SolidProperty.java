package miniventure.game.world.tile;

import miniventure.game.world.entity.Entity;

@FunctionalInterface
public interface SolidProperty extends TileProperty {
	
	SolidProperty SOLID = (e) -> false;
	SolidProperty WALKABLE = (e) -> true;
	
	boolean isPermeableBy(Entity entity);
	
	@Override
	default Integer[] getInitData() { return new Integer[0]; }
}
