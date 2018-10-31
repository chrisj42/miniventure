package miniventure.game.item;

import miniventure.game.world.entity.Entity;

/**
 * An item that, when placed in the world, becomes an entity.
 * This is similar to TileItems, which become tiles when placed.
 */
public class EntityItem extends ServerItem {
	
	public EntityItem(String entityData) {
		super(ItemType)
	}
	
	@Override
	public String[] save() {
		return new String[0];
	}
	
	@Override
	public EntityItem copy() {
		return null;
	}
}
