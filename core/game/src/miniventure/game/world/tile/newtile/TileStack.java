package miniventure.game.world.tile.newtile;

import java.util.LinkedList;

import miniventure.game.world.tile.newtile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileStack {
	
	// top tile is first, bottom tile is last.
	private LinkedList<TileType> stack = new LinkedList<>();
	
	public TileStack() { stack.push(TileTypeEnum.HOLE.tileType); }
	public TileStack(TileType[] types) {
		for(TileType type: types)
			pushLayer(type);
	}
	public TileStack(TileTypeEnum[] enumTypes) {
		for(TileTypeEnum type: enumTypes)
			pushLayer(type.tileType);
	}
	
	public int size() { return stack.size(); }
	
	public TileType getTopLayer() { return stack.peek(); }
	public TileType getGroundType() { return stack.peekLast(); }
	
	public TileType[] getTypes() { return stack.toArray(new TileType[stack.size()]); }
	public TileTypeEnum[] getEnumTypes() {
		TileTypeEnum[] types = new TileTypeEnum[stack.size()];
		for(int i = 0; i < types.length; i++)
			types[i] = stack.get(i).getEnumType();
		return types;
	}
	
	public boolean hasType(@NotNull TileType type) {
		for(TileType layer: stack)
			if(type.equals(layer))
				return true;
		
		return false;
	}
	
	public TileType getLayerFromTop(int offset) { return stack.get(offset); }
	public TileType getLayerFromBottom(int offset) { return stack.get(size()-1-offset); }
	
	public void pushLayer(TileType newLayer) {
		// TODO later, check if the new layer is a ground layer; if so, then add this stack to the data of the new layer, clear it, and add the new layer to the now empty stack.
		
		stack.push(newLayer);
	}
	
	@Nullable
	public TileType popLayer() {
		// In the future, return null if the stack only has a hole tile, meaning it has just a ground tile and that tile doesn't have a substack in its data.
		TileType layer = stack.pop();
		if(stack.size() == 0) {
			// TODO check layer's data map for lower stack; only do below if none found.
			stack.push(TileTypeEnum.HOLE.tileType);
		}
		return layer;
	}
}
