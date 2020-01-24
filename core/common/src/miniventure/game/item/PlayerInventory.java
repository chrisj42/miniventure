package miniventure.game.item;

import java.util.EnumMap;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection AbstractClassWithoutAbstractMethods*/
public abstract class PlayerInventory<TItem extends Item, TItemStack extends ItemStack, TInv extends Inventory<TItem, TItemStack>> {
	
	public static final int INV_SIZE = 50;
	public static final int HOTBAR_SIZE = 5;
	
	// server player inventory and client player inventory; each extends their side of the inventory class, as well as implementing this; this in turn extends the inventory interface.
	
	// player inventories have a hotbar which can contain empty spaces, as well as the ability to suppress certain items from showing up while still counting them towards the quota.
	
	@NotNull private TInv inventory;
	// @NotNull private TInv hotbar;
	
	final EnumMap<EquipmentType, TItem> equippedItems;
	
	// private final TreeMap<Integer, Integer> usedHotbarSlots;
	
	// private boolean swapping = false; // if true, then the listeners ignore events.
	
	PlayerInventory(@NotNull TInv inventory) {
		this.inventory = inventory;
		// this.hotbar = hotbar;
		
		equippedItems = new EnumMap<>(EquipmentType.class);
		
		// usedHotbarSlots = new TreeMap<>();
		
		reset();
	}
	
	public void reset() {
		inventory.reset();
		// hotbar.reset();
		equippedItems.clear();
		
		/*for (int i = 0; i < hotbar.getSpace(); i++) {
			hotbar.uniqueItems.add(null);
		}*/
	}
	
	@NotNull
	public TInv getInv() { return inventory; }
	
	@Nullable
	public Item getEquippedItem(EquipmentType slot) {
		return equippedItems.get(slot);
	}
	
	/*public int getHotbarItemCount() { return usedHotbarSlots.size(); }
	
	public int getInventoryIndex(int hotbarIdx) {
		return usedHotbarSlots.getOrDefault(hotbarIdx, -1);
	}*/
	
	/*@Override
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
	}*/
	
	/*void swapItems(int startItemIdx, int startHotbarIdx, int finItemIdx, int finHotbarIdx) {
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
	}*/
	
	// this is called by the inventory overlay
	public boolean equipItem(@NotNull EquipmentType equipmentSlot, int index) {
		// final Inventory<TItem, TItemStack> inv = hotbar ? this : inventory;
		TItem item = inventory.getItem(index);
		
		// ensure the given item is allowed to be in this slot
		if(!(item instanceof EquipmentItem) || ((EquipmentItem)item).getEquipmentType() != equipmentSlot)
			return false;
		
		// swap it with whatever is there
		TItem curEquip = equippedItems.get(equipmentSlot);
		
		if(Objects.equals(curEquip, item))
			return true; // nothing would have changed anyway, so say it went smoothly without doing anything
		
		inventory.removeItem(item, false);
		
		unequipItem(equipmentSlot, index);
		
		equippedItems.put(equipmentSlot, item);
		
		return true;
	}
	
	public boolean unequipItem(@NotNull EquipmentType equipmentSlot, int index) {
		TItem item = equippedItems.remove(equipmentSlot);
		if(item == null)
			return false;
		
		inventory.addItem(index, item, false);
		
		return true;
	}
	
	int setEquipment(TItem[] equipment) {
		equippedItems.clear();
		for(int i = 0; i < equipment.length; i++) {
			if(equipment[i] != null)
				equippedItems.put(EquipmentType.values[i], equipment[i]);
		}
		return equippedItems.size();
	}
	
	/*void setHotbarSlots(String[] positions) {
		usedHotbarSlots.clear();
		int invIdx = 0;
		for(String posData: positions) {
			usedHotbarSlots.put(Integer.parseInt(posData), invIdx++);
		}
	}*/
	
	/*String[] getHotbarData() {
		Integer[] positions = usedHotbarSlots.keySet().toArray(new Integer[0]);
		return ArrayUtils.mapArray(positions, String.class, String::valueOf);
	}*/
}
