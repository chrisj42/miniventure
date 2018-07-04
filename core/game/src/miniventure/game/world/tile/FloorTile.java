package miniventure.game.world.tile;

import miniventure.game.item.ToolType;
import miniventure.game.world.tile.DestructionManager.RequiredTool;

import org.jetbrains.annotations.NotNull;

public class FloorTile extends GroundTileType {
	FloorTile(@NotNull TileTypeEnum enumType, ToolType toolType) {
		super(enumType, true, new DestructionManager(enumType, new RequiredTool(toolType)), new TileTypeRenderer(enumType, true, new ConnectionManager(enumType, RenderStyle.SINGLE_FRAME, enumType)));
	}
}
