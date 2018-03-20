package miniventure.game.world.tile;


@FunctionalInterface
public interface UpdateProperty extends TilePropertyInstance {
	
	void update(float delta, Tile tile);
	
}
