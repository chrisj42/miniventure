package miniventure.game.item;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import miniventure.game.util.InstanceCounter;
import miniventure.game.util.MyUtils;

public abstract class Inventory<TItem extends Item, TItemStack extends ItemStack> {
	
	/*
		idea for inventory management:
		- give item stacks id's, that are specific to the inventory but are shared between client and server.
		- new stacks are given new ids
		- if the client moves around their items in their inventory, the server doesn't have to know
			- it doesn't affect the stack ids
		- to reference a certain item/stack, the either end just uses the stack id instead of position
	 */
	
	private final int size;
	final ArrayList<TItem> uniqueItems;
	final InstanceCounter<TItem> itemCounter;
	private final Class<TItem> itemClass;
	private final Class<TItemStack> stackClass;
	int spaceTaken = 0;
	
	public Inventory(int size, Class<TItem> itemClass, Class<TItemStack> stackClass) {
		this.size = size;
		uniqueItems = new ArrayList<>(size);
		itemCounter = new InstanceCounter<>(size);
		this.itemClass = itemClass;
		this.stackClass = stackClass;
		reset();
	}
	
	public synchronized void reset() {
		itemCounter.clear();
		uniqueItems.clear();
		spaceTaken = 0;
	}
	
	public int getSpace() { return size; }
	public int getSpaceLeft() { return getSpace() - spaceTaken; }
	public int getSlotsTaken() { return uniqueItems.size(); }
	public float getPercentFilled() { return spaceTaken / (float) getSpace(); }
	
	public synchronized int getCount(TItem item) { return itemCounter.get(item); }
	public boolean hasItem(TItem item) { return hasItem(item, 1); }
	public boolean hasItem(TItem item, int count) { return getCount(item) >= count; }
	
	// public synchronized TItem[] getUniqueItems() { return uniqueItems.toArray(new TItem[0]); }
	
	public synchronized TItem getItem(int idx) {
		if(idx < 0 || idx >= uniqueItems.size())
			return null;
		return uniqueItems.get(idx);
	}
	
	public synchronized TItemStack getItemStack(int idx) {
		try {
			return stackClass.getConstructor(itemClass, int.class).newInstance(getItem(idx), getCount(getItem(idx)));
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized TItemStack[] getItemStacks() {
		TItemStack[] stacks = (TItemStack[]) Array.newInstance(stackClass, uniqueItems.size());
		for(int i = 0; i < stacks.length; i++)
			stacks[i] = getItemStack(i);
		return stacks;
	}
	
	public synchronized int getIndex(TItem item) { return uniqueItems.indexOf(item); }
	
	public synchronized boolean moveItem(int oldIdx, int newIdx) {
		if (oldIdx < newIdx)
			newIdx--;
		
		if (oldIdx == newIdx) return true;
		
		if (oldIdx < 0 || newIdx < 0 || oldIdx >= getSlotsTaken() || newIdx >= getSlotsTaken())
			return false; // invalid indices
		
		uniqueItems.add(newIdx, uniqueItems.remove(oldIdx));
		return true;
	}
	
	public boolean addItem(TItem item) { return addItem(uniqueItems.size(), item); }
	public synchronized boolean addItem(int index, TItem item) {
		if(getSpaceLeft() < 1)
			return false; // not enough space left in inventory.
		
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1)
			uniqueItems.add(Math.min(uniqueItems.size(), index), item);
		
		spaceTaken++;
		return true;
	}
	
	public synchronized boolean removeItem(TItem item) {
		if(!hasItem(item)) return false;
		
		// remove from uniqueItems if none are left
		if(itemCounter.removeInstance(item) == 0)
			uniqueItems.remove(item);
		
		spaceTaken--;
		return true;
	}
	
	public synchronized int removeItemStack(TItem item) {
		if(item == null) return 0;
		int count = getCount(item);
		if(count == 0) return 0;
		itemCounter.remove(item);
		uniqueItems.remove(item);
		spaceTaken -= count;
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void updateItems(String[][] data) {
		reset();
		for(String[] stackData: data) {
			// String[] data = MyUtils.parseLayeredString(stackData);
			TItemStack stack = parseStack(stackData);
			itemCounter.put((TItem) stack.getItem(), stack.count);
			spaceTaken += stack.count;
			uniqueItems.add((TItem) stack.getItem());
		}
	}
	
	abstract TItemStack parseStack(String[] data);
}
