package miniventure.game.world.tile;

public class TransitionData {
	
	public final String name;
	public final float startTime;
	public final TransitionMode mode;
	public final TileTypeEnum nextType;
	
	public TransitionData(String name, float startTime, TransitionMode mode, TileTypeEnum nextType) {
		this.name = name;
		this.startTime = startTime;
		this.mode = mode;
		this.nextType = nextType;
	}
}
