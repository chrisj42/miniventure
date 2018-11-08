package miniventure.game.world.tile;

import java.lang.reflect.Array;
import java.util.LinkedList;

import miniventure.game.world.WorldManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileStack<T extends TileType> {
	
	private static final TileTypeEnum baseType = TileTypeEnum.HOLE;
	
	// For now, TileStacks cannot have multiple of the same TileType... has this been enforced?
	
	private final Class<T> typeClass;
	// bottom tile is first, top tile is last.
	private LinkedList<T> stack = new LinkedList<>();
	
	@SuppressWarnings("unchecked")
	TileStack(Class<T> typeClass, @NotNull WorldManager world) {
		this.typeClass = typeClass;
		addLayer((T) baseType.getTypeInstance(world));
	}
	TileStack(Class<T> typeClass, T[] types) {
		this.typeClass = typeClass;
		for(T type: types)
			addLayer(type);
	}
	@SuppressWarnings("unchecked")
	TileStack(Class<T> typeClass, @NotNull WorldManager world, TileTypeEnum[] enumTypes) {
		this.typeClass = typeClass;
		for(TileTypeEnum type: enumTypes)
			addLayer((T) type.getTypeInstance(world));
	}
	
	@SuppressWarnings("unchecked")
	private T[] emptyTypeArray() {
		return (T[]) Array.newInstance(typeClass, 0);
	}
	
	public int size() { return stack.size(); }
	
	public T getTopLayer() { return stack.peekLast(); }
	
	public T[] getTypes() { return stack.toArray(emptyTypeArray()); }
	
	public TileTypeEnum[] getEnumTypes() {
		T[] tileTypes = getTypes();
		TileTypeEnum[] types = new TileTypeEnum[tileTypes.length];
		for(int i = 0; i < types.length; i++)
			types[i] = tileTypes[i].getTypeEnum();
		return types;
	}
	
	public boolean hasType(@NotNull T type) {
		for(T layer: stack)
			if(type.equals(layer))
				return true;
		
		return false;
	}
	
	public T getLayerFromTop(int offset) { return getLayerFromTop(offset, false); }
	public T getLayerFromTop(int offset, boolean clamp) { return stack.get(clamp(offset, clamp)); }
	public T getLayerFromBottom(int offset) { return getLayerFromBottom(offset, false); }
	public T getLayerFromBottom(int offset, boolean clamp) { return stack.get(clamp(size()-1-offset, clamp)); }
	
	private int clamp(int idx) { return clamp(idx, true); }
	private int clamp(int idx, boolean doClamp) { return doClamp ? Math.max(Math.min(idx, size()-1), 0) : idx; }
	
	void addLayer(T newLayer) {
		stack.addLast(newLayer);
	}
	
	@Nullable
	T removeLayer() {
		if(stack.size() == 1) return null;
		return stack.removeLast();
	}
}
