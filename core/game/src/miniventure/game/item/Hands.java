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
	
	public boolean removeItem(Item item) {
		return replaceItem(item, new HandItem());
	}
	
	boolean replaceItem(Item oldItem, Item newItem) {
		for(int i = 0; i < hotbarItems.length; i++) {
			if(hotbarItems[i].equals(oldItem)) {
				hotbarItems[i] = newItem;
				return true;
			}
		}
		return false;
	}
	
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
	
	
	public void setSelection(int idx) { selection = idx; }
	public int getSelection() { return selection; }
	
	/** @noinspection BooleanMethodIsAlwaysInverted*/
	public boolean hasUsableItem() { return !(getSelectedItem().isUsed()); }
	
	@NotNull
	public Item getSelectedItem() { return hotbarItems[selection]; }
	
	
	public String[] save() {
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
