package miniventure.game.world.tilenew;

import miniventure.game.world.entity.Entity;


@FunctionalInterface
public interface SolidProperty extends TilePropertyInstance {
	
	SolidProperty SOLID = (e) -> false;
	SolidProperty WALKABLE = (e) -> true;
	
	boolean isPermeableBy(Entity entity);
	
}
