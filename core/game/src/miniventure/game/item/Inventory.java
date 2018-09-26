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
	
	public Inventory(int size) {
		this.size = size;
		uniqueItems = new ArrayList<>(size);
		itemCounter = new InstanceCounter<>(size);
		reset();
	}
	
	public void reset() {
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
