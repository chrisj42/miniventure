package miniventure.game.world.tile;

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
	public boolean addTile(@NotNull TileType newType) { return tileUpdate(super.addTile(newType)); }
	
	@Override
	boolean breakTile(boolean checkForExitAnim) { return tileUpdate(super.breakTile(checkForExitAnim)); }
	
	@Override
	boolean replaceTile(@NotNull TileType newType) { return tileUpdate(super.replaceTile(newType)); }
	
	private boolean tileUpdate(boolean success) {
		if(success) level.onTileUpdate(this);
		return success;
	}
	
	@Override
	public String toString() { return getType().getName()+" ServerTile"; }
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
	
}
