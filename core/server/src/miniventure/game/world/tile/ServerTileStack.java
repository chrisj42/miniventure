package miniventure.game.world.tile;

import miniventure.game.server.ServerCore;

public class ServerTileStack extends TileStack<ServerTileType> {
	
	public ServerTileStack() {
		super(ServerTileType.class, ServerCore.getWorld());
	}
	
	public ServerTileStack(ServerTileType[] types) {
		super(ServerTileType.class, types);
	}
	
	public ServerTileStack(TileTypeEnum[] enumTypes) {
		super(ServerTileType.class, ServerCore.getWorld(), enumTypes);
	}
	
}
