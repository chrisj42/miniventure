package miniventure.game.world.tile;

import miniventure.game.GameProtocol.TileUpdate;
import miniventure.game.server.ServerCore;
import miniventure.game.world.ServerLevel;

import org.jetbrains.annotations.NotNull;

public class ServerTile extends Tile {
	
	private final ServerLevel level;
	
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileType[] types, @NotNull String[] data) {
		super(level, x, y, types, data);
		this.level = level;
	}
	
	
	@NotNull @Override
	public ServerLevel getLevel() { return level; }
	
	@Override
	public boolean addTile(@NotNull TileType newType) {
		boolean success = super.addTile(newType);
		if(success)
			ServerCore.getServer().broadcast(new TileUpdate(this), level);
		
		return success;
	}
	
	@Override
	boolean breakTile(boolean checkForExitAnim) {
		boolean success = super.breakTile(checkForExitAnim);
		if(success)
			ServerCore.getServer().broadcast(new TileUpdate(this), level);
		
		return success;
	}
	
	@Override
	boolean replaceTile(@NotNull TileType newType) {
		boolean success = super.replaceTile(newType);
		if(success)
			ServerCore.getServer().broadcast(new TileUpdate(this), level);
		
		return success;
	}
	
	@Override
	public String toString() { return getType().getName()+" ServerTile"; }
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
	
}
