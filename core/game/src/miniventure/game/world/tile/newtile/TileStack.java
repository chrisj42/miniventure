package miniventure.game.world.tile.newtile;

import java.util.LinkedList;

import miniventure.game.world.tile.newtile.data.DataMap;

public class TileStack {
	
	private LinkedList<TileLayer> stack = new LinkedList<>();
	
	public TileStack() {
		stack.push(TileType.HOLE.get(new DataMap()));
	}
	
	
	
}
