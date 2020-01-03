package miniventure.game.item;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.util.InstanceCounter;
import miniventure.game.util.Version;
import miniventure.game.util.function.MapFunction;

import org.jetbrains.annotations.NotNull;

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
	final Class<TItem> itemClass;
	final Class<TItemStack> stackClass;
	private int spaceTaken = 0;
	
	public Inventory(int size, Class<TItem> itemClass, Class<TItemStack> stackClass) {
		this.size = size;
		uniqueItems = new ArrayList<>(size);
		itemCounter = new InstanceCounter<>(size);
		this.itemClass = itemClass;
		this.stackClass = stackClass;
		reset();
	}
	
	public void reset() {
		itemCounter.clear();
		uniqueItems.clear();
		spaceTaken = 0;
	}
	
	public int getSpace() { return size; }
	public int getSlotsTaken() { return uniqueItems.size(); }
	public int getSpaceTaken() { return spaceTaken; }
	public int getSpaceLeft() { return getSpace() - spaceTaken; }
	public float getPercentFilled() { return spaceTaken / (float) getSpace(); }
	
	public int getCount(TItem item) { return itemCounter.get(item); }
	public boolean hasItem(TItem item) { return hasItem(item, 1); }
	public boolean hasItem(TItem item, int count) { return getCount(item) >= count; }
	
	// public synchronized TItem[] getUniqueItems() { return uniqueItems.toArray(new TItem[0]); }
	
	public TItem getItem(int idx) {
		if(idx < 0 || idx >= uniqueItems.size())
			return null;
		return uniqueItems.get(idx);
	}
	
	public TItemStack getItemStack(int idx) {
		TItem item = getItem(idx);
		return getItemStack(item, getCount(item));
	}
	
	private TItemStack getItemStack(TItem item, int count) {
		try {
			return stackClass.getConstructor(itemClass, int.class).newInstance(item, count);
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Error creating item stack instance", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public TItemStack[] getItemStacks() {
		TItemStack[] stacks = (TItemStack[]) Array.newInstance(stackClass, uniqueItems.size());
		for(int i = 0; i < stacks.length; i++)
			stacks[i] = getItemStack(i);
		return stacks;
	}
	
	int getIndex(TItem item) { return uniqueItems.indexOf(item); }
	
	public boolean moveItem(int oldIdx, int newIdx) {
		if (oldIdx < newIdx)
			newIdx--;
		
		if (oldIdx == newIdx) return true;
		
		if (oldIdx < 0 || newIdx < 0 || oldIdx >= getSlotsTaken() || newIdx >= getSlotsTaken())
			return false; // invalid indices
		
		uniqueItems.add(newIdx, uniqueItems.remove(oldIdx));
		return true;
	}
	
	public boolean addItem(TItem item) { return addItem(uniqueItems.size(), item); }
	public boolean addItem(int index, TItem item) { return addItem(index, item, true); }
	boolean addItem(int index, TItem item, boolean addSpace) {
		if(addSpace && getSpaceLeft() < 1)
			return false; // not enough space left in inventory.
		
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1)
			uniqueItems.add(Math.min(uniqueItems.size(), index), item);
		
		if(addSpace)
			spaceTaken++;
		return true;
	}
	
	void addItem(TItem item, int count) {
		uniqueItems.add(item);
		itemCounter.put(item, count);
		spaceTaken += count;
	}
	
	public boolean removeItem(TItem item) { return removeItem(item, true); }
	boolean removeItem(TItem item, boolean removeSpace) {
		if(!hasItem(item)) return false;
		
		// remove from uniqueItems if none are left
		if(itemCounter.removeInstance(item) == 0)
			uniqueItems.remove(item);
		
		if(removeSpace)
			spaceTaken--;
		return true;
	}
	
	public int removeItemStack(TItem item) {
		if(item == null) return 0;
		int count = getCount(item);
		if(count == 0) return 0;
		itemCounter.remove(item);
		uniqueItems.remove(item);
		spaceTaken -= count;
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public void setItems(TItemStack[] items, int buffer) {
		reset();
		spaceTaken = buffer; // equipped items
		for(TItemStack stack: items) {
			itemCounter.put((TItem) stack.item, stack.count);
			spaceTaken += stack.count;
			uniqueItems.add((TItem) stack.item);
		}
	}
}
