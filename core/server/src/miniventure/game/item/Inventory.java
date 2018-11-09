package miniventure.game.item;

import java.util.ArrayList;

import miniventure.game.util.InstanceCounter;
import miniventure.game.util.MyUtils;

public class Inventory {
	
	// TODO player inventory should be managed (and tracked) only from the server. Right before opening the inventory screen, the client asks for an inventory update. It displays a waiting message until the update is received. Leaving the inventory screen before the update is recieved will cause the client to ignore the update. A request id is attached to the request so the client doesn't pay attention to an old response.
	// any edits to the inventory/hotbar in the inventory screen aren't sent until the screen is closed. When the client edits the inventory it tracks the changes made, and then sends over the changes (idx # moved to idx #, removed # from stack idx #, all new hotbar indices). the server maps these to their items, and then applies the changes. Note that item removals are acted on immediately.
	
	private static final HandItem hand = HandItem.hand; // ref var so it isn't constantly re-instantiated.
	
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
		itemCounter.put(hand, size);
		// note that uniqueItems is not given a HandItem.
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
	
	int getIndex(ServerItem item) { return uniqueItems.indexOf(item); }
	
	public boolean addItem(ServerItem item) {
		if(getSpaceLeft() < item.getSpaceUsage())
			return false; // not enough space left in inventory.
		
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1 && !(item instanceof HandItem))
			uniqueItems.add(0, item); // don't put any HandItems here.
		
		spaceTaken += item.getSpaceUsage();
		return true;
	}
	
	public boolean removeItem(ServerItem item) {
		if(!hasItem(item)) return false;
		
		// remove from uniqueItems if none are left
		if(itemCounter.removeInstance(item) == 0 && !(item instanceof HandItem))
			uniqueItems.remove(item); // there shouldn't be any HandItems in it anyway.
		
		itemCounter.add(hand);
		spaceTaken -= item.getSpaceUsage();
		return true;
	}
	
	public int removeItemStack(ServerItem item) {
		if(item == null || item instanceof HandItem) return 0;
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
		int off = 0;
		for(String stackData: allData) {
			String[] data = MyUtils.parseLayeredString(stackData);
			ServerItemStack stack = ServerItemStack.load(data);
			off += stack.count;
			itemCounter.put(stack.item, stack.count);
			spaceTaken += stack.item.getSpaceUsage() * stack.count;
			uniqueItems.add(stack.item);
		}
		itemCounter.put(hand, size - off); // FIXME stop counting hand items
	}
	
}
