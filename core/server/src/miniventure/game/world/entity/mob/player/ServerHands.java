package miniventure.game.world.entity.mob.player;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.item.Inventory;
import miniventure.game.item.ItemStack;
import miniventure.game.item.ItemType;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player.Stat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerHands {
	
	// holds the items in the player's hotbar.
	
	/* 
		- for now, I won't worry about being able to reorder the inventory. I'll be
		wary of implementations that specifically inhibit reordering, but I won't try
		to deal with anything besides dropped items, hotbar selections, and items picked up
		by the player while idle (if for example a mob dies nearby or another player drops an item
		nearby).
		
		- that means, for the client, the inventory (screen) needs to be able to:
			- add item (stack) to the end
			- modify item stack at an index
			- remove item (stack) at an index
		- the client hands just need:
			- set item at index
			- ...and that's about it!
	 */
	
	private ServerPlayer player;
	private final ServerItem[] hotbarItems;
	
	ServerHands(ServerPlayer player) {
		this.player = player;
		hotbarItems = new ServerItem[Player.HOTBAR_SIZE];
	}
	
	ServerHands(ServerPlayer player, String[] itemData) {
		this(player);
		
		for(int i = 0; i < hotbarItems.length; i++)
			hotbarItems[i] = ServerItem.load(MyUtils.parseLayeredString(itemData[i]));
	}
	
	void reset() { Arrays.fill(hotbarItems, null); }
	
	private Inventory getInv() { return player.getInventory(); }
	
	// note, the server hotbar does not track item count, only item reference.
	// when sending to the client, it fetches the counts from the inventory. 
	private void setSlot(int idx, @Nullable ServerItem item) {
		hotbarItems[idx] = item;
	}
	
	boolean addItem(@NotNull ServerItem item) { return addItem(item, 0); }
	private boolean addItem(@NotNull ServerItem item, int fromIndex) {
		// if(item instanceof HandItem)
		// 	return false; // just kinda ignore these
		
		// check for given item while also finding the first open slot starting from "fromIndex" (and looping around if necessary)
		int firstOpen = -1;
		for(int i = 0; i < hotbarItems.length; i++) {
			int idx = (i+fromIndex) % hotbarItems.length;
			if(item.equals(hotbarItems[idx]))
				return false; // item is already in hotbar
			else if(firstOpen < 0 && hotbarItems[idx] == null)
				firstOpen = idx; // finds first open slot
		}
		
		if(firstOpen < 0) // no open hotbar slots
			return false;
		
		// open slot found; setting to given item.
		setSlot(firstOpen, item);
		return true;
	}
	
	@Nullable
	private ServerItem removeItem(int idx) {
		ServerItem prevItem = hotbarItems[idx];
		hotbarItems[idx] = null;
		return prevItem;
	}
	private boolean removeItem(@NotNull ServerItem item) {
		int idx = findItem(item);
		if(idx < 0) return false;
		removeItem(idx);
		return true;
	}
	
	private int findItem(@NotNull ServerItem item) {
		for(int i = 0; i < hotbarItems.length; i++)
			if(item.equals(hotbarItems[i]))
				return i;
		
		return -1;
	}
	
	@NotNull
	ServerItem getHeldItem(int index) {
		ServerItem item = getItem(index);
		if(item == null) item = HandItem.hand;
		return item;
	}
	
	void resetItemUsage(@NotNull ServerItem item, int index) {
		ServerItem newItem = item.getUsedItem();
		
		if(!GameCore.debug)
			player.changeStat(Stat.Stamina, -item.getStaminaUsage());
		
		// While I could consider this asking for trouble, I'm already stacking items, so any unspecified "individual" data is lost already.
		if(item.equals(newItem)) // this is true for the hand item.
			return; // there is literally zero difference in what the item is now, and what it was before.
		
		// the item has changed (possibly into nothing)
		
		// remove the current item.
		getInv().removeItem(item);
		if(newItem != null) {
			// the item has changed either in metadata or into an entirely separate item.
			// add the new item to the inventory, and then determine what should become the held item: previous or new item.
			// if the new item doesn't fit, then drop it on the ground instead.
			
			if(!getInv().addItem(newItem)) {
				// inventory is full, try to drop it on the ground
				ServerLevel level = player.getLevel();
				if(level != null)
					level.dropItem(newItem, player.getCenter(), player.getCenter().add(player.getDirection().getVector()));
				else // this is a very bad situation, ideally it should never happen. I was considering adding a check for it, but think about it: you're using an item, but not on a level? The only way you're not on a level is if you're travelling between levels, and you're definitely not using items then, you're browsing the level select screen.
					System.err.println("ERROR: could not drop usage-spawn item "+newItem+", ServerLevel for player "+player+" is null. (inventory is also full)");
			}
			else if(findItem(newItem) < 0) { // new item not found in hotbar
				// item was successfully added to the inventory; now figure out what to do with the hotbar.
				// this block is only needed to figure out where to put the new item; if it's already in the hotbar then we don't need to do anything. ;)
				
				// check if the original item has run out, in which case the new item should replace it in the hotbar.
				if(!getInv().hasItem(item))
					hotbarItems[index] = newItem;
				else {
					// original item still exists; decide if new item should replace it or not
					if(newItem.getName().equals(item.getName())) {
						// metadata changed, same item, replace stack
						setSlot(index, newItem);
						addItem(item, index); // try and keep the stack in the hotbar
					}
					else // name changed, different item, keep stack
						addItem(newItem); // try-add new item to hotbar like when you normally pick items up
				}
			}
		}
		
		// remove old item from hotbar if it's no longer in the inventory
		if(!getInv().hasItem(item))
			removeItem(item);
		
		// we are never going to be in inventory mode here, because the client has just used an item; items can't be used with a menu open.
		ServerCore.getServer().sendToPlayer(player, player.getHotbarUpdate());
	}
	
	@Nullable
	public ServerItem getItem(int idx) { return hotbarItems[idx]; }
	
	// check each slot and remove any that points to an item not in the inventory. Return true if an update occurs.
	public boolean validate() {
		boolean updated = false;
		for(int i = 0; i < hotbarItems.length; i++) {
			if(!getInv().hasItem(hotbarItems[i])) {
				hotbarItems[i] = null;
				updated = true;
			}
		}
		
		return updated;
	}
	
	public String[][] serialize() {
		String[][] data = new String[hotbarItems.length][];
		for(int i = 0; i < hotbarItems.length; i++)
			data[i] = hotbarItems[i] == null ? null : ItemStack.serialize(hotbarItems[i], getInv().getCount(hotbarItems[i]));
		return data;
	}
	
	public String[] save() {
		// make sure we don't save out-of-date information.
		validate();
		
		String[] data = new String[hotbarItems.length];
		for(int i = 0; i < hotbarItems.length; i++)
			data[i] = MyUtils.encodeStringArray(hotbarItems[i].save());
		return data;
	}
	
	public void fromInventoryIndex(int[] indices) {
		for(int i = 0; i < indices.length; i++)
			hotbarItems[i] = indices[i] < 0 ? null : getInv().getItem(indices[i]);
	}
	
	public int[] toInventoryIndex() {
		int[] indices = new int[hotbarItems.length];
		for(int i = 0; i < indices.length; i++)
			indices[i] = getInv().getIndex(hotbarItems[i]);
		return indices;
	}
	
	private static final class HandItem extends ServerItem {
		
		// this is to be used only for the purposes of interaction; the inventory should not have them, and the hotbar ought to be filled with nulls for empty spaces.
		
		static final HandItem hand = new HandItem();
		
		private HandItem() {
			super(ItemType.Misc, "Hand", GameCore.icons.get("blank"));
		}
		
		@Override
		public int getSpaceUsage() { return 0; }
		
		@Override
		public String[] save() {
			throw new IllegalMethodReferenceException("save"); // hand items cannot be saved 
		}
		
		public static ServerItem load(String[] data) {
			throw new IllegalMethodReferenceException("load"); // hand items cannot be loaded
		}
		
		@Override public ServerItem getUsedItem() { return this; }
		@Override public ServerItem copy() { return this; }
		
		@Override public Result interact(WorldObject obj, Player player) {
			return obj.interactWith(player, null);
		}
		@Override public Result attack(WorldObject obj, Player player) {
			return obj.attackedBy(player, null, 1);
		}
		
		private static class IllegalMethodReferenceException extends RuntimeException {
			IllegalMethodReferenceException(String method) {
				super("Method '"+method+"' cannot be called on objects of type HandItem.");
			}
		}
	}
}
