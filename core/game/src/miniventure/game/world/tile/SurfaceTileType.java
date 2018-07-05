package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public class SurfaceTileType extends TileType {
	SurfaceTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer) {
		super(enumType, walkable, destructionManager, renderer);
	}
	
	SurfaceTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, destructionManager, renderer, updateManager);
	}
	
	SurfaceTileType(@NotNull TileTypeEnum enumType, boolean walkable, float lightRadius, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, lightRadius, destructionManager, renderer, updateManager);
	}
}
