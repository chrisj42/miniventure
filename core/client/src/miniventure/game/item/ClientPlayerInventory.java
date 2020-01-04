package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.network.GameProtocol.EquipRequest;
import miniventure.game.network.GameProtocol.ItemDropRequest;

import org.jetbrains.annotations.NotNull;

public class ClientPlayerInventory extends PlayerInventory<Item, ItemStack, ClientInventory> {
	
	// private final ClientInventory inv;
	// private final ClientInventory hotbar;
	private int selection;
	
	public ClientPlayerInventory() {
		super(new ClientInventory(PlayerInventory.INV_SIZE));
	}
	/*private ClientPlayerInventory(ClientInventory inv, ClientInventory hotbar) {
		super(inv, hotbar);
		this.inv = inv;
		this.hotbar = hotbar;
	}*/
	
	public void dropInvItems(boolean all) {
		dropInvItems(selection, all);
	}
	private void dropInvItems(int index, boolean all) {
		if(index < 0) return;
		ClientCore.getClient().send(new ItemDropRequest(index, all));
		if(all)
			getInv().removeItemStack(index);
		else
			getInv().removeItem(getInv().getItem(index));
	}
	
	public void setSelection(int idx) {
		idx %= getInv().getSlotsTaken();
		if(idx < 0)
			idx += getInv().getSlotsTaken();
		
		selection = idx;
	}
	public int getSelection() { return selection; }
	
	public ItemStack getSelectedItem() {
		return getInv().getItemStack(getSelection());
	}
	
	/*void linkInventory(InventoryPanel invPanel, InventoryPanel hotbarPanel) {
		invPanel.setInventory(inv);
		hotbarPanel.setInventory(hotbar);
	}*/
	
	@Override
	public boolean equipItem(@NotNull EquipmentSlot equipmentSlot, int index) {
		boolean success = super.equipItem(equipmentSlot, index);
		if(success)
			ClientCore.getClient().send(new EquipRequest(equipmentSlot, index, true));
		return success;
	}
	
	@Override
	public boolean unequipItem(@NotNull EquipmentSlot equipmentSlot, int index) {
		boolean success = super.equipItem(equipmentSlot, index);
		if(success)
			ClientCore.getClient().send(new EquipRequest(equipmentSlot, index, false));
		return success;
	}
	
	public void updateItems(String[][] inventoryData, String[][] equipmentData/*, String[] hotbarData*/) {
		if(equipmentData != null) {
			Item[] equipment = new Item[EquipmentSlot.values.length];
			for (int i = 0; i < equipment.length; i++)
				equipment[i] = ClientItem.deserialize(equipmentData[i]);
			
			setEquipment(equipment);
		}
		// setHotbarSlots(hotbarData);
		
		getInv().updateItems(inventoryData, equippedItems.size());
	}
}
