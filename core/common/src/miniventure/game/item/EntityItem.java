package miniventure.game.item;

import miniventure.game.world.entity.Entity;

/**
 * An item that, when placed in the world, becomes an entity.
 * This is similar to TileItems, which become tiles when placed.
 */
public class EntityItem extends Item {
	
	public EntityItem(String entityData) {
		// FIXME this is just to make it compile
		super(ItemType.Misc, "Entity");
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
