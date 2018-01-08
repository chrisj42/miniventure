package miniventure.game.world.tile;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ItemEntity;

@FunctionalInterface
public interface SolidProperty extends TileProperty {
	
	SolidProperty SOLID = (e) -> e instanceof ItemEntity;
	SolidProperty WALKABLE = (e) -> true;
	
	boolean isPermeableBy(Entity entity);
	
	@Override
	default Integer[] getInitData() { return new Integer[0]; }
}
