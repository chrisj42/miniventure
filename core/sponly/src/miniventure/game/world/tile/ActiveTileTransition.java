package miniventure.game.world.tile;

public class ActiveTileTransition {
	
	final TileTransition transition;
	final boolean entering;
	final float startTime;
	final TileType nextType; // add after transition if present; previous tiles on entrances will not be recorded
	
	public ActiveTileTransition(TileTransition transition, boolean entering, float startTime, TileType nextType) {
		this.transition = transition;
		this.entering = entering;
		this.startTime = startTime;
		this.nextType = nextType;
	}
}
