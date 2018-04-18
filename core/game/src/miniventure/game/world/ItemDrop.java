package miniventure.game.world;

import miniventure.game.item.Item;

public class ItemDrop {
	
	private Item item;
	private int count;
	
	public ItemDrop(Item item, int minCount, int maxCount, float biasAmt, boolean roundUp) {
		this(item, (maxCount+minCount)/2);
	}
	
	public ItemDrop(Item item) {
		this(item, 1);
	}
	
	public ItemDrop(Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	public ItemDrop(Item item, int peaceCount, int easyCount, int mediumCount, int hardCount) {
		this(item, mediumCount);
	}
	
	
	
	
	public int getItemsDropped() {
		// TODO implement this for different counts
		return count;
	}
	
	public Item[] getDroppedItems() {
		Item[] items = new Item[getItemsDropped()];
		for(int i = 0; i < items.length; i++)
			items[i] = item.copy();
		
		return items;
	}
}
