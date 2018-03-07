package miniventure.game.world.tile;

@FunctionalInterface
public interface UpdateProperty extends TileProperty {
	
	void update(float delta, Tile tile);
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return UpdateProperty.class; }
}
