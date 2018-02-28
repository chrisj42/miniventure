package miniventure.game.world.tile;

@FunctionalInterface
interface LightProperty extends TileProperty {
	float getLightRadius();
}
