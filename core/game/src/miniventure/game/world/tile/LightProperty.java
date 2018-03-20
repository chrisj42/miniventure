package miniventure.game.world.tile;

@FunctionalInterface
interface LightProperty extends TilePropertyInstance {
	
	float getLightRadius();
	
}
