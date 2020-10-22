package miniventure.game.world.tile;

import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.FetchParam;
import miniventure.game.util.param.FetchParam.FetchValue;
import miniventure.game.world.Point;

import org.jetbrains.annotations.Nullable;

public abstract class TileType {
	
	// used in subclasses to conveniently create Params for the tile types.
	static class TParam<T extends TileProperty> extends FetchParam<TileTypeEnum, T> {
		
		TParam(MapFunction<TileTypeEnum, T> defaultValue) {
			super(defaultValue);
		}
		/*private TParam(T defaultValue) {
			super(type -> defaultValue);
		}
		static <T extends TileProperty> TParam<T> direct(T defaultValue) {
			return new TParam<>(defaultValue);
		}*/
		
		@Override
		public TValue<T> as(MapFunction<TileTypeEnum, T> mapper) {
			return new TValue<>(this, mapper);
		}
		
		@Override
		public TValue<T> as(FetchFunction<T> fetcher) {
			return new TValue<>(this, tileType -> fetcher.get());
		}
	}
	
	static class TValue<T extends TileProperty> extends FetchValue<TileTypeEnum, T> {
		TValue(TParam<T> param, MapFunction<TileTypeEnum, T> value) {
			super(param, value);
		}
	}
	
	private final TileTypeEnum enumType;
	private final TileTypeDataOrder dataTypes;
	
	TileType(TileTypeEnum enumType) {
		this.enumType = enumType;
		dataTypes = new TileTypeDataOrder(this);
		
		if(isMulti())
			registerData(TileDataTag.AnchorPos);
	}
	
	public TileTypeEnum getTypeEnum() { return enumType; }
	
	public boolean isWalkable() { return enumType.walkable; }
	public float getSpeedRatio() {
		return enumType.speedRatio;
	}
	
	public boolean isMulti() {
		return enumType.size.x > 1 || enumType.size.y > 1;
	}
	
	// public Point getSize() { return enumType.size; }
	
	public String getName() { return MyUtils.toTitleCase(enumType.name(), "_"); }
	
	void registerData(TileDataTag<?> tag) {
		// if the same tag is registered more than once, subsequent registrations are ignored
		dataTypes.addKey(tag);
	}
	
	TileTypeDataMap createDataMap() {
		return new TileTypeDataMap(dataTypes);
	}
	
	TileTypeDataMap parseDataMap(String mapData, @Nullable Version versionIfFile) {
		return new TileTypeDataMap(dataTypes, mapData, versionIfFile);
	}
	
	@Override
	public String toString() { return getName()+' '+getClass().getSimpleName(); }
	
}
