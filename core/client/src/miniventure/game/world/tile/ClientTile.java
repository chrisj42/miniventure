package miniventure.game.world.tile;

import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	@NotNull private final ClientLevel level;
	
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, SerialMap[] data) {
		super(level, x, y, types, data);
		this.level = level;
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return level; }
	
	@Override public boolean addTile(@NotNull TileType newType) { return false; }
	@Override boolean breakTile() { return false; }
	@Override boolean breakTile(boolean checkForExitAnim) { return false; }
	@Override boolean replaceTile(@NotNull TileType newType) { return false; }
}
