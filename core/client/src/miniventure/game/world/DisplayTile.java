package miniventure.game.world;

import miniventure.game.world.tile.RenderTile;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

class DisplayTile extends RenderTile {
	
	DisplayTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types) {
		this((DisplayLevel)level, x, y, types);
	}
	DisplayTile(@NotNull DisplayLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
		super(level, x, y, types, null);
	}
	
}
