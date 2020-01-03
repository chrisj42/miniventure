package miniventure.game.item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import miniventure.game.item.Inventory.ChangeListener;
import miniventure.game.util.ArrayUtils;

import org.jetbrains.annotations.NotNull;

public abstract class PlayerInventory<TItem extends Item, TItemStack extends ItemStack, TInv extends Inventory<TItem, TItemStack>> implements ChangeListener {
	
	public static final int INV_SIZE = 50;
	public static final int HOTBAR_SIZE = 5;
	
	// server player inventory and client player inventory; each extends their side of the inventory class, as well as implementing this; this in turn extends the inventory interface.
	
	// player inventories have a hotbar which can contain empty spaces, as well as the ability to suppress certain items from showing up while still counting them towards the quota.
	
	@NotNull private TInv inventory;
	
	final EnumMap<EquipmentSlot, TItem> equippedItems;
	
	private final TreeMap<Integer, Integer> usedHotbarSlots;
	
	private boolean swapping = false; // if true, then the listeners ignore events.
	
	PlayerInventory(@NotNull TInv inventory) {
		this.inventory = inventory;
		inventory.addListener(this);
		
		equippedItems = new EnumMap<>(EquipmentSlot.class);
		
		usedHotbarSlots = new TreeMap<>();
	}
	
	@NotNull
	public TInv getInv() { return inventory; }
	
	public void reset() {
		inventory.reset();
		equippedItems.clear();
	}
	
	@Override
	public void onInsert(int idx) {
		if(swapping) return;
		// attempt to fit it into the hotbar
		for (int i = 0; i < HOTBAR_SIZE; i++) {
			if(!usedHotbarSlots.containsKey(i)) {
				usedHotbarSlots.put(i, idx);
				usedHotbarSlots.tailMap(i, false).replaceAll((k, v) -> v+1);
				return;
			}
		}
	}
	
	@Override
	public void onRemove(int idx) {
		if(swapping) return;
		if(idx < usedHotbarSlots.size()) {
			// hotbar slot
			int hotbarIdx = usedHotbarSlots.keySet().toArray(new Integer[0])[idx];
			usedHotbarSlots.tailMap(hotbarIdx, false).replaceAll((k, v) -> v-1);
			usedHotbarSlots.remove(hotbarIdx);
		}
	}
	
	void swapItems(int startItemIdx, int startHotbarIdx, int finItemIdx, int finHotbarIdx) {
		assert startItemIdx >= 0; // must have starting item
		assert finItemIdx >= 0 || finHotbarIdx >= 0; // must have destination
		
		swapping = true;
		if(finHotbarIdx >= 0 && finItemIdx >= 0) {
			// dest is hotbar pos with existing item; swap
			inventory.swapItems(startItemIdx, finItemIdx);
		}
		else {
			// insert
			// inv to empty slot, inv to inv, or full slot to empty slot
			
			if(finItemIdx < 0) {
				// dest is empty slot; add entry
				Entry<Integer, Integer> entry = usedHotbarSlots.ceilingEntry(finHotbarIdx);
				finItemIdx = entry == null ? HOTBAR_SIZE : entry.getValue();
				usedHotbarSlots.put(finHotbarIdx, finItemIdx);
				usedHotbarSlots.tailMap(finHotbarIdx, false).replaceAll((k, v) -> v+1);
			}
			
			if(startHotbarIdx >= 0 && startHotbarIdx != finHotbarIdx) {
				// source is hotbar slot; remove entry
				usedHotbarSlots.tailMap(startHotbarIdx, false).replaceAll((k, v) -> v-1);
				usedHotbarSlots.remove(startHotbarIdx);
			}
			
			if(startItemIdx != finItemIdx)
				inventory.moveItem(startItemIdx, finItemIdx);
		}
		swapping = false;
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
	
	void setHotbarSlots(String[] positions) {
		usedHotbarSlots.clear();
		int invIdx = 0;
		for(String posData: positions) {
			usedHotbarSlots.put(Integer.parseInt(posData), invIdx++);
		}
	}
	
	String[] getHotbarData() {
		Integer[] positions = usedHotbarSlots.keySet().toArray(new Integer[0]);
		return ArrayUtils.mapArray(positions, String.class, String::valueOf);
	}
}
