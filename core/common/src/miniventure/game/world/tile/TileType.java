package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;

public abstract class TileType {
	
	private final TileTypeEnum enumType;
	
	TileType(TileTypeEnum enumType) { this.enumType = enumType; }
	
	public TileTypeEnum getTypeEnum() { return enumType; }
	
	public boolean isWalkable() { return enumType.walkable; }
	public float getSpeedRatio() {
		return enumType.speedRatio;
	}
	
	public String getName() { return MyUtils.toTitleCase(enumType.name(), "_"); }
	
	@Override
	public String toString() { return getName()+' '+getClass().getSimpleName(); }
}
