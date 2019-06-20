package miniventure.game.world.tile;

import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.TileStack.TileData;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	public ClientTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @NotNull SerialMap[] data) {
		this((ClientLevel)level, x, y, types, data);
	}
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, @NotNull SerialMap[] data) {
		super(level, x, y, types, data);
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	public void apply(TileData tileData) {
		TileTypeEnum[] types = tileData.getTypes();
		SerialMap[] maps = tileData.getDataMaps();
		
		setTileStack(makeStack(types, maps));
	}
}
