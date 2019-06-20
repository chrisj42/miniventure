package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileStack<T extends TileType> {
	
	private static final TileTypeEnum baseType = TileTypeEnum.HOLE;
	
	// For now, TileStacks cannot have multiple of the same TileType... has this been enforced?
	
	// bottom tile is first, top tile is last.
	private final LinkedList<T> stack = new LinkedList<>();
	
	private final EnumMap<TileTypeEnum, SerialMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	private final Object dataLock = new Object();
	
	/*TileStack(T[] types) {
		for(T type: types)
			addLayer(type);
	}*/
	@SuppressWarnings("unchecked")
	TileStack(@NotNull WorldManager world, TileTypeEnum[] enumTypes, @Nullable SerialMap[] dataMaps) {
		for(int i = 0; i < enumTypes.length; i++)
			//noinspection ConstantConditions // IntelliJ doesn't realize that just because dataMaps can be null, doesn't mean the elements of a non-null instance can also be null.
			addLayer((T) enumTypes[i].getTypeInstance(world), dataMaps == null ? new SerialMap() : dataMaps[i]);
	}
	
	private void sync(Action a) {
		synchronized (dataLock) { a.act(); }
	}
	private <RT> RT sync(FetchFunction<RT> func) {
		synchronized (dataLock) { return func.get(); }
	}
	
	public int size() { return sync(stack::size); }
	
	// called by Tile.java
	SerialMap getDataMap(TileTypeEnum tileType) {
		return sync(() -> dataMaps.get(tileType));
	}
	
	public T getTopLayer() { return sync(stack::peekLast); }
	
	public List<T> getTypes() { return sync(() -> new ArrayList<>(stack)); }
	
	public TileTypeEnum[] getEnumTypes() {
		List<T> tileTypes = getTypes();
		TileTypeEnum[] types = new TileTypeEnum[tileTypes.size()];
		for(int i = 0; i < types.length; i++)
			types[i] = tileTypes.get(i).getTypeEnum();
		return types;
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
	
	void addLayer(@NotNull T newLayer, @NotNull SerialMap dataMap) {
		synchronized (dataLock) {
			stack.addLast(newLayer);
			dataMaps.put(newLayer.getTypeEnum(), dataMap);
		}
	}
	
	@Nullable
	T removeLayer() {
		synchronized (dataLock) {
			if(stack.size() == 1) return null;
			T type = stack.removeLast();
			dataMaps.remove(type.getTypeEnum());
			return type;
		}
	}
	
	public static class TileData {
		public final int[] typeOrdinals;
		public final String[] data;
		
		private TileData() { this((int[])null, null); }
		private TileData(int[] typeOrdinals, String[] data) {
			this.typeOrdinals = typeOrdinals;
			this.data = data;
		}
		public TileData(Tile tile, boolean save) {
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
					data[i] = stack.dataMaps.get(tileTypes[i]).serialize(save);
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
		
		public SerialMap[] getDataMaps() { return getDataMaps(data); }
		public static SerialMap[] getDataMaps(String[] data) {
			SerialMap[] maps = new SerialMap[data.length];
			for(int i = 0; i < data.length; i++)
				maps[i] = SerialMap.deserialize(data[i], TileCacheTag.class);
			return maps;
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(getClass().getSimpleName()).append('[');
		TileTypeEnum[] types;
		SerialMap[] maps;
		synchronized (dataLock) {
			types = getEnumTypes();
			maps = dataMaps.values().toArray(new SerialMap[0]);
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
