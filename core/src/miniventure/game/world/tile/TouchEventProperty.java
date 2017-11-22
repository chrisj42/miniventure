package miniventure.game.world.tile;

import miniventure.game.world.entity.Entity;

// FIXME Darn it! This isn't how it's supposed to work... this is an *event* class; it ought to be a functional interface. But, I don't know how to 

public class TouchEventProperty implements TileProperty {
	
	public static final TouchEventProperty SOLID = new TouchEventProperty() {
		@Override public boolean touchedBy(Entity entity) { return false; }
	};
	
	public TouchEventProperty() {}
	
	/**
	 * Called when an entity is attempting to move onto the tile; one may do any other things to the entity here, like hurt it or apply knockback.
	 * 
	 * @param entity the entity that touched the tile.
	 * @return whether the entity is allowed to step onto the tile.
	 */
	public boolean touchedBy(Entity entity) {
		return true;
	}
	
	@Override
	public int getDataCount() { return 0; }
	
	@Override
	public int[] getData() {
		return new int[0];
	}
}
