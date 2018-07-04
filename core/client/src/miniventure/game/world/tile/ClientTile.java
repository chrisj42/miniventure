package miniventure.game.world.tile;

import miniventure.game.world.ClientLevel;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	@NotNull private final ClientLevel level;
	
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, DataMap[] data) {
		super(level, x, y, types, data);
		this.level = level;
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return level; }
}
