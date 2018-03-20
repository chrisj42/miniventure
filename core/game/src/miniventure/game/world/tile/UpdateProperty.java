package miniventure.game.world.tile;

import miniventure.game.world.tilenew.Tile;

@FunctionalInterface
public interface UpdateProperty extends TileProperty {
	
	void update(float delta, Tile tile);
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return UpdateProperty.class; }
}
