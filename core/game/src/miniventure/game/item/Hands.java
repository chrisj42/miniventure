package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public class Hands extends Inventory {
	
	// holds the items in the player's hotbar.
	
	private int selection;
	private Inventory inventory;
	
	Hands(@NotNull Inventory inventory) {
		super(3);
		this.inventory = inventory;
	}
	
	@Override
	public boolean addItem(Item item) {
		if(getCount(item) == 0 || !inventory.addItem(item))
			return super.addItem(item);
		else
			return true; // was added to inventory instead
	}
	
	public void setSelection(int idx) { selection = idx; }
	public int getSelection() { return selection; }
	
	void swapItem(Inventory inv, int invIdx, int hotbarIdx) {
		Item item = inv.replaceItemAt(invIdx, getItemAt(hotbarIdx));
		replaceItemAt(hotbarIdx, item);
	}
	
	public void resetItemUsage() {}
	
	public boolean hasUsableItem() { return !(getSelectedItem().isUsed()); }
	
	@NotNull
	public Item getSelectedItem() { return getItemAt(selection); }
}
