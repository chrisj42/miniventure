package miniventure.game.item;

import miniventure.game.client.ClientCore;
import miniventure.game.network.GameProtocol.EquipRequest;

import org.jetbrains.annotations.NotNull;

public class ClientPlayerInventory extends PlayerInventory<Item, ItemStack, ClientInventory> {
	
	public ClientPlayerInventory() {
		super(new ClientInventory(PlayerInventory.INV_SIZE));
	}
	
	@Override
	boolean equipItem(@NotNull EquipmentSlot equipmentSlot, int invIdx) {
		boolean success = super.equipItem(equipmentSlot, invIdx);
		if(success)
			ClientCore.getClient().send(new EquipRequest(equipmentSlot, invIdx));
		return success;
	}
	
	public void updateItems(String[][] inventoryData, String[][] equipmentData) {
		Item[] equipment = new Item[EquipmentSlot.values.length];
		for (int i = 0; i < equipment.length; i++)
			equipment[i] = ClientItem.deserialize(equipmentData[i]);
		
		int buffer = setEquipment(equipment);
		
		getInv().updateItems(inventoryData, buffer);
	}
}
