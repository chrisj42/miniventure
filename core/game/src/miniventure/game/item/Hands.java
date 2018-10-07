package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.util.MyUtils;

import org.jetbrains.annotations.NotNull;

public class Hands {
	
	public static final int HOTBAR_SIZE = 5;
	
	// holds the items in the player's hotbar.
	
	private Inventory inventory;
	private Item[] hotbarItems;
	private int selection;
	
	Hands(@NotNull Inventory inventory) {
		this.inventory = inventory;
		hotbarItems = new Item[HOTBAR_SIZE];
		reset();
	}
	
	public void reset() { Arrays.fill(hotbarItems, new HandItem()); }
	
	protected Inventory getInv() { return inventory; }
	
	public boolean addItem(Item item) { return addItem(item, 0); }
	public boolean addItem(Item item, int fromIndex) {
		if(item instanceof HandItem)
			return false; // just kinda ignore these
		
		// check for given item while also finding the first open slot starting from "fromIndex" (and looping around if necessary)
		int firstOpen = -1;
		for(int i = 0; i < hotbarItems.length; i++) {
			int idx = (i+fromIndex) % hotbarItems.length;
			if(hotbarItems[idx].equals(item))
				return false; // item is already in hotbar
			else if(firstOpen < 0 && hotbarItems[idx] instanceof HandItem)
				firstOpen = idx; // finds first open slot
		}
		
		if(firstOpen < 0) // no open hotbar slots
			return false;
		
		// open slot found; setting to given item.
		hotbarItems[firstOpen] = item;
		return true;
	}
	
	public boolean removeItem(Item item) { return replaceItem(item, new HandItem()); }
	public Item removeItem(int idx) {
		Item prevItem = hotbarItems[idx];
		if(!(prevItem instanceof HandItem))
			hotbarItems[idx] = new HandItem();
		return prevItem;
	}
	
	boolean replaceItem(Item oldItem, @NotNull Item newItem) {
		int curNewIdx = -1;
		boolean foundOld = false;
		final boolean matchNew = !(newItem instanceof HandItem);
		
		for(int i = 0; i < hotbarItems.length; i++) {
			if(matchNew && hotbarItems[i].equals(newItem))
				curNewIdx = i;
			if(hotbarItems[i].equals(oldItem)) {
				hotbarItems[i] = newItem;
				if(!matchNew) return true; // we can short-circuit here if we don't care about keeping the new item unique.
				foundOld = true;
			}
		}
		
		if(!foundOld)
			return false; // take no action if the target item wasn't found in the hotbar.
		
		if(curNewIdx > 0) // new item already existed; replace its previous position with a HandItem.
			hotbarItems[curNewIdx] = new HandItem();
		
		return true;
	}
	
	Item replaceItem(int oldIdx, Item newItem) {
		if(hotbarItems[oldIdx].equals(newItem))
			return newItem; // makes you not have to go through all the stuff below, for the same result.
		
		int curNewIdx = -1;
		for(int i = 1; i < hotbarItems.length; i++) {
			int idx = (i + oldIdx) % hotbarItems.length;
			if(hotbarItems[idx].equals(newItem)) {
				curNewIdx = idx;
				break;
			}
		}
		
		if(curNewIdx > 0) // newItem already exists in the hotbar; replace the old position with a hand
			hotbarItems[curNewIdx] = new HandItem();
		
		// now put the new item in at the given index, and return what was there before.
		Item oldItem = hotbarItems[oldIdx];
		hotbarItems[oldIdx] = newItem;
		return oldItem;
	}
	
	boolean containsItem(Item item) {
		for(Item i: hotbarItems)
			if(i.equals(item))
				return true;
		
		return false;
	}
	
	@NotNull
	Item getItem(int idx) { return hotbarItems[idx]; }
	
	public void setSelection(int idx) { selection = idx; }
	public int getSelection() { return selection; }
	
	/** @noinspection BooleanMethodIsAlwaysInverted*/
	public boolean hasUsableItem() { return !(getSelectedItem().isUsed()); }
	
	@NotNull
	public Item getSelectedItem() { return hotbarItems[selection]; }
	
	// check each slot and remove any that points to an item not in the inventory. Return true if an update occurs.
	public boolean validate() {
		boolean updated = false;
		for(int i = 0; i < hotbarItems.length; i++) {
			if(!inventory.hasItem(hotbarItems[i])) {
				hotbarItems[i] = new HandItem();
				updated = true;
			}
		}
		
		return updated;
	}
	
	public String[] save() {
		// make sure we don't save out-of-date information.
		validate();
		
		String[] data = new String[hotbarItems.length];
		for(int i = 0; i < hotbarItems.length; i++)
			data[i] = MyUtils.encodeStringArray(hotbarItems[i].save());
		return data;
	}
	
	public void loadItemShortcuts(String[] data) {
		for(int i = 0; i < hotbarItems.length; i++)
			hotbarItems[i] = Item.load(MyUtils.parseLayeredString(data[i]));
	}
}
