package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.world.Point;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class ProtoTile {
	
	// experimental attempt to reduce number of (empty) arrays made during stack.toArray calls.
	private static final TileTypeEnum[] EMPTY = new TileTypeEnum[0];
	
	public final Point pos;
	public final int id;
	private final LinkedList<TileTypeEnum> stack;
	
	ProtoTile(int x, int y, int id) {
		pos = new Point(x, y);
		this.id = id;
		stack = new LinkedList<>();
		addLayer(TileTypeEnum.HOLE);
	}
	
	public void addLayer(TileTypeEnum type) {
		if(type.underType != null)
			stack.clear(); // this is the new bottom
		stack.add(type);
	}
	
	/*public void replaceLayer(TileTypeEnum type) {
		if(stack.size() == 0)
			addLayer(type);
		else
			stack.set(stack.size()-1, type);
	}*/
	
	@NotNull
	public TileTypeEnum getTopLayer() {
		TileTypeEnum type = stack.peekLast();
		return type == null ? TileTypeEnum.HOLE : type;
	}
	
	@NotNull
	public TileTypeEnum[] getStack() { return stack.toArray(EMPTY); }
	
	public float getVal(float[][] noise) { return noise[pos.x][pos.y]; }
	
}
