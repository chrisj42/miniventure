package miniventure.game.world.tile;

import miniventure.game.item.ToolType;

public class PreferredToolProperty implements TileProperty {
	
	PreferredToolProperty(ToolType tool) {
		
	}
	
	@Override
	public int getDataCount() {
		return 1;
	}
	
	@Override
	public int[] getData() {
		return new int[0];
	}
}
