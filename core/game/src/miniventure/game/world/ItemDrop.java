package miniventure.game.world;

import miniventure.game.item.Item;

import com.badlogic.gdx.math.MathUtils;

public class ItemDrop {
	
	private Item item;
	private final int minCount;
	private final int maxCount;
	private float bias;
	
	public ItemDrop(Item item) { this(item, 1); }
	public ItemDrop(Item item, int count) { this(item, count, count); }
	public ItemDrop(Item item, int minCount, int maxCount) { this(item, minCount, maxCount, 0.5f); }
	public ItemDrop(Item item, int minCount, int maxCount, float bias) {
		
		/*
			I can bias so that 0.5 is no bias. 1 is always max count, 0 is always min count.
				split up item count determination as a sequence of binary decisions -- left or right.
				
				The recursive system works like this: use math.random, and if it is >= bias, repeat with right half, else repeat with left half. Continue until only one choice remains.
				If there is a center item, then the center item is still possible whether one goes left, or right.
		 */
		
		this.item = item;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.bias = bias;
	}
	
	
	private int getItemsDropped() { return getItemsDropped(minCount, maxCount); }
	private int getItemsDropped(int min, int max) {
		if(min == max) return min;
		
		int halfSize = (max-min+1)/2;
		if(MathUtils.random() >= 1-bias) // right side
			min += halfSize;
		else // left side
			max -= halfSize;
		
		return getItemsDropped(min, max);
	}
	
	public Item[] getDroppedItems() {
		Item[] items = new Item[getItemsDropped()];
		for(int i = 0; i < items.length; i++)
			items[i] = item.copy();
		
		return items;
	}
}
