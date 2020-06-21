package miniventure.game.item.inventory;

import java.util.ArrayList;

import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;
import miniventure.game.util.InstanceCounter;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Inventory {
	
	/*
		idea for inventory management:
		- give item stacks id's, that are specific to the inventory but are shared between client and server.
		- new stacks are given new ids
		- if the client moves around their items in their inventory, the server doesn't have to know
			- it doesn't affect the stack ids
		- to reference a certain item/stack, the either end just uses the stack id instead of position
	 */
	
	interface ChangeListener {
		void onInsert(int idx);
		void onRemove(int idx);
		// void onStackEdit(int idx, int delta);
	}
	
	private final int size;
	final ArrayList<Item> uniqueItems;
	final InstanceCounter<Item> itemCounter;
	private int spaceTaken = 0;
	
	// private HashSet<ChangeListener> changeListeners;
	
	public Inventory(int size) {
		this.size = size;
		uniqueItems = new ArrayList<>(size);
		itemCounter = new InstanceCounter<>(size);
		// this.itemClass = itemClass;
		// this.stackClass = stackClass;
		// changeListeners = new HashSet<>();
		reset();
	}
	
	// public void addListener(ChangeListener l) { changeListeners.add(l); }
	// public void removeListener(ChangeListener l) { changeListeners.remove(l); }
	
	/*private void postEvent(ValueAction<ChangeListener> action) {
		for(ChangeListener l: changeListeners)
			action.act(l);
	}
	private void postInsert(int index) {
		postEvent(l -> l.onInsert(index));
	}
	private void postRemove(int index) {
		postEvent(l -> l.onRemove(index));
	}*/
	
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
	
	public int getCount(Item item) { return itemCounter.get(item); }
	public boolean hasItem(Item item) { return hasItem(item, 1); }
	public boolean hasItem(Item item, int count) { return getCount(item) >= count; }
	
	// public synchronized Item[] getUniqueItems() { return uniqueItems.toArray(new Item[0]); }
	
	@Nullable
	public Item getItem(int idx) {
		if(idx < 0 || idx >= uniqueItems.size())
			return null;
		return uniqueItems.get(idx);
	}
	
	@Nullable
	public ItemStack getItemStack(int idx) {
		Item item = getItem(idx);
		return item == null ? null : new ItemStack(item, getCount(item));
	}
	
	/*public ItemStack[] getItemStacks() {
		ItemStack[] stacks = new ItemStack[uniqueItems.size()];
		for(int i = 0; i < stacks.length; i++)
			stacks[i] = getItemStack(i);
		return stacks;
	}*/
	
	public int getIndex(Item item) { return uniqueItems.indexOf(item); }
	
	public boolean moveItem(int oldIdx, int newIdx) {
		if (oldIdx < newIdx)
			newIdx--;
		
		if (oldIdx == newIdx) return true;
		
		if (oldIdx < 0 || newIdx < 0 || oldIdx >= getSlotsTaken() || newIdx >= getSlotsTaken())
			return false; // invalid indices
		
		uniqueItems.add(newIdx, uniqueItems.remove(oldIdx));
		// postRemove(oldIdx);
		// postInsert(newIdx);
		return true;
	}
	
	/*public boolean swapItems(int pos1, int pos2) {
		Item temp = uniqueItems.get(pos1);
		uniqueItems.set(pos1, uniqueItems.get(pos2));
		uniqueItems.set(pos2, temp);
		return true;
	}*/
	
	public boolean addItem(Item item) { return addItem(uniqueItems.size(), item); }
	// TODO after seeing what the current behavior is, check if it makes more sense to not have items slide on an insert, and instead the current item at the insert position would be moved to the end.
	public boolean addItem(int index, Item item) { return addItem(index, item, true); }
	protected boolean addItem(int index, Item item, boolean addSpace) {
		if(addSpace && getSpaceLeft() < 1)
			return false; // not enough space left in inventory.
		
		index = Math.min(uniqueItems.size(), index);
		// add new items to uniqueItems
		if(itemCounter.add(item) == 1) {
			uniqueItems.add(index, item);
			// postInsert(index);
		}
		
		if(addSpace)
			spaceTaken++;
		return true;
	}
	
	public boolean removeItem(Item item) { return removeItem(item, true); }
	protected boolean removeItem(Item item, boolean removeSpace) {
		if(!hasItem(item)) return false;
		
		// remove from uniqueItems if none are left
		if(itemCounter.removeInstance(item) == 0) {
			uniqueItems.remove(item);
			// postRemove(idx);
		}
		
		if(removeSpace)
			spaceTaken--;
		return true;
	}
	
	public int removeItemStack(Item item) { return removeItemStack(getIndex(item)); }
	public int removeItemStack(int idx) {
		if(idx < 0) return 0;
		Item item = uniqueItems.remove(idx);
		int count = itemCounter.remove(item);
		// postRemove(idx);
		spaceTaken -= count;
		return count;
	}
	
	public String[] saveItems() {
		String[] data = new String[uniqueItems.size()];
		for(int i = 0; i < data.length; i++) {
			Item item = uniqueItems.get(i);
			data[i] = ItemStack.save(item, itemCounter.get(item));
		}
		
		return data;
	}
	
	// this expects exactly the output of the save function above.
	public void loadItems(String[] data, @NotNull Version version) { loadItems(data, 0, 0, version); }
	protected void loadItems(String[] data, int dataOffset, int buffer, @NotNull Version version) {
		reset();
		spaceTaken = buffer; // equipped items
		
		for(int i = dataOffset; i < data.length; i++) {
			ItemStack stack = new ItemStack(data[i], version);
			itemCounter.put(stack.item, stack.count);
			spaceTaken += stack.count;
			uniqueItems.add(stack.item);
		}
	}
}
