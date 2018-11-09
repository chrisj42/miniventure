package miniventure.game.world;

import miniventure.game.item.ServerItem;
import miniventure.game.item.ServerItem;

import com.badlogic.gdx.math.MathUtils;

public class ItemDrop {
	
	private ServerItem item;
	private final int minCount;
	private final int maxCount;
	private float bias;
	
	public ItemDrop(ServerItem item) { this(item, 1); }
	public ItemDrop(ServerItem item, int count) { this(item, count, count); }
	public ItemDrop(ServerItem item, int minCount, int maxCount) { this(item, minCount, maxCount, 0.5f); }
	public ItemDrop(ServerItem item, int minCount, int maxCount, float bias) {
		
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
		while(min != max) {
			int halfSize = (max - min + 1) / 2;
			if(MathUtils.random() >= 1 - bias) // right side
				min += halfSize;
			else // left side
				max -= halfSize;
		}
		return min;
	}
	
	public ServerItem[] getDroppedItems() {
		ServerItem[] items = new ServerItem[getItemsDropped()];
		for(int i = 0; i < items.length; i++)
			items[i] = item.copy();
		
		return items;
	}
}
