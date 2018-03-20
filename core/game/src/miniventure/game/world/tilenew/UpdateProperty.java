package miniventure.game.world.tilenew;


@FunctionalInterface
public interface UpdateProperty extends TilePropertyInstance {
	
	void update(float delta, Tile tile);
	
}
