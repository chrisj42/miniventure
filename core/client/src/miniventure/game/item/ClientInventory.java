package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.network.GameProtocol.ItemDropRequest;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ClientInventory extends Inventory<Item, ItemStack> {
	
	// the fact that there is items in the hotbar is mostly for rendering. Most of these methods are for rendering.
	
	// item equipping is handled here. 
	
	private int selection;
	
	public ClientInventory(int size) {
		super(size, Item.class, ItemStack.class);
	}
	
	public void dropInvItems(boolean all) {
		ClientCore.getClient().send(new ItemDropRequest(selection, all));
		if(all)
			removeItemStack(getItem(selection));
		else
			removeItem(getItem(selection));
	}
	
	public void setSelection(int idx) {
		idx %= getSlotsTaken();
		if(idx < 0)
			idx += getSlotsTaken();
		
		selection = idx;
	}
	public int getSelection() { return selection; }
	
	public ItemStack getSelectedItem() {
		return getItemStack(getSelection());
	}
	
	// called by the inventory overlay when an 
	/*public synchronized boolean equip(@NotNull EquipmentSlot equipmentType, int idx, @Nullable Item item) {
		Item[] slots = equippedItems.get(equipmentType);
		Item cur = slots[idx];
		if(item == null || suppressItem(item)) {
			slots[idx] = item;
			if(cur != null)
				unsuppressItem(cur);
		}
	}*/
	
	public void updateItems(String[][] data) { updateItems(data, 0); }
	public void updateItems(String[][] data, int buffer) {
		ItemStack[] stacks = new ItemStack[data.length];
		for (int i = 0; i < stacks.length; i++)
			stacks[i] = ClientItem.deserializeStack(data[i]);
		
		setItems(stacks, buffer);
	}
}
