package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Item {
	
	/*
		Will have a use() method, to mark that an item has gotten used. Called by tiles and entities. This class will determine whether it can be used again, however.
		Perhaps later I can add a parameter to the use method to specify how *much* to use it.
		
		
	 */
	
	private static final TextureRegion missing = GameCore.icons.get("missing");
	
	private final ItemData itemData;
	private boolean used = false;
	private int stackSize = 1;
	
	public Item(ItemData data) {
		this.itemData = data;
	}
	
	public ItemData getItemData() { return itemData; }
	
	void setUsed() { used = true; }
	public boolean isUsed() { return used; }
	
	public Item consume() {
		if(!used) return this;
		
		stackSize--;
		
		if(stackSize == 0)
			return null; // generally, used up items disappear. But, tool items will only lose durability, and stacks will decrease by one.
		else {
			used = false;
			return this;
		}
	}
	
	public int getStackSize() { return stackSize; }
	
	public boolean addToStack(Item other) {
		if(other.itemData == itemData) {
			stackSize += other.stackSize;
			if(stackSize > itemData.getMaxStackSize()) {
				other.stackSize = stackSize - itemData.getMaxStackSize();
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	public boolean attack(WorldObject obj, Player player) {
		if(used) return true;
		return itemData.attack(this, obj, player);
	}
	
	public boolean interact(Player player) {
		if(itemData.isReflexive()) {
			player.interactWith(player, this);
			return true; // do not interact with other things
		}
		return false; // continue
	}
	
	public boolean interact(WorldObject obj, Player player) {
		if(used) return true;
		return itemData.interact(this, obj, player);
	}
	
	public int getDamage(WorldObject obj) { return itemData.getDamage(obj); }
	
	//public Item clone() { return new Item(itemData); }
	
	public String toString() {
		return itemData.getName() + " item ("+stackSize+" stack)";
	}
}
