package miniventure.game.world.tile;

@FunctionalInterface
interface LightProperty extends TileProperty {
	float getLightRadius();
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return LightProperty.class; }
}
