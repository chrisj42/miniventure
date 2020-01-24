package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.network.GameProtocol.EquipRequest;
import miniventure.game.network.GameProtocol.ItemDropRequest;
import miniventure.game.network.GameProtocol.SerialItem;
import miniventure.game.network.GameProtocol.SerialItemStack;
import miniventure.game.util.MyUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientPlayerInventory extends PlayerInventory<Item, ItemStack, ClientInventory> {
	
	// private final ClientInventory inv;
	// private final ClientInventory hotbar;
	private int selection = -1; // this is reserved for an empty hand.
	
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
		setSelection(Math.min(getInv().getSlotsTaken() - 1, selection));
	}
	
	public void setSelection(int idx) {
		int slots = getInv().getSlotsTaken();
		// add one to buffer for the -1 option
		selection = MyUtils.wrapIndex(idx+1, slots+1) - 1;
	}
	public int getSelection() { return selection; }
	
	@Nullable
	public ItemStack getSelectedItem() {
		return getInv().getItemStack(getSelection());
	}
	
	/*void linkInventory(InventoryPanel invPanel, InventoryPanel hotbarPanel) {
		invPanel.setInventory(inv);
		hotbarPanel.setInventory(hotbar);
	}*/
	
	@Override
	public boolean equipItem(@NotNull EquipmentType equipmentSlot, int index) {
		boolean success = super.equipItem(equipmentSlot, index);
		if(success)
			ClientCore.getClient().send(new EquipRequest(equipmentSlot, index, true));
		return success;
	}
	
	@Override
	public boolean unequipItem(@NotNull EquipmentType equipmentSlot, int index) {
		boolean success = super.unequipItem(equipmentSlot, index);
		if(success)
			ClientCore.getClient().send(new EquipRequest(equipmentSlot, index, false));
		return success;
	}
	
	public void updateItems(SerialItemStack[] inventoryData, SerialItem[] equipmentData) {
		if(equipmentData != null) {
			Item[] equipment = new Item[EquipmentType.values.length];
			for (int i = 0; i < equipment.length; i++)
				equipment[i] = equipmentData[i] == null ? null : new ClientItem(equipmentData[i]);
			
			setEquipment(equipment);
		}
		
		getInv().updateItems(inventoryData, equippedItems.size());
		setSelection(Math.min(getInv().getSlotsTaken() - 1, selection));
	}
}
