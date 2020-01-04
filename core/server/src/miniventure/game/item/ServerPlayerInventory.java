package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.network.GameProtocol.InventoryUpdate;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ServerPlayerInventory extends PlayerInventory<ServerItem, ServerItemStack, ServerInventory> {
	
	public ServerPlayerInventory() {
		super(new ServerInventory(PlayerInventory.INV_SIZE));
	}
	
	public String[] save() {
		String[] invData = getInv().save();
		
		String[] allData = new String[EquipmentSlot.values.length + invData.length];
		System.arraycopy(invData, 0, allData, EquipmentSlot.values.length, invData.length);
		for (int i = 0; i < EquipmentSlot.values.length; i++) {
			ServerItem item = equippedItems.get(EquipmentSlot.values[i]);
			allData[i] = item == null ? "null" : MyUtils.encodeStringArray(item.save());
		}
		
		// allData[EquipmentSlot.values.length] = MyUtils.encodeStringArray(getHotbarData());
		
		return allData;
	}
	
	public void loadItems(String[] data, @NotNull Version version) {
		ServerItem[] equipment = new ServerItem[EquipmentSlot.values.length];
		for (int i = 0; i < equipment.length; i++)
			equipment[i] = data[i].equals("null") ? null : ServerItem.load(MyUtils.parseLayeredString(data[i]), version);
		
		int buffer = setEquipment(equipment);
		// setHotbarSlots(MyUtils.parseLayeredString(data[equipment.length]));
		getInv().loadItems(Arrays.copyOfRange(data, equipment.length, data.length), buffer, version);
	}
	
	public InventoryUpdate getUpdate() { return getUpdate(true); }
	public InventoryUpdate getUpdate(boolean includeEquipment) {
		String[][] equipment;
		if(includeEquipment) {
			equipment = new String[EquipmentSlot.values.length][];
			for (int i = 0; i < equipment.length; i++) {
				Item item = equippedItems.get(EquipmentSlot.values[i]);
				equipment[i] = item == null ? null : item.serialize();
			}
		}
		else equipment = null;
		
		return new InventoryUpdate(getInv().serialize(), equipment);
	}
}
