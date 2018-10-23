package miniventure.game.item;

import miniventure.game.world.entity.Entity;

/**
 * An item that, when placed in the world, becomes an entity.
 * This is similar to TileItems, which become tiles when placed.
 */
public class EntityItem extends Item {
	
	public EntityItem(Entity e) {
		super(ItemType.Entity, e.getClass().getSimpleName());
	}
	
	@Override
	public String[] save() {
		return new String[0];
	}
	
	@Override
	public Item copy() {
		return null;
	}
}
