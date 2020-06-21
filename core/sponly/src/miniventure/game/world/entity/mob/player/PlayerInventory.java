package miniventure.game.world.entity.mob.player;

import java.util.EnumMap;
import java.util.Objects;

import miniventure.game.core.GdxCore;
import miniventure.game.item.EquipmentItem;
import miniventure.game.item.EquipmentItem.EquipmentType;
import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;
import miniventure.game.item.inventory.CraftingScreen;
import miniventure.game.item.inventory.Inventory;
import miniventure.game.item.recipe.ObjectRecipe;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerInventory extends Inventory {
	
	public static final int INV_SIZE = 50;
	public static final int HOTBAR_SIZE = 5;
	
	// server player inventory and client player inventory; each extends their side of the inventory class, as well as implementing this; this in turn extends the inventory interface.
	
	// player inventories have a hotbar which can contain empty spaces, as well as the ability to suppress certain items from showing up while still counting them towards the quota.
	
	@NotNull private Player player;
	// @NotNull private Inventory inventory;
	
	private HammerItem lastQueried = null;
	final EnumMap<EquipmentType, Item> equippedItems;
	
	private int selection = -1; // -1 is reserved for an empty hand.
	
	public PlayerInventory(@NotNull Player player) {
		super(INV_SIZE);
		
		this.player = player;
		// this.inventory = new Inventory(INV_SIZE);
		// this.hotbar = hotbar;
		
		equippedItems = new EnumMap<>(EquipmentType.class);
		
		// usedHotbarSlots = new TreeMap<>();
		
		reset();
	}
	
	void setPlayer(@NotNull Player player) {
		this.player = player;
	}
	
	@Override
	public void reset() {
		super.reset();
		// hotbar.reset();
		if(equippedItems != null)
			equippedItems.clear();
		
		/*for (int i = 0; i < hotbar.getSpace(); i++) {
			hotbar.uniqueItems.add(null);
		}*/
	}
	
	@NotNull
	public Player getPlayer() { return player; }
	
	public void setSelection(int idx) {
		int slots = getSlotsTaken();
		// add one to buffer for the -1 option
		selection = MyUtils.wrapIndex(idx+1, slots+1) - 1;
	}
	public int getSelection() { return selection; }
	
	@Nullable
	public Item getSelectedItem() {
		return getItem(getSelection());
	}
	
	@Nullable
	public ItemStack getSelectedStack() {
		return getItemStack(getSelection());
	}
	
	@NotNull
	public Item getHeldItem(int index) {
		Item item = getItem(index);
		if(item == null) item = HandItem.hand;
		return item;
	}
	
	@Nullable
	public Item getEquippedItem(EquipmentType slot) {
		return equippedItems.get(slot);
	}
	
	// this is called by the inventory overlay
	public boolean equipItem(@NotNull EquipmentType equipmentSlot, int index) {
		// final Inventory<Item, ItemStack> inv = hotbar ? this : inventory;
		Item item = getItem(index);
		
		// ensure the given item is allowed to be in this slot
		//noinspection InstanceofIncompatibleInterface
		if(!(item instanceof EquipmentItem) || ((EquipmentItem)item).getEquipmentType() != equipmentSlot)
			return false;
		
		// swap it with whatever is there
		Item curEquip = equippedItems.get(equipmentSlot);
		
		if(Objects.equals(curEquip, item))
			return true; // nothing would have changed anyway, so say it went smoothly without doing anything
		
		removeItem(item, false);
		
		unequipItem(equipmentSlot, index);
		
		equippedItems.put(equipmentSlot, item);
		
		return true;
	}
	
	public boolean unequipItem(@NotNull EquipmentType equipmentSlot, int index) {
		Item item = equippedItems.remove(equipmentSlot);
		if(item == null)
			return false;
		
		addItem(index, item, false);
		
		return true;
	}
	
	int setEquipment(Item[] equipment) {
		equippedItems.clear();
		for(int i = 0; i < equipment.length; i++) {
			if(equipment[i] != null)
				equippedItems.put(EquipmentType.values[i], equipment[i]);
		}
		return equippedItems.size();
	}
	
	void resetItemUsage(@NotNull Item item, int index) {
		Item newItem = item.getUsedItem();
		
		if(item.equals(newItem)) // this is true for the hand item.
			return; // there is literally zero difference in what the item is now, and what it was before.
		
		// the item has changed (possibly into nothing)
		
		// remove the current item.
		removeItem(item);
		if(newItem != null) {
			// the item has changed either in metadata or into an entirely separate item.
			// add the new item to the inventory, and then determine what should become the held item: previous or new item.
			// if the new item doesn't fit, then drop it on the ground instead.
			
			if (!addItem(index, newItem)) {
				// inventory is full, drop it on the ground
				player.level.dropItem(newItem, player.getCenter(), player.getCenter().add(player.getDirection().getVector()));
			}
		}
	}
	
	void dropItems(int index, boolean all) {
		Item item = getItem(index);
		if(item == null)
			return;
		
		int removed;
		if(all)
			removed = removeItemStack(item);
		else
			removed = removeItem(item) ? 1 : 0;
		
		if(removed == 0)
			return;
		
		setSelection(Math.min(getSlotsTaken() - 1, selection));
		
		// get target pos, which is one tile in front of player.
		Vector2 center = player.getCenter();
		Vector2 targetPos = center.cpy().add(player.getDirection().getVector().scl(2)); // adds 2 in the direction of the player.
		for(int i = 0; i < removed; i++)
			player.getLevel().dropItem(item, true, center, targetPos);
	}
	
	// called on interaction with a hammer item. Opens a crafting screen with special recipes, and upon selecting one the hammer will be replaced to reflect the chosen recipe.
	// goes through here before opening menu to cache the hammer used, so we know what item to replace when the crafting screen is closed.
	void useHammer(HammerItem prevHammer) {
		lastQueried = prevHammer;
		// reset the selected recipe for this hammer
		HammerItem nullHammer = prevHammer.setRecipe(null);
		if(!nullHammer.equals(prevHammer)) {
			// replace the inventory item
			int idx = getIndex(prevHammer); // there's no reason this should ever fail...
			if(idx >= 0) { // ...but I'll add this check anyway
				if(removeItem(prevHammer))
					addItem(idx, nullHammer);
				// getWorld().getServer().sendToPlayer(this, invManager.getUpdate(false));
			}
			lastQueried = nullHammer;
		}
		GdxCore.setScreen(new CraftingScreen(prevHammer.getRecipeSet(), this));
		// give the client the info to open the crafting screen
		/*getWorld().getServer().sendToPlayer(this, new RecipeUpdate(
				item.getRecipeSet().getSerialRecipes(),
				new RecipeStockUpdate(inventory.getItemStacks())
		));*/
	}
	
	public void setHammerRecipe(ObjectRecipe recipe) {
		if(lastQueried == null)
			return;
		HammerItem newHammer = lastQueried.setRecipe(recipe);
		if(newHammer.equals(lastQueried))
			return;
		int idx = getIndex(lastQueried); // there's no reason this should ever fail...
		if(idx >= 0) { // ...but I'll add this check anyway
			if(removeItem(lastQueried))
				addItem(idx, newHammer);
			// getWorld().getServer().sendToPlayer(this, invManager.getUpdate(false));
		}
	}
	
	public String save() {
		// String[] invData = saveItems();
		
		// String[] allData = new String[EquipmentType.values.length + invData.length];
		// System.arraycopy(invData, 0, allData, EquipmentType.values.length, invData.length);
		String[] equipData = new String[EquipmentType.values.length];
		for (int i = 0; i < equipData.length; i++) {
			Item item = equippedItems.get(EquipmentType.values[i]);
			equipData[i] = item == null ? "" : item.save();
		}
		
		// allData[EquipmentSlot.values.length] = MyUtils.encodeStringArray(getHotbarData());
		
		return MyUtils.encodeStringArray(equipData, saveItems());
	}
	
	public void load(String data, @NotNull Version version) {
		String[] dataAr = MyUtils.parseLayeredString(data);
		
		equippedItems.clear();
		for (int i = 0; i < EquipmentType.values.length; i++) {
			Item item = dataAr[i].length() == 0 ? null : Item.load(dataAr[i], version);
			if(item != null)
				equippedItems.put(EquipmentType.values[i], item);
		}
		int buffer = equippedItems.size();
		
		// setHotbarSlots(MyUtils.parseLayeredString(data[equipment.length]));
		loadItems(dataAr, EquipmentType.values.length, buffer, version);
	}
}
