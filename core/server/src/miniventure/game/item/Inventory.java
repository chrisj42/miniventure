package miniventure.game.item;

import java.util.ArrayList;

import miniventure.game.util.InstanceCounter;
import miniventure.game.util.MyUtils;

public class Inventory {
	
	private final int size;
	private final ArrayList<ServerItem> uniqueItems;
	private final InstanceCounter<ServerItem> itemCounter;
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
		spaceTaken = 0;
	}
	
	public int getSpace() { return size; }
	public int getSpaceLeft() { return getSpace() - spaceTaken; }
	public int getSlotsTaken() { return uniqueItems.size(); }
	public float getPercentFilled() { return spaceTaken / (float) getSpace(); }
	
	public int getCount(ServerItem item) { return itemCounter.get(item); }
	public boolean hasItem(ServerItem item) { return hasItem(item, 1); }
	public boolean hasItem(ServerItem item, int count) { return getCount(item) >= count; }
	
	public ServerItem[] getUniqueItems() { return uniqueItems.toArray(new ServerItem[0]); }
	
	public ServerItem getItem(int idx) { return uniqueItems.get(idx); }
	public ServerItemStack getItemStack(int idx) { return new ServerItemStack(getItem(idx), getCount(getItem(idx))); }
	
	public ServerItemStack[] getItemStacks() {
		ServerItemStack[] stacks = new ServerItemStack[uniqueItems.size()];
		for(int i = 0; i < stacks.length; i++)
			stacks[i] = getItemStack(i);
		return stacks;
	}
	
	public int getIndex(ServerItem item) { return uniqueItems.indexOf(item); }
	
	public boolean addItem(ServerItem item) {
		if(getSpaceLeft() < item.getSpaceUsage())
			return false; // not enough space left in inventory.
		
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1)
			uniqueItems.add(0, item);
		
		spaceTaken += item.getSpaceUsage();
		return true;
	}
	
	public boolean removeItem(ServerItem item) {
		if(!hasItem(item)) return false;
		
		// remove from uniqueItems if none are left
		if(itemCounter.removeInstance(item) == 0)
			uniqueItems.remove(item);
		
		spaceTaken -= item.getSpaceUsage();
		return true;
	}
	
	public int removeItemStack(ServerItem item) {
		if(item == null) return 0;
		int count = getCount(item);
		if(count == 0) return 0;
		itemCounter.remove(item);
		uniqueItems.remove(item);
		spaceTaken -= item.getSpaceUsage() * count;
		return count;
	}
	
	public String[][] serialize() {
		String[][] data = new String[uniqueItems.size()][];
		for(int i = 0; i < data.length; i++) {
			ServerItem item = uniqueItems.get(i);
			data[i] = ItemStack.serialize(item, itemCounter.get(item));
		}
		
		return data;
	}
	
	public String[] save() {
		String[] data = new String[uniqueItems.size()];
		for(int i = 0; i < data.length; i++) {
			ServerItem item = uniqueItems.get(i);
			data[i] = MyUtils.encodeStringArray(ServerItemStack.save(item, itemCounter.get(item)));
		}
		
		return data;
	}
	
	// this expects exactly the output of the save function above.
	public void loadItems(String[] allData) {
		reset();
		for(String stackData: allData) {
			String[] data = MyUtils.parseLayeredString(stackData);
			ServerItemStack stack = ServerItemStack.load(data);
			itemCounter.put(stack.item, stack.count);
			spaceTaken += stack.item.getSpaceUsage() * stack.count;
			uniqueItems.add(stack.item);
		}
	}
	
}
