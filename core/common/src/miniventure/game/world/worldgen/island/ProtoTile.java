package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.world.Point;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class ProtoTile {
	
	// experimental attempt to reduce number of (empty) arrays made during stack.toArray calls.
	private static final TileTypeEnum[] EMPTY = new TileTypeEnum[0];
	
	public final Point pos;
	private final LinkedList<TileTypeEnum> stack;
	
	ProtoTile(int x, int y) {
		pos = new Point(x, y);
		stack = new LinkedList<>();
		addLayer(TileTypeEnum.HOLE);
	}
	
	public void addLayer(TileTypeEnum type) { stack.add(type); }
	
	@NotNull
	public TileTypeEnum getTopLayer() { return stack.peekLast(); }
	
	@NotNull
	public TileTypeEnum[] getStack() { return stack.toArray(EMPTY); }
	
	public float getVal(float[][] noise) { return noise[pos.x][pos.y]; }
	
}
