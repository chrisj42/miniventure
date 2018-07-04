package miniventure.game.world.tile;

import miniventure.game.world.ServerLevel;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import org.jetbrains.annotations.NotNull;

/** @noinspection EqualsAndHashcode*/
public class ServerTile extends Tile {
	
	private final ServerLevel level;
	
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
		super(level, x, y, types, null);
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
	public float update() {
		float nextUpdate = super.update();
		getType().getRenderer().transitionManager.tryFinishAnimation(this);
		updateSprites();
		return nextUpdate;
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
}
