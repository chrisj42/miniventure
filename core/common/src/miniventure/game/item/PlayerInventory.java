package miniventure.game.item;

import java.util.EnumMap;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public abstract class PlayerInventory<TItem extends Item, TItemStack extends ItemStack, TInv extends Inventory<TItem, TItemStack>> {
	
	public static final int INV_SIZE = 50;
	public static final int HOTBAR_SIZE = 5;
	
	// server player inventory and client player inventory; each extends their side of the inventory class, as well as implementing this; this in turn extends the inventory interface.
	
	// player inventories have a hotbar which can contain empty spaces, as well as the ability to suppress certain items from showing up while still counting them towards the quota.
	
	@NotNull private TInv inventory;
	
	final EnumMap<EquipmentSlot, TItem> equippedItems;
	
	PlayerInventory(@NotNull TInv inventory) {
		this.inventory = inventory;
		
		equippedItems = new EnumMap<>(EquipmentSlot.class);
	}
	
	@NotNull
	public TInv getInv() { return inventory; }
	
	public void reset() {
		inventory.reset();
		equippedItems.clear();
	}
	
	/**
	 * Equips or unequips an item to an equipment slot, or swaps one equipped item for another one. This involves:
	 * - checking if the item is allowed into the given slot
	 * - swapping it with what is currently there
	 * - executing anything related to the specific slot/item being equipped/unequipped
	 * @param equipmentSlot the type of equipment slot we want to edit
	 * @param invIdx the index of the item to be equipped, or -1 to unequip the current item
	 * @return if the item was equipped / unequipped successfully
	 */
	boolean equipItem(@NotNull EquipmentSlot equipmentSlot, int invIdx) {
		TItem item = invIdx < 0 ? null : inventory.getItem(invIdx);
		
		// ensure the given item is allowed to be in this slot
		if(item != null && item.getEquipmentType() != equipmentSlot)
			return false;
		
		// swap it with whatever is there
		TItem cur = equippedItems.get(equipmentSlot);
		
		if(Objects.equals(cur, item))
			return true; // nothing would have changed anyway, so say it went smoothly without doing anything
		
		int replaceIdx = item == null ? inventory.getSlotsTaken() : inventory.getIndex(item);
		
		if(cur != null && !inventory.removeItem(cur, false))
			return false; // the inventory does not contain the given item; might happen if there is an inventory update during a drag or something.
		
		if(item != null)
			inventory.addItem(replaceIdx, item, false);
		equippedItems.put(equipmentSlot, item);
		
		return true;
	}
	
	int setEquipment(TItem[] equipment) {
		equippedItems.clear();
		for(int i = 0; i < equipment.length; i++) {
			if(equipment[i] != null)
				equippedItems.put(EquipmentSlot.values[i], equipment[i]);
		}
		return equippedItems.size();
	}
}
