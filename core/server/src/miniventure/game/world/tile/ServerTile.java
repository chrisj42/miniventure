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
	public void tick() {
		for(TileType type: getTypes()) {// goes from bottom to top
			if(!(type == getType() && getProp(type, TilePropertyType.Transition).playingAnimation(this))) // only update main tile if not transitioning.
				getProp(type, TilePropertyType.Tick).tick(this);
		}
	}
	
	@Override
	public boolean update(float delta, boolean initial) {
		boolean shouldUpdate = super.update(delta, initial);
		TileType startType = getType();
		TransitionProperty transProp = getProp(startType, TilePropertyType.Transition);
		if(transProp.playingAnimation(this)) {
			transProp.getAnimationFrame(this, delta);
			shouldUpdate = (startType == getType() && transProp.playingAnimation(this)) || shouldUpdate;
		}
		
		return shouldUpdate;
	}
	
	@Override
	public String toString() { return getType().getName()+" ServerTile"; }
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
	
}
