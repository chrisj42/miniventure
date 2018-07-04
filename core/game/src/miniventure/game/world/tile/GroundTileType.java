package miniventure.game.world.tile;

import miniventure.game.world.tile.DestructionManager;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeRenderer;
import miniventure.game.world.tile.UpdateManager;

import org.jetbrains.annotations.NotNull;

public class GroundTileType extends TileType {
	GroundTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer) {
		super(enumType, walkable, destructionManager, renderer);
	}
	
	GroundTileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, destructionManager, renderer, updateManager);
	}
	
	GroundTileType(@NotNull TileTypeEnum enumType, boolean walkable, float lightRadius, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		super(enumType, walkable, lightRadius, destructionManager, renderer, updateManager);
	}
}
