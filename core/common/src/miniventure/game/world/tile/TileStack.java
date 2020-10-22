package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.TreeMap;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.world.Point;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileStack<T extends TileType> {
	
	private static final TileTypeEnum baseType = TileTypeEnum.HOLE;
	
	// For now, TileStacks cannot have multiple of the same TileType... has this been enforced?
	
	private final Tile tile;
	
	// bottom tile is first, top tile is last.
	final Array<T> stack;
	final TreeMap<TileTypeEnum, TileTypeDataMap> data = new TreeMap<>();
	
	// private final EnumMap<TileTypeEnum, TileTypeDataMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	// private final EnumMap<TileTypeEnum, TileTypeDataMap> cacheMaps = new EnumMap<>(TileTypeEnum.class);
	private final Object dataLock = new Object();
	// private Object[] data; // the data map
	
	/*TileStack(T[] types) {
		for(T type: types)
			addLayer(type);
	}*/
	@SuppressWarnings("unchecked")
	TileStack(@NotNull Tile tile) {
		this.tile = tile;
		WorldManager world = tile.getWorld();
		Class<T> clazz = (Class<T>) world.getTileType(baseType).getClass();
		stack = new Array<>(true, 1, clazz);
		T type = (T) world.getTileType(baseType);
		addLayer(type, type.createDataMap());
	}
	
	private void sync(Action a) {
		synchronized (dataLock) { a.act(); }
	}
	private <RT> RT sync(FetchFunction<RT> func) {
		synchronized (dataLock) { return func.get(); }
	}
	
	public int size() { return sync(() -> stack.size); }
	
	@SuppressWarnings("unchecked")
	void setStack(WorldManager world, TileTypeInfo[] stackInfo) {
		synchronized (dataLock) {
			stack.clear();
			stack.ensureCapacity(stackInfo.length);
			data.clear();
			for(TileTypeInfo info: stackInfo) {
				stack.add((T) world.getTileType(info.typeEnum));
				data.put(info.typeEnum, info.getData());
			}
		}
	}
	
	// called by Tile.java
	TileTypeDataMap getDataMap(TileTypeEnum tileType) {
		return sync(() -> data.get(tileType));
	}
	// TileTypeDataMap getCacheMap(TileTypeEnum tileType) {
	// 	return sync(() -> cacheMaps.get(tileType));
	// }
	
	public T getTopLayer() { return sync(() -> stack.get(stack.size-1)); }
	
	public T[] getTypes() { return sync((FetchFunction<T[]>) stack::toArray); }
	
	public TileTypeEnum[] getEnumTypes() {
		synchronized (dataLock) {
			TileTypeEnum[] types = new TileTypeEnum[stack.size];
			for(int i = 0; i < stack.size; i++)
				types[i] = stack.get(i).getTypeEnum();
			return types;
		}
	}
	
	/*public boolean hasType(@NotNull T type) {
		for(T layer: stack)
			if(type.equals(layer))
				return true;
		
		return false;
	}*/
	
	public T getLayerFromTop(int offset) { return getLayerFromTop(offset, false); }
	public T getLayerFromTop(int offset, boolean clamp) { return getLayer(clamp(size()-1-offset, clamp)); }
	public T getLayerFromBottom(int offset) { return getLayerFromBottom(offset, false); }
	public T getLayerFromBottom(int offset, boolean clamp) { return getLayer(clamp(offset, clamp)); }
	private T getLayer(int offset) {
		return sync(() -> stack.get(offset));
	}
	
	private int clamp(int idx) { return clamp(idx, true); }
	private int clamp(int idx, boolean doClamp) { return doClamp ? Math.max(Math.min(idx, size()-1), 0) : idx; }
	
	@SuppressWarnings("unchecked")
	void addLayer(@NotNull T newLayer, @NotNull TileTypeDataMap dataMap) {
		synchronized (dataLock) {
			stack.add(newLayer);
			data.put(newLayer.getTypeEnum(), dataMap);
			if(newLayer.isMulti()) {
				// spread to rest of tiles
				TileTypeEnum type = newLayer.getTypeEnum();
				for(int x = 0; x < type.size.x; x++) {
					for(int y = 0; y < type.size.y; y++) {
						if(x == 0 && y == 0) continue;
						TileStack<T> ostack = (TileStack<T>) tile.getLevel().getTile(tile.x+x, tile.y+y).getTypeStack();
						//noinspection NestedSynchronizedStatement
						synchronized (ostack.dataLock) {
							ostack.stack.add(newLayer);
							ostack.data.put(newLayer.getTypeEnum(), dataMap);
						}
					}
				}
			}
		}
	}
	
	@Nullable
	@SuppressWarnings("unchecked")
	T removeLayer() {
		synchronized (dataLock) {
			if(stack.size == 1) return null;
			T type = stack.removeIndex(stack.size-1);
			data.remove(type.getTypeEnum());
			if(type.isMulti()) {
				// remove others
				Point size = type.getTypeEnum().size;
				for(int x = 0; x < size.x; x++) {
					for(int y = 0; y < size.y; y++) {
						if(x == 0 && y == 0) continue;
						TileStack<T> ostack = (TileStack<T>) tile.getLevel().getTile(tile.x+x, tile.y+y).getTypeStack();
						//noinspection NestedSynchronizedStatement
						synchronized (ostack.dataLock) {
							ostack.stack.removeValue(type, false);
							ostack.data.remove(type.getTypeEnum());
						}
					}
				}
			}
			return type;
		}
	}
	
	public static class TileData {
		public final int[] typeOrdinals;
		public final String[] data;
		private final transient Version dataVersion; // null = serial, !null = from file
		
		private TileData() { this((int[])null, null); }
		private TileData(int[] typeOrdinals, String[] data) {
			this.typeOrdinals = typeOrdinals;
			this.data = data;
			dataVersion = null;
		}
		public TileData(Tile tile, boolean save) {
			this.dataVersion = save ? Version.CURRENT : null; // this is kinda unnecessary because nothing ever uses this version variable for saving, but whatever, doing this maintains consistent state meaning.
			TileStack<?> stack = tile.getTypeStack();
			synchronized (stack.dataLock) {
				TileTypeEnum[] tileTypes = stack.getEnumTypes();
				typeOrdinals = new int[tileTypes.length];
				for(int i = 0; i < tileTypes.length; i++) {
					TileTypeEnum type = tileTypes[i];
					typeOrdinals[i] = type.ordinal();
				}
				
				this.data = new String[tileTypes.length];
				for(int i = 0; i < data.length; i++)
					data[i] = stack.data.get(tileTypes[i]).serialize(save);
			}
		}
		
		public TileData(Version dataVersion, String tileData) {
			this.dataVersion = dataVersion;
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
		
		public TileTypeInfo[] parseStack(WorldManager world) {
			TileTypeInfo[] stack = new TileTypeInfo[typeOrdinals.length];
			for(int i = 0; i < stack.length; i++) {
				TileTypeEnum typeEnum = TileTypeEnum.value(typeOrdinals[i]);
				TileType type = world.getTileType(typeEnum);
				TileTypeDataMap dataMap = type.parseDataMap(data[i], dataVersion);
				stack[i] = new TileTypeInfo(typeEnum, dataMap);
			}
			
			return stack;
		}
		
		public TileTypeEnum[] getTypes() {
			TileTypeEnum[] types = new TileTypeEnum[typeOrdinals.length];
			for(int i = 0; i < types.length; i++) {
				types[i] = TileTypeEnum.value(typeOrdinals[i]);
			}
			return types;
		}
		
		public TileTypeDataMap[] getDataMaps(WorldManager world) {
			TileTypeDataMap[] maps = new TileTypeDataMap[data.length];
			TileType[] tileTypes = ArrayUtils.mapArray(int.class, typeOrdinals, TileType.class,
					ord -> world.getTileType(TileTypeEnum.value(ord))
			);
			for(int i = 0; i < data.length; i++)
				maps[i] = tileTypes[i].parseDataMap(data[i], dataVersion);
			return maps;
		}
	}
	
	public String getDebugString() {
		StringBuilder str = new StringBuilder(getClass().getSimpleName()).append('[');
		TileTypeEnum[] types;
		TileTypeDataMap[] maps;
		synchronized (dataLock) {
			types = getEnumTypes();
			maps = data.values().toArray(new TileTypeDataMap[0]);
		}
		for(int i = 0; i < types.length; i++) {
			str.append(types[i]).append(':').append(maps[i]);
			if(i < types.length - 1)
				str.append(',');
		}
		str.append(']');
		return str.toString();
	}
}
