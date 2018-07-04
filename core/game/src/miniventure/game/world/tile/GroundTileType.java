package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public class GroundTileType extends TileType {
	GroundTileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer) {
		super(enumType, destructionManager, renderer);
	}
	
	GroundTileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, destructionManager, renderer, updateManager);
	}
}
