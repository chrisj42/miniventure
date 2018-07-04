package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public class LiquidTileType extends GroundTileType {
	LiquidTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer) {
		super(enumType, walkable, destructionManager, renderer);
	}
	
	LiquidTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, destructionManager, renderer, updateManager);
	}
	
	LiquidTileType(@NotNull TileTypeEnum enumType, boolean walkable, float lightRadius, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, lightRadius, destructionManager, renderer, updateManager);
	}
}
