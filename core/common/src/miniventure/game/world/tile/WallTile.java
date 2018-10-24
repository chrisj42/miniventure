package miniventure.game.world.tile;

import miniventure.game.util.customenum.PropertyMap;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.data.TilePropertyTag;

import org.jetbrains.annotations.NotNull;

public class WallTile extends TileType {
	WallTile(@NotNull TileTypeEnum enumType, int health, PreferredTool prefTool) {
		super(enumType, false,
			new PropertyMap(TilePropertyTag.ZOffset.as(0.4f)),
			new DestructionManager(enumType, health, prefTool),
			new TileTypeRenderer(enumType, false)
		);
	}
}
