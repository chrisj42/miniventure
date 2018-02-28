package miniventure.game.world.tile;

@FunctionalInterface
public interface UpdateProperty extends TileProperty {
	
	void update(float delta, Tile tile);
	
}
