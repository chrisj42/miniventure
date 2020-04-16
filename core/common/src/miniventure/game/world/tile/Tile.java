package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Tile implements WorldObject {
	
	/*
		So. The client, in terms of properties, doesn't have to worry about tile interaction properties. It always sends a request to the server when interactions should occur.
		The server, in turn, doesn't have to worry about animation or rendering properties.
		
		So, it seems like we have a situation where there are ServerProperties and ClientProperties, and some property types are both.
		
		This could mean that I should just take the client and server properties out of the main game module and into their own respective modules. But that would mean they can never be referenced in the main package, and I like how the property types are all given in the same place, the TileType class. So maybe I can leave the shell there, and just separate the stuff that actually does the heavy lifting..?
		
		 Hey, that actually gives me an idea: What if the property types/classes I specify in the TileType class weren't actually the objects that I used for the tile behaviors, in the end? What if they were just markers, and the actual property instances were instantiated later? For entities, they would obviously be instantiated on entity creation, but since there are so many tiles loaded at one time, we have to do tiles different...
		 Hey, I know: how about we have a tile property instance fetcher, that creates all the actual tile property instances within the respective client/server modules, with the right classes, based on the given main property class? That could work! Then, whenever a tile property was asked for, it would fetch it from the fetcher, given the TileType and property class/type. With entities, each would simply have their own list, their own fetcher.
		 The fetchers would be created in ClientCore and ServerCore, more or less. or maybe the Worlds, since the WorldManager class would have to have a way to fetch a property instance given a TileType and and Property class/type. For entities, the fetcher would be given the entity instance too. Or maybe each entity would just already have its properties. Yeah that'll probably be the case.
		 
		 So! End result. Actual property instances are created in Client/Server individually, not in the TileType enum. That is only where the basic templates go. The is a fetcher that can take a property type instance, and return a completed property instance of that type.
		 Note, we might end up having a property type enum as well as a tile type enum...
	 */
	
	public static final int RESOLUTION = 32;
	public static final int SCALE = 4;
	public static final int SIZE = RESOLUTION * SCALE;
	
	// private TileStack tileStack;
	private Array<TileType> typeStack;
	private Array<Object[]> typeData;
	private Object[] topData;
	private TileContext contextCache;
	
	@NotNull private final Level level;
	final int x, y;
	private final TileTag tag;
	// final EnumMap<TileTypeEnum, SerialMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	
	// the TileType array is ALWAYS expected in order of bottom to top.
	Tile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @Nullable TileStackData dataMaps) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.tag = new TileTag(this);
		contextCache = new TileContext();
		// contextCache.tile = this;
		typeStack = new Array<>(true, 4, TileType.class);
		typeData = new Array<>(true, 4, Object[].class);
		setStack(types, dataMaps);
	}
	
	// abstract TileStack makeStack(@NotNull TileTypeEnum[] types, @Nullable TileTypeDataMap[] dataMaps);
	
	// void setTileStack(TileStack stack) { this.tileStack = stack; }
	
	// init and client
	void setStack(TileTypeEnum[] types, @Nullable TileStackData dataMaps) {
		typeStack.clear();
		typeData.clear();
		topData = null;
		for(int i = 0; i < types.length; i++) {
			TileType type = getWorld().getTileType(types[i]);
			typeStack.add(type);
			typeData.add(type.createDataArray());
			TileContext context = setMainContext();
			if(i == types.length - 1)
				topData = type.createTopDataArray();
			// add given properties
			if(dataMaps != null) {
				dataMaps.deserializeTypeData(i, context);
			}
		}
	}
	
	int getStackSize() { return typeStack.size; }
	
	TileType getLayer(int layer) { return typeStack.get(layer); }
	
	TileType getOnBreakType() {
		return typeStack.size == 1 ? getWorld().getTileType(getType().getUnderType()) : typeStack.get(typeStack.size - 2);
	}
	
	Iterable<? extends TileType> getStack() {
		return typeStack;
	}
	
	/*TileType[] getStackCopy() {
		return typeStack.toArray();
	}*/
	
	// server
	void addLayer(TileType newLayer) {
		typeStack.add(newLayer);
		typeData.add(newLayer.createDataArray());
		topData = newLayer.createTopDataArray();
	}
	
	// server
	TileType removeLayer() {
		TileType removed = typeStack.removeIndex(typeStack.size - 1);
		typeData.removeIndex(typeData.size - 1);
		
		if(typeStack.size == 0) // bottom of stack; ask type for new type
			addLayer(getWorld().getTileType(removed.getUnderType()));
		else // type is in stack
			topData = typeStack.get(typeStack.size - 1).createTopDataArray();
		
		return removed;
	}
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	
	public TileType getType() { return typeStack.get(typeStack.size - 1); }
	// public TileStack getTypeStack() { return tileStack; }
	
	// using the word "set" instead of "get" to better convey that calling this at the wrong time could have adverse effects
	protected TileContext setContext(int layer) {
		contextCache.layer = layer;
		return contextCache;
	}
	protected TileContext setMainContext() {
		return setContext(typeStack.size - 1);
	}
	
	// public SerialMap getDataMap(TileType tileType) { return getDataMap(tileType.getTypeEnum()); }
	/*@NotNull
	public TileDataTag.TileTypeDataMap getDataMap(TileTypeEnum tileType) {
		TileTypeDataMap map = tileStack.getDataMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			GameCore.error("ERROR: tile " + toLocString() + " came back with a null data map for tiletype " + tileType + "; stack: " + tileStack.getDebugString(), true, true);
			map = new TileTypeDataMap();
		}
		return map;
	}*/
	
	/*@NotNull
	public TileDataTag.TileDataMap getDataMap(TileTypeEnum tileType) {
		TileDataMap map = tileStack.getDataMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			GameCore.error("ERROR: tile " + toLocString() + " came back with a null cache map for tiletype " + tileType + "; stack: " + tileStack.getDebugString(), true, true);
			map = new TileDataMap();
		}
		return map;
	}*/
	
	
	public HashSet<Tile> getAdjacentTiles(boolean includeCorners) {
		if(includeCorners)
			return level.getAreaTiles(x, y, 1, false);
		else {
			HashSet<Tile> tiles = new HashSet<>();
			if(x > 0) tiles.add(level.getTile(x-1, y));
			if(y < level.getHeight()-1) tiles.add(level.getTile(x, y+1));
			if(x < level.getWidth()-1) tiles.add(level.getTile(x+1, y));
			if(y > 0) tiles.add(level.getTile(x, y-1));
			tiles.remove(null);
			return tiles;
		}
	}
	
	public boolean isAdjacent(@NotNull Tile other, boolean includeCorners) {
		if(includeCorners)
			return Math.abs(x - other.x) <= 1 && Math.abs(y - other.y) <= 1;
		int xdiff = Math.abs(x - other.x);
		int ydiff = Math.abs(y - other.y);
		return xdiff == 1 && ydiff == 0 || xdiff == 0 && ydiff == 1;
	}
	
	
	
	@Override
	public String toString() { return getType().getName()+' '+getClass().getSimpleName(); }
	
	public String toLocString() { return x+","+y+" ("+toString()+')'; }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return level.equals(o.level) && x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return Point.javaPointHashCode(x, y) + level.getLevelId() * 17; }
	
	public static class TileTag implements Tag<Tile> {
		public final int x;
		public final int y;
		public final int levelId;
		
		private TileTag() { this(0, 0, 0); }
		public TileTag(Tile tile) { this(tile.x, tile.y, tile.getLevel().getLevelId()); }
		public TileTag(int x, int y, int levelId) {
			this.x = x;
			this.y = y;
			this.levelId = levelId;
		}
		
		@Override
		public Tile getObject(WorldManager world) {
			Level level = world.getLevel(levelId);
			if(level != null)
				return level.getTile(x, y);
			return null;
		}
	}
	
	@Override
	public Tag<Tile> getTag() { return tag; }
	
	public class TileContext {
		
		private int layer;
		
		public <T extends WorldManager> T getWorld() { return (T) getTile().getWorld(); }
		
		public <T extends Level> T getLevel() { return (T) getTile().getLevel(); }
		
		public <T extends Tile> T getTile() { return (T) Tile.this; }
		
		public <T extends TileType> T getType() {
			return (T) typeStack.get(layer);
		}
		
		
		public <T> T getData(TileDataTag<T> dataTag) {
			return getType().getData(dataTag, typeData.get(layer), topData);
		}
		public <T> T getData(TileDataTag<T> dataTag, T defaultValue) {
			T data = getData(dataTag);
			return data == null ? defaultValue : data;
		}
		
		public <T> void setData(TileDataTag<T> dataTag, T data) {
			getType().setData(dataTag, data, typeData.get(layer), topData);
		}
		
		public <T> T clearData(TileDataTag<T> dataTag) {
			return updateData(dataTag, null);
		}
		
		// returns old value.
		public <T> T updateData(TileDataTag<T> dataTag, T data) {
			T value = getData(dataTag);
			setData(dataTag, data);
			return value;
		}
		
		// sets the data if not present, then returns now-current value
		public <T> T getOrInitData(TileDataTag<T> dataTag, T putIfAbsent) {
			T value = getData(dataTag);
			if(value == null) {
				setData(dataTag, putIfAbsent);
				value = putIfAbsent;
			}
			return value;
		}
	}
	
	// todo fix tile data; tile data maps are carefully indexed with the data types that will be used, including both saving and non-saving data tags. When saving a tile, I need to be able to turn them all into strings, but exclude the unnecessary keys. Then go in reverse. It might work to make another ordered data set that contains the orderings of saved and serialized sets... but any more of those sets and things are getting cumbersome. there ought to be a better way.
	// tile data objects are made to:
	// - send tile data to clients
	// - save tile data to file
	// these are the only two use cases.
	public static class TileData {
		public final int[] typeOrdinals;
		public final String[] data;
		
		private TileData() { typeOrdinals = null; data = null; }
		public TileData(Tile tile, boolean save) {
			typeOrdinals = new int[tile.typeStack.size];
			for(int i = 0; i < typeOrdinals.length; i++) {
				TileTypeEnum type = tile.typeStack.get(i).getTypeEnum();
				typeOrdinals[i] = type.ordinal();
			}
			
			this.data = new String[typeOrdinals.length];
			for(int i = 0; i < data.length; i++) {
				// only include the top data for the top tile type
				Object[] topData = i == data.length - 1 ? tile.topData : null;
				data[i] = tile.typeStack.get(i).serializeData(save, tile.typeData.get(i), topData);
			}
		}
		
		public TileData(Version dataVersion, String tileData) {
			String[] all = MyUtils.parseLayeredString(tileData);
			data = Arrays.copyOfRange(all, 1, all.length);
			
			typeOrdinals = ArrayUtils.mapArray(all[0].split(","), int.class, int[].class, Integer::parseInt);
		}
		
		public String serialize() {
			String[] all = new String[data.length+1];
			System.arraycopy(data, 0, all, 1, data.length);
			
			all[0] = ArrayUtils.arrayToString(typeOrdinals, ",");
			
			return MyUtils.encodeStringArray(all);
		}
		
		public TileTypeEnum[] getTypes() { return getTypes(typeOrdinals); }
		public static TileTypeEnum[] getTypes(int[] typeOrdinals) {
			TileTypeEnum[] types = new TileTypeEnum[typeOrdinals.length];
			for(int i = 0; i < types.length; i++) {
				types[i] = TileTypeEnum.value(typeOrdinals[i]);
			}
			return types;
		}
		
		public TileStackData getDataMaps() { return getDataMaps(data); }
		public static TileStackData getDataMaps(String[] data) {
			return new TileStackData(data);
		}
		
	}
}
