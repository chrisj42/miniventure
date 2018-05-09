package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.util.InstanceCounter;
import miniventure.game.util.MyUtils;

public class Inventory {
	
	private static final HandItem hand = new HandItem(); // ref var so it isn't constantly re-instantiated.
	
	private Item[] items;
	private final InstanceCounter<Item> itemCounter = new InstanceCounter<>();
	
	public Inventory(int size) {
		items = new Item[size];
		reset();
	}
	
	public void reset() {
		Arrays.fill(items, hand);
		itemCounter.clear();
		itemCounter.put(hand, items.length);
	}
	
	public int getSlots() { return items.length; }
	public int getFilledSlots() { return getSlots() - itemCounter.get(hand); }
	
	public int getCount(Item item) { return itemCounter.get(item); }
	public boolean hasItem(Item item) { return hasItem(item, 1); }
	public boolean hasItem(Item item, int count) { return getCount(item) >= count; }
	
	public Item[] getItems() {
		Item[] items = new Item[this.items.length];
		System.arraycopy(this.items, 0, items, 0, items.length);
		return items;
	}
	
	public boolean addItem(Item item) {
		int openIdx = getFirstMatch(hand);
		if(openIdx < 0) return false; // no empty slots
		
		replaceItemAt(openIdx, item);
		return true;
	}
	public boolean removeItem(Item item) {
		int idx = getFirstMatch(item);
		if(idx < 0) return false; // item not found
		
		replaceItemAt(idx, hand);
		return true;
	}
	
	private int getFirstMatch(Item item) { return getFirstMatch(0, item); }
	private int getFirstMatch(int startIdx, Item item) {
		for(int i = startIdx; i < items.length; i++)
			if(items[i].equals(item))
				return i;
		
		return -1;
	}
	
	private int getLastMatch(Item item) { return getLastMatch(0, item); }
	private int getLastMatch(int startIdx, Item item) {
		for(int i = Math.min(startIdx, items.length-1); i >= 0; i--)
			if(items[i].equals(item))
				return i;
		
		return -1;
	}
	
	Item getItemAt(int idx) {
		checkIndex(idx);
		return items[idx];
	}
	
	Item replaceItemAt(int idx, Item item) {
		checkIndex(idx);
		// okay to replace with hand item.
		
		Item cur = items[idx];
		itemCounter.removeInstance(cur);
		itemCounter.add(item);
		items[idx] = item;
		return cur;
	}
	
	private void checkIndex(int idx) {
		//if(idx >= items.length) throw new IndexOutOfBoundsException("cannot access index " + idx + " of "+slots+"-slot inventory.");
		if(idx >= items.length) throw new IndexOutOfBoundsException("cannot access inventory index " + idx + "; inventory only contains " + items.length + " items.");
	}
	
	
	public String[] save() {
		String[] data = new String[items.length];
		for(int i = 0; i < data.length; i++) {
			data[i] = MyUtils.encodeStringArray(items[i].save());
		}
		
		return data;
	}
	
	public void loadItems(String[] allData) {
		itemCounter.clear();
		for(int i = 0; i < items.length; i++) {
			String[] data = MyUtils.parseLayeredString(allData[i]);
			items[i] = Item.load(data);
			itemCounter.add(items[i]);
		}
	}
	
}
