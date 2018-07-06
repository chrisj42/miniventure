package miniventure.game.world.tile;

import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.PropertyTag;

import org.jetbrains.annotations.NotNull;

public class WallTile extends TileType {
	WallTile(@NotNull TileTypeEnum enumType, int health, PreferredTool prefTool) {
		super(enumType, false,
			new DataMap(PropertyTag.ZOffset.as(0.4f)),
			new DestructionManager(enumType, health, prefTool),
			new TileTypeRenderer(enumType, true)
		);
	}
}
