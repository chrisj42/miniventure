package miniventure.game.world.tile;

import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.ClientLevel;

import org.jetbrains.annotations.NotNull;

public class ClientTile extends RenderTile {
	
	public ClientTile(@NotNull ClientLevel level, int x, int y, @NotNull TileTypeEnum[] types, SerialMap[] data) {
		super(level, x, y, types, data);
	}
	
	@NotNull @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	public void apply(TileData tileData) {
		TileTypeEnum[] types = tileData.getTypes();
		SerialMap[] maps = tileData.getDataMaps();
		
		setTileStack(makeStack(types));
		dataMaps.clear();
		for(int i = 0; i < tileData.data.length; i++)
			dataMaps.put(types[i], maps[i]);
	}
}
