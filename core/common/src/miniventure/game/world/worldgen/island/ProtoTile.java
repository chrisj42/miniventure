package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import miniventure.game.world.Point;
import miniventure.game.world.tile.TileTypeEnum;

public class ProtoTile {
	
	public final Point pos;
	final LinkedList<TileTypeEnum> stack;
	
	ProtoTile(int x, int y) {
		pos = new Point(x, y);
		stack = new LinkedList<>();
	}
	
}
