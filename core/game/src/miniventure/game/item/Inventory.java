package miniventure.game.item;

import java.util.ArrayList;

import miniventure.game.util.InstanceCounter;
import miniventure.game.util.MyUtils;

public class Inventory {
	
	private static final HandItem hand = new HandItem(); // ref var so it isn't constantly re-instantiated.
	
	private final int size;
	private final ArrayList<Item> uniqueItems;
	private final InstanceCounter<Item> itemCounter;
	private int spaceTaken = 0;
	// private Item[] items; // mainly for the optimization of getItems
	
	public Inventory(int size) {
		// items = new Item[size];
		this.size = size;
		uniqueItems = new ArrayList<>(size);
		itemCounter = new InstanceCounter<>(size);
		reset();
	}
	
	public void reset() {
		// Arrays.fill(items, hand);
		itemCounter.clear();
		uniqueItems.clear();
		itemCounter.put(hand, size);
		// note that uniqueItems is not given a HandItem.
		spaceTaken = 0;
	}
	
	public int getSlots() { return size; }
	public int getSpaceLeft() { return getSlots() - spaceTaken; }
	public int getSlotsTaken() { return uniqueItems.size(); }
	
	public int getCount(Item item) { return itemCounter.get(item); }
	public boolean hasItem(Item item) { return hasItem(item, 1); }
	public boolean hasItem(Item item, int count) { return getCount(item) >= count; }
	
	public Item[] getUniqueItems() { return uniqueItems.toArray(new Item[0]); }
	
	public boolean addItem(Item item) {
		if(getSpaceLeft() < item.getSpaceUsage())
			return false; // not enough space left in inventory.
		
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1 && !(item instanceof HandItem))
			uniqueItems.add(0, item); // don't put any HandItems here.
		
		spaceTaken += item.getSpaceUsage();
		return true;
	}
	
	public boolean removeItem(Item item) {
		if(!hasItem(item)) return false;
		
		// remove from uniqueItems if none are left
		if(itemCounter.removeInstance(item) == 0 && !(item instanceof HandItem))
			uniqueItems.remove(item); // there shouldn't be any HandItems in it anyway.
		
		spaceTaken -= item.getSpaceUsage();
		return true;
	}
	
	/*private int getFirstMatch(Item item) { return getFirstMatch(0, item); }
	private int getFirstMatch(int startIdx, Item item) {
		for(int i = startIdx; i < size; i++)
			if(items[i].equals(item))
				return i;
		
		return -1;
	}*/
	
	/*private int getLastMatch(Item item) { return getLastMatch(0, item); }
	private int getLastMatch(int startIdx, Item item) {
		for(int i = Math.min(startIdx, size-1); i >= 0; i--)
			if(items[i].equals(item))
				return i;
		
		return -1;
	}*/
	
	Item getUniqueItemAt(int idx) {
		if(idx >= uniqueItems.size())
			throw new IndexOutOfBoundsException("cannot access uniqueItems index " + idx + "; though inventory can contain " + size + " items, unique item count is "+uniqueItems.size()+".");
		
		return uniqueItems.get(idx);
	}
	
	/*Item replaceUniqueItemAt(int idx, Item item) {
		checkIndex(idx);
		// okay to replace with hand item.
		if(item == null)
			item = hand;
		
		Item cur = uniqueItems.get(idx);
		if(itemCounter.removeInstance(cur) == 0 && !(cur instanceof HandItem))
			uniqueItems.remove(cur); // remove from here if none are left; there shouldn't be any HandItems here.
		
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1 && !(item instanceof HandItem))
			uniqueItems.add(0, item); // don't put any HandItems here.
		
		// items[idx] = item;
		uniqueItems.set(idx, item);
		return cur;
	}*/
	
	/*private void checkIndex(int idx) {
		
	}*/
	
	
	public String[] save() {
		String[] data = new String[uniqueItems.size()];
		for(int i = 0; i < data.length; i++) {
			Item item = uniqueItems.get(i);
			data[i] = MyUtils.encodeStringArray(ItemStack.save(item, itemCounter.get(item)));
		}
		
		return data;
	}
	
	// this expects exactly the output of the save function above.
	public void loadItems(String[] allData) {
		reset();
		int off = 0;
		for(String stackData: allData) {
			String[] data = MyUtils.parseLayeredString(stackData);
			ItemStack stack = ItemStack.load(data);
			off += stack.count;
			itemCounter.put(stack.item, stack.count);
			spaceTaken += stack.item.getSpaceUsage() * stack.count;
			uniqueItems.add(stack.item);
		}
		itemCounter.put(hand, size - off);
	}
	
}
