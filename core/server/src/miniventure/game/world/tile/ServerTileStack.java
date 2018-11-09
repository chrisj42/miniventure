package miniventure.game.world.tile;

import miniventure.game.server.ServerCore;

public class ServerTileStack extends TileStack<ServerTileType> {
	
	public ServerTileStack() {
		super(ServerCore.getWorld());
	}
	
	public ServerTileStack(ServerTileType[] types) {
		super(types);
	}
	
	public ServerTileStack(TileTypeEnum[] enumTypes) {
		super(ServerCore.getWorld(), enumTypes);
	}
	
}
