package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.util.MyUtils;

import org.jetbrains.annotations.NotNull;

public class Hands {
	
	// holds the items in the player's hotbar.
	
	private static final int MAX_ITEM_COUNT = 5;
	
	private static final HandItem hand = new HandItem(); // ref var so it isn't constantly re-instantiated.
	
	private int selection;
	private Inventory playerInventory;
	
	// a hashmap really isn't worth it.
	// private final HashMap<Item, Integer> itemPositions = new HashMap<>();
	private Item[] slots;
	
	Hands(@NotNull Inventory playerInventory) {
		this.playerInventory = playerInventory;
		slots = new Item[MAX_ITEM_COUNT];
		reset();
	}
	
	public void reset() {
		Arrays.fill(slots, hand);
	}
	
	public int getSize() { return slots.length; }
	
	public boolean addItem(Item item) {
		if(item == null) return false;
		
		for(int i = 0; i < slots.length; i++) {
			if(slots[i].equals(item))
				return false;
			if(slots[i].equals(hand)) {
				slots[i] = item;
				return true;
			}
		}
		
		return false; // no empty slots
	}
	
	public boolean removeItem(Item item) {
		for(int i = 0; i < slots.length; i++) {
			if(slots[i].equals(item)) {
				slots[i] = hand;
				return true;
			}
		}
		return false;
	}
	
	
	public String[] save() {
		String[] data = new String[getSize()];
		for(int i = 0; i < data.length; i++)
			data[i] = MyUtils.encodeStringArray(slots[i].save());
		
		return data;
	}
	
	public void loadItems(String[] allData) {
		reset();
		for(String data: allData)
			addItem(Item.load(MyUtils.parseLayeredString(data)));
	}
	
	
	public void setSelection(int idx) { selection = idx; }
	public int getSelection() { return selection; }
	
	public void resetItemUsage() {}
	
	public boolean hasUsableItem() { return !(getSelectedItem().isUsed()); }
	
	@NotNull
	public Item getSelectedItem() { return slots[selection]; }
}
