package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.world.Point;
import miniventure.game.world.tile.TileDataTag;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeDataMap;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.tile.TileTypeInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProtoTile {
	
	// experimental attempt to reduce number of (empty) arrays made during stack.toArray calls.
	private static final TileTypeInfo[] EMPTY = new TileTypeInfo[0];
	
	public final Point pos;
	
	private final ProtoLevel level;
	private final LinkedList<TileTypeInfo> stack;
	// private final LinkedList<TileTypeInfo> dataMaps;
	
	ProtoTile(ProtoLevel level, int x, int y) {
		this.level = level;
		pos = new Point(x, y);
		// this.id = id;
		stack = new LinkedList<>();
		// dataMaps = new LinkedList<>();
		addLayer(TileTypeEnum.HOLE);
	}
	
	/*@Nullable
	private TileTypeDataMap addMulti(TileTypeEnum type) {
		TileType typeInstance = level.getWorld().getTileType(type);
		if(!typeInstance.isMulti())
			throw new IllegalArgumentException("TileType "+type+" is not a multi-tile.");
		
		// validate that we can place on adjacent tiles too
		// multi-tiles cannot be placed on other multi-tiles
		for(int x = 0; x < type.size.x; x++) {
			for(int y = 0; y < type.size.y; y++) {
				if(level.getTile(x, y).getTopLayer().getTypeInstance(level.getWorld()).isMulti())
					return null;
			}
		}
		
		// set all tiles
		TileTypeInfo info = new TileTypeInfo(level.getWorld(), type);
		stack.add(info);
		TileTypeDataMap map = info.getData();
		map.put(TileDataTag.AnchorPos, pos);
		for(int x = 0; x < type.size.x; x++) {
			for(int y = 0; y < type.size.y; y++) {
				if(x == 0 && y == 0) continue;
				level.getTile(pos.x+x, pos.y+y).stack.add(new TileTypeInfo(type, map));
			}
		}
		
		return map;
	}*/
	
	// public void addLayer(TileTypeEnum type) { stack.add(type); }
	public TileTypeDataMap addLayer(TileTypeEnum type) {
		// if(level.getWorld().getTileType(type).isMulti())
		// 	return addMulti(type);
		
		TileTypeInfo info = new TileTypeInfo(level.getWorld(), type);
		stack.add(info);
		return info.getData();
	}
	
	public TileTypeDataMap replaceLayer(TileTypeEnum type) {
		if(stack.size() == 0)
			return addLayer(type);
		else {
			TileTypeInfo info = new TileTypeInfo(level.getWorld(), type);
			stack.set(stack.size() - 1, info);
			return info.getData();
		}
	}
	
	/** @noinspection ConstantConditions*/
	@NotNull
	public TileTypeEnum getTopLayer() { return stack.peekLast().typeEnum; }
	
	@NotNull
	public TileTypeInfo[] getStack() { return stack.toArray(EMPTY); }
	/*@NotNull
	public TileTypeDataMap[] getDataMaps(@NotNull WorldManager world) {
		TileTypeDataMap[] maps = new TileTypeDataMap[dataMaps.size()];
		Iterator<TileTypeEnum> enumIter = stack.iterator();
		Iterator<TileDataTag<?>[]> dataIter = dataMaps.iterator();
		int i = 0;
		while(enumIter.hasNext()) {
			TileTypeEnum typeEnum = enumIter.next();
			TileDataTag<?>[] tags = dataIter.next();
			TileType type = world.getTileType(typeEnum);
			maps[i] = type.
		}
	}*/
	
	public float getVal(float[][] noise) { return noise[pos.x][pos.y]; }
	
	
}
