package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileStack<T extends TileType> {
	
	private static final TileTypeEnum baseType = TileTypeEnum.HOLE;
	
	// For now, TileStacks cannot have multiple of the same TileType... has this been enforced?
	
	// bottom tile is first, top tile is last.
	private LinkedList<T> stack = new LinkedList<>();
	
	@SuppressWarnings("unchecked")
	TileStack(@NotNull WorldManager world) {
		addLayer((T) baseType.getTypeInstance(world));
	}
	TileStack(T[] types) {
		for(T type: types)
			addLayer(type);
	}
	@SuppressWarnings("unchecked")
	TileStack(@NotNull WorldManager world, TileTypeEnum[] enumTypes) {
		for(TileTypeEnum type: enumTypes)
			addLayer((T) type.getTypeInstance(world));
	}
	
	public int size() { return stack.size(); }
	
	public T getTopLayer() { return stack.peekLast(); }
	
	public List<T> getTypes() { return new ArrayList<>(stack); }
	
	public TileTypeEnum[] getEnumTypes() {
		List<T> tileTypes = getTypes();
		TileTypeEnum[] types = new TileTypeEnum[tileTypes.size()];
		for(int i = 0; i < types.length; i++)
			types[i] = tileTypes.get(i).getTypeEnum();
		return types;
	}
	
	public boolean hasType(@NotNull T type) {
		for(T layer: stack)
			if(type.equals(layer))
				return true;
		
		return false;
	}
	
	public T getLayerFromTop(int offset) { return getLayerFromTop(offset, false); }
	public T getLayerFromTop(int offset, boolean clamp) { return stack.get(clamp(size()-1-offset, clamp)); }
	public T getLayerFromBottom(int offset) { return getLayerFromBottom(offset, false); }
	public T getLayerFromBottom(int offset, boolean clamp) { return stack.get(clamp(offset, clamp)); }
	
	private int clamp(int idx) { return clamp(idx, true); }
	private int clamp(int idx, boolean doClamp) { return doClamp ? Math.max(Math.min(idx, size()-1), 0) : idx; }
	
	void addLayer(@NotNull T newLayer) {
		stack.addLast(newLayer);
	}
	
	@Nullable
	T removeLayer() {
		if(stack.size() == 1) return null;
		return stack.removeLast();
	}
}
