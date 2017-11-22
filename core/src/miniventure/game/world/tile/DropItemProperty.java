package miniventure.game.world.tile;

import miniventure.game.world.ItemDrop;

public class DropItemProperty implements TileProperty {
	
	final ItemDrop[] drops;
	
	DropItemProperty(ItemDrop... drops) {
		this.drops = drops;
	}
	
	@Override
	public int getDataCount() {
		return 0;
	}
	
	@Override
	public int[] getData() {
		return new int[0];
	}
}
