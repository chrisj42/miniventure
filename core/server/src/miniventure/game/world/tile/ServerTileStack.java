package miniventure.game.world.tile;

import miniventure.game.world.management.ServerWorld;

import org.jetbrains.annotations.NotNull;

public class ServerTileStack extends TileStack<ServerTileType> {
	
	public ServerTileStack(@NotNull ServerWorld world) {
		super(world);
	}
	
	public ServerTileStack(@NotNull ServerWorld world, ServerTileType[] types) {
		super(types);
	}
	
	public ServerTileStack(@NotNull ServerWorld world, TileTypeEnum[] enumTypes) {
		super(world, enumTypes);
	}
	
}
