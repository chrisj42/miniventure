package miniventure.game.world.tile;

import miniventure.game.world.tile.TileDataTag.TileDataEnumMap;
import miniventure.game.world.management.ServerWorld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerTileStack extends TileStack<ServerTileType> {
	
	/*public ServerTileStack(@NotNull ServerWorld world, ServerTileType[] types) {
		super(types);
	}*/
	
	public ServerTileStack(@NotNull ServerWorld world, TileTypeEnum[] enumTypes, @Nullable TileDataEnumMap[] dataMaps) {
		super(world, enumTypes, dataMaps);
	}
	
}
