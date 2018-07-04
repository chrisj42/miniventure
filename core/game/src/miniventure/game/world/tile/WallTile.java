package miniventure.game.world.tile;

import miniventure.game.world.tile.DestructionManager.PreferredTool;

import org.jetbrains.annotations.NotNull;

public class WallTile extends SurfaceTileType {
	WallTile(@NotNull TileTypeEnum enumType, int health, PreferredTool prefTool) {
		super(enumType, false, new DestructionManager(enumType, health, prefTool), new TileTypeRenderer(enumType, true));
	}
}
