package miniventure.game.world.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.GEnumMap;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.worldgen.island.ProtoTile;
import miniventure.game.world.worldgen.island.TileProcessor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileType {
	
	public enum TileTypeEnum implements TileProcessor {
		
		HOLE(true, true),
		DIRT(true, true, null, new Color(200, 100, 0)),
		SAND(true, true, null, Color.YELLOW),
		GRASS(true, true, TileTypeEnum.DIRT, Color.GREEN),
		STONE_PATH(true, true), // todo repurpose this to a tile I can use for rocky beaches
		SNOW(true, true, null, Color.WHITE),
		SMALL_STONE(true, true, TileTypeEnum.GRASS, Color.LIGHT_GRAY),
		WATER(true, true, null, Color.BLUE.darker(), 0.6f),
		DOCK(true, false),
		COAL_ORE(false, true, TileTypeEnum.DIRT),
		IRON_ORE(false, true, TileTypeEnum.DIRT),
		TUNGSTEN_ORE(false, true, TileTypeEnum.DIRT),
		RUBY_ORE(false, true, TileTypeEnum.DIRT),
		STONE(false, true, TileTypeEnum.DIRT, Color.GRAY),
		STONE_FLOOR(true, true),
		WOOD_WALL(false, false), // by saying it's not opaque, grass will still connect under it
		STONE_WALL(false, false),
		OPEN_DOOR(true, false),
		CLOSED_DOOR(false, false),
		TORCH(true, false),
		CACTUS(false, false, null, Color.GREEN.darker().darker()),
		CARTOON_TREE(false, false, null, Color.GREEN.darker().darker()),
		DARK_TREE(false, false, null, Color.GREEN.darker().darker()),
		PINE_TREE(false, false, null, Color.GREEN.darker().darker()),
		POOF_TREE(false, false, null, Color.GREEN.darker().darker()),
		AIR(true, false);
		
		public final boolean walkable;
		public final boolean opaque;
		public final float speedRatio;
		@NotNull
		public final Color color;
		@Nullable // those with non-null under type are always going to have the same type under them
		public final TileTypeEnum underType;
		
		TileTypeEnum(boolean walkable, boolean opaque) { this(walkable, opaque, null, null); }
		TileTypeEnum(boolean walkable, boolean opaque, TileTypeEnum underType) {
			this(walkable, opaque, underType, null);
		}
		TileTypeEnum(boolean walkable, boolean opaque, TileTypeEnum underType, Color color) {
			this(walkable, opaque, underType, color, 1f);
		}
		TileTypeEnum(boolean walkable, boolean opaque, @Nullable TileTypeEnum underType, Color color, float speedRatio) {
			this.walkable = walkable;
			this.opaque = opaque;
			this.color = color == null ? Color.BLACK : color;
			this.underType = underType;
			this.speedRatio = speedRatio;
			GameCore.debug("Initialized TileType "+this);
		}
		
		private static final TileTypeEnum[] values = TileTypeEnum.values();
		public static TileTypeEnum value(int ord) { return values[ord]; }
		
		public TileType getTypeInstance(@NotNull WorldManager world) {
			return world.getTileType(this);
		}
		
		@Override
		// adds this tile type to the tile stack.
		public void processTile(ProtoTile tile) {
			tile.addLayer(this);
		}
		
		public enum TypeGroup {
			GROUND(DIRT, GRASS, SAND, STONE_PATH, STONE_FLOOR, SNOW);
			
			private final EnumSet<TileTypeEnum> types;
			
			TypeGroup(TileTypeEnum... types) {
				this.types = EnumSet.copyOf(Arrays.asList(types));
			}
			
			public boolean contains(TileTypeEnum type) {
				return types.contains(type);
			}
		}
	}
	
	private static class TileDataSet extends GEnumOrderedDataSet<TileDataTag> {
		TileDataSet() {
			super(TileDataTag.class);
		}
	}
	
	private final TileTypeEnum enumType;
	private final TileDataSet dataSet;
	private final TileDataSet topSet;
	
	TileType(TileTypeEnum enumType, TileDataTag<?>... dataTags) {
		this.enumType = enumType;
		dataSet = new TileDataSet();
		topSet = new TileDataSet();
	}
	
	protected void addDataTag(TileDataTag<?> dataTag) {
		if(dataTag.perLayer)
			topSet.addKey(dataTag);
		else
			dataSet.addKey(dataTag);
	}
	public Object[] createDataArray() {
		return dataSet.createDataArray();
	}
	public Object[] createTopDataArray() {
		return topSet.createDataArray();
	}
	
	// different tile types will use different data tags, and the tags that are used should be packed efficiently into an array.
	public boolean hasData(TileDataTag<?> dataTag) {
		TileDataSet set = dataTag.perLayer ? topSet : dataSet;
		return set.getDataIndex(dataTag) >= 0;
	}
	// this method will provide the index into the data array for a certain tag, for this tile type.
	public <T> T getData(TileDataTag<T> dataTag, Object[] dataArray, Object[] topArray) {
		Object[] ar = dataTag.perLayer ? topArray : dataArray;
		TileDataSet set = dataTag.perLayer ? topSet : dataSet;
		return (T) ar[set.getDataIndex(dataTag)];
	}
	public <T> void setData(TileDataTag<T> dataTag, T value, Object[] dataArray, Object[] topArray) {
		Object[] ar = dataTag.perLayer ? topArray : dataArray;
		TileDataSet set = dataTag.perLayer ? topSet : dataSet;
		ar[set.getDataIndex(dataTag)] = value;
	}
	
	// make topArray null to prevent it from being drawn from
	/*public void serializeData(TileTypeDataMap map, Object[] dataArray, @Nullable Object[] topArray) {
		for(TileDataTag tag: TileDataTag.values) {
			Object[] ar = tag.topOnly ? topArray : dataArray;
			if(ar == null) continue;
			TileDataSet set = tag.topOnly ? topSet : dataSet;
			int idx = set.getDataIndex(tag);
			if(idx < 0 || ar[idx] == null) continue;
			map.add(tag, ar[idx]);
		}
	}*/
	
	private static class DataIterator implements Iterator<String> {
		
		private final TileType tileType;
		private boolean save;
		private Object[] data;
		private Object[] top;
		// private int pos;
		private int nextPos;
		private boolean giveOrdinal = true;
		
		DataIterator(TileType tileType) {
			this.tileType = tileType;
		}
		
		void reset(boolean save, Object[] data, Object[] top) {
			this.save = save;
			this.data = data;
			this.top = top;
			// pos = -1;
			nextPos = -1;
			giveOrdinal = true;
			next();
		}
		
		@Override
		public boolean hasNext() {
			return nextPos < TileDataTag.values.length;
		}
		
		@Override
		public String next() {
			if(!hasNext()) return null;
			
			String serial = null;
			if(nextPos >= 0) {
				if (giveOrdinal) {
					giveOrdinal = false;
					return String.valueOf(TileDataTag.values[nextPos].ordinal());
				} else {
					giveOrdinal = true;
					serial = getSerial(TileDataTag.values[nextPos]);
				}
			}
			
			// find next pos
			do nextPos++;
			while(hasNext() && !currentPosValid());
			
			return serial;
		}
		
		private <T> String getSerial(TileDataTag<T> tag) {
			return tag.serialize(tileType.getData(tag, data, top));
		}
		
		private boolean currentPosValid() {
			TileDataTag<?> tag = TileDataTag.values[nextPos];
			// is this tag part of the data set?
			if(save && !tag.save || !save && !tag.send)
				return false;
			Object[] ar = tag.perLayer ? top : data;
			if(ar == null || !tileType.hasData(tag))
				return false;
			return true;
		}
	}
	private final DataIterator dataIterator = new DataIterator(this);
	private final Iterable<String> dataIterable = () -> dataIterator;
	
	// make topArray null to prevent it from being drawn from
	public String serializeData(boolean save, Object[] dataArray, @Nullable Object[] topArray) {
		dataIterator.reset(save, dataArray, topArray);
		return MyUtils.encodeStringArray(dataIterable);
	}
	
	public TileTypeEnum getTypeEnum() { return enumType; }
	
	public TileTypeEnum getUnderType() {
		return enumType.underType == null ? TileTypeEnum.HOLE : enumType.underType;
	}
	
	public boolean isWalkable() { return enumType.walkable; }
	public boolean isOpaque() { return enumType.opaque; }
	public float getSpeedRatio() {
		return enumType.speedRatio;
	}
	
	public String getName() { return MyUtils.toTitleCase(enumType.name(), "_"); }
	
	@Override
	public String toString() { return getName()+' '+getClass().getSimpleName(); }
	
	static abstract class TilePropertyGroup<T extends TileProperty, E extends TilePropertyGroup<T, E>> extends GenericEnum<T, E> {
		
		private Param<MapFunction<TileTypeEnum, T>> fetchParam;
		
		TilePropertyGroup(MapFunction<TileTypeEnum, T> defaultValue) {
			fetchParam = new Param<>(defaultValue);
		}
		
		public Value<MapFunction<TileTypeEnum, T>> as(MapFunction<TileTypeEnum, T> value) {
			return fetchParam.as(value);
		}
		
		public void addToMap(GEnumMap<E> propertyMap, ParamMap paramMap, TileType tileType) {
			T value = paramMap.get(fetchParam).get(tileType.enumType);
			propertyMap.add(as(value));
			value.registerDataTags(tileType);
		}
	}
	
}
