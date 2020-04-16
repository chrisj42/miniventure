package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public interface ServerTileTypeInterface extends TileTypeInterface {
	
	// void tileDestroyed(@NotNull Tile.TileContext context);
	// Result tileAttacked(@NotNull Tile.TileContext context, @NotNull WorldObject attacker, @Nullable ServerItem item, int damage);
	DestructionManager pDestruct();
	
	float update(@NotNull Tile.TileContext context, float delta);
	
	
	
}
