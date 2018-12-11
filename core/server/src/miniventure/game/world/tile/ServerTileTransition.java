package miniventure.game.world.tile;

import java.util.EnumSet;

import miniventure.game.util.MyUtils;

public class ServerTileTransition {
	
	final String name;
	private final float duration;
	private final EnumSet<TileTypeEnum> triggerTypes;
	
	public ServerTileTransition(String name, float duration, TileTypeEnum... triggerTypes) {
		this.name = name;
		this.duration = duration;
		this.triggerTypes = MyUtils.enumSet(triggerTypes);
		// if triggertypes is empty, then anything triggers it
	}
	
	public boolean isTriggerType(TileType type) { return isTriggerType(type.getTypeEnum()); }
	public boolean isTriggerType(TileTypeEnum type) {
		return triggerTypes.size() == 0 || triggerTypes.contains(type);
	}
	
	public float getDuration() { return duration; }
}
