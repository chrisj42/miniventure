package miniventure.game.world.tile;

import java.util.LinkedList;
import java.util.List;

import miniventure.game.world.WorldManager;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileStack {
	
	private static final TileTypeEnum baseType = TileTypeEnum.HOLE;
	
	// For now, TileStacks cannot have multiple of the same TileType.
	
	// top tile is first, bottom tile is last.
	private LinkedList<TileType> stack = new LinkedList<>();
	private LinkedList<TileType> groundStack = new LinkedList<>(); // ground tiles only
	
	public TileStack(@NotNull WorldManager world) { pushLayer(baseType.getTileType(world)); }
	public TileStack(TileType[] types) {
		for(TileType type: types)
			pushLayer(type);
	}
	public TileStack(@NotNull WorldManager world, TileTypeEnum[] enumTypes) {
		for(TileTypeEnum type: enumTypes)
			pushLayer(type.getTileType(world));
	}
	
	public int size() { return stack.size(); }
	
	public TileType getTopLayer() { return stack.peek(); }
	public TileType getGroundType() { return stack.peekLast(); }
	
	public TileType[] getTypes() { return getTypes(false); }
	public TileType[] getTypes(boolean includeCovered) {
		if(includeCovered)
			return stack.toArray(new TileType[stack.size()]);
		else {
			List<TileType> typeList = stack.subList(0, stack.indexOf(groundStack.peekFirst())+1);
			return typeList.toArray(new TileType[typeList.size()]);
		}
	}
	public TileTypeEnum[] getEnumTypes() { return getEnumTypes(false); }
	public TileTypeEnum[] getEnumTypes(boolean includeCovered) {
		TileType[] tileTypes = getTypes(includeCovered);
		TileTypeEnum[] types = new TileTypeEnum[tileTypes.length];
		for(int i = 0; i < types.length; i++)
			types[i] = tileTypes[i].getEnumType();
		return types;
	}
	
	public boolean hasType(@NotNull TileType type) {
		for(TileType layer: stack)
			if(type.equals(layer))
				return true;
		
		return false;
	}
	
	public TileType getLayerFromTop(int offset) { return getLayerFromTop(offset, false); }
	public TileType getLayerFromTop(int offset, boolean clamp) { return stack.get(clamp(offset, clamp)); }
	public TileType getLayerFromBottom(int offset) { return getLayerFromBottom(offset, false); }
	public TileType getLayerFromBottom(int offset, boolean clamp) { return stack.get(clamp(size()-1-offset, clamp)); }
	
	private int clamp(int idx) { return clamp(idx, true); }
	private int clamp(int idx, boolean doClamp) { return doClamp ? Math.max(Math.min(idx, size()-1), 0) : idx; }
	
	void pushLayer(TileType newLayer) {
		stack.push(newLayer);
		// TODO check if layer is a ground type, and if so, add it to the ground type stack.
	}
	
	@Nullable
	TileType popLayer() {
		if(stack.size() == 1) return null;
		TileType layer = stack.pop();
		// TODO if layer is a ground type, remove it from the ground stack
		return layer;
	}
}
