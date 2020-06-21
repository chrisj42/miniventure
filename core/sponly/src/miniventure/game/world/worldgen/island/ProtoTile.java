package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.world.Point;
import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

public class ProtoTile {
	
	// experimental attempt to reduce number of (empty) arrays made during stack.toArray calls.
	private static final TileType[] EMPTY = new TileType[0];
	
	public final Point pos;
	public final int id;
	private final LinkedList<TileType> stack;
	
	ProtoTile(int x, int y, int id) {
		pos = new Point(x, y);
		this.id = id;
		stack = new LinkedList<>();
		addLayer(TileType.HOLE);
	}
	
	// fixme - using under types is a bit sketchy because they don't consider more than one layer back; if you happened to put something you want to preserve under a type with an under type, it's gone.
	public void addLayer(TileType type) {
		// if(type.hasUnderType())
		// 	stack.clear(); // this is the new bottom
		stack.add(type);
	}
	
	/*public void replaceLayer(TileType type) {
		if(stack.size() == 0)
			addLayer(type);
		else
			stack.set(stack.size()-1, type);
	}*/
	
	@NotNull
	public TileType getTopLayer() {
		TileType type = stack.peekLast();
		return type == null ? TileType.HOLE : type;
	}
	
	@NotNull
	public TileType[] getStack() { return stack.toArray(EMPTY); }
	
	public float getVal(float[][] noise) { return noise[pos.x][pos.y]; }
	
	
}
