package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public class LiquidTileType extends GroundTileType {
	LiquidTileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer) {
		super(enumType, destructionManager, renderer);
	}
	
	LiquidTileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, destructionManager, renderer, updateManager);
	}
}
