package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.FetchParam;

public abstract class TileType {
	
	// used in subclasses to conveniently create Params for the tile types.
	static class TParam<T> extends FetchParam<TileTypeEnum, T> {
		TParam(MapFunction<TileTypeEnum, T> defaultValue) {
			super(defaultValue);
		}
	}
	
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
