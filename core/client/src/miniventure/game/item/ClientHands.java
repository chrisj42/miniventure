package miniventure.game.item;

import java.awt.Color;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;

public class ClientHands extends Hands {
	
	private ItemSelectionTable hotbarTable;
	
	public ClientHands(Inventory inventory) {
		super(inventory);
		
		ItemSlot[] slots = new ItemSlot[getSlots()];
		for(int i = 0; i < slots.length; i++)
			slots[i] = new HotbarSlot(i, inventory);
		
		// hotbarTable = new ItemSelectionTable(slots, ItemSlot.HEIGHT);
		// hotbarTable.pack();
	}
	
	// public ItemSelectionTable getHotbarTable() { return hotbarTable; }
	
	@Override
	public void setSelection(int idx) {
		super.setSelection(idx);
		hotbarTable.setSelection(idx);
	}
	
	@Override
	Item replaceItemAt(int idx, Item item) {
		Item prev = super.replaceItemAt(idx, item);
		hotbarTable.update(idx, item);
		return prev;
	}
	
	@Override
	public void loadItems(String[] allData) {
		super.loadItems(allData);
		for(int i = 0; i < getSlots(); i++)
			hotbarTable.update(i, getItemAt(i));
	}
	
	public void dropInvItems(Item item, boolean all) {
		if(!(item instanceof HandItem)) {
			int count = all ? getCount(item) : 1;
			for(int i = 0; i < count; i++)
				removeItem(item);
			ClientCore.getClient().send(new ItemDropRequest(new ItemStack(item, count)));
		}
	}
	
	private class HotbarSlot extends ItemStackSlot {
		private final Inventory inventory;
		
		public HotbarSlot(int idx, Inventory inventory) {
			super(idx, false, getItemAt(idx), 1, Color.DARK_GRAY);
			this.inventory = inventory;
			// FIXME I can't seem to prevent clicks on the hotbar from propagating to the game screen and moving the player...
			
		}
		
		@Override
		// the count displayed shows how many of that item are in the inventory.
		public int getCount() {
			//if(ClientCore.getScreen() instanceof InventoryScreen)
			//	return 0; // don't show counts when inventory screen is open..?
			if(inventory.getCount(this.getItem()) == 0)
				return 0;
			
			return ClientHands.this.getCount(getItem()); // includes both inventory and hotbar
		}
	}
	
	public int getSlots() { return 0; }
}
