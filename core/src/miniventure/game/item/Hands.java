package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Hands {
	
	/// This is a wrapper class for items, when being currently held by the player. Perhaps *this* should be extended..?
	
	private class HandItem extends Item {
		HandItem() {
			super("Hand", GameCore.icons.get("blank"));
		}
		
		@Override public boolean isUsed() { return true; } // always use the stamina... note, this may have undesired results if anything else checks to see if the item is used, besides here...
		@Override public Item getUsedItem() { return this; }
		@Override public Item copy() { return new HandItem(); }
		
		@Override public boolean interact(WorldObject obj, Player player) { return obj.interactWith(player, null); }
		
		@Override
		public void drawItem(int stackSize, Batch batch, BitmapFont font, float x, float y) {}
	}
	
	@NotNull private Item item;
	private int count = 1;
	private final Player player;
	
	public Hands(Player player) {
		this.player = player;
		item = new HandItem();
	}
	
	public void setItem(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	public boolean addItem(@NotNull Item other) {
		if(item instanceof HandItem)
			item = other;
		else if(item.equals(other) && count < item.getMaxStackSize())
			count++;
		else
			return false;
		
		return true;
	}
	
	public void clearItem(Inventory inv) {
		if(count > 0 && !(item instanceof HandItem))
			inv.addItem(item, count);
		
		item = new HandItem();
		count = 1;
	}
	
	public void resetItemUsage() {
		if(count <= 0) { // this shouldn't happen, generally... unless stuff has been removed from the active item by the crafting menu.
			item = new HandItem();
			count = 1;
			return;
		}
		
		if(!item.isUsed()) return;
		
		Item newItem = item.resetUsage();
		
		player.changeStat(Stat.Stamina, -item.getStaminaUsage());
		
		if(count == 1 || item instanceof HandItem)
			setItem(newItem == null ? new HandItem() : newItem, 1);
		else {
			count--;
			if(newItem != null && !player.takeItem(newItem)) {// will add it to hand, or inventory, whichever fits.
				// this happens if the inventory is full; in such a case, drop the new item on the ground.
				Level level = player.getLevel();
				if(level != null)
					level.dropItem(newItem, player.getCenter(), null);//count++; // if there was a new item, and it couldn't be picked up, then the count is not decreased.
			}
		}
	}
	
	public boolean hasUsableItem() { return !(item.isUsed() || count <= 0 || player.getStat(Stat.Stamina) < item.getStaminaUsage()); }
	
	// reflexive usage
	/*public boolean interact() {
		if(used()) return false;
		if(item.interact(player) || player.interactWith(item)) used = true;
		return used;
	}*/
	
	/*public boolean interact(WorldObject obj) {
		if(used()) return false;
		if(item.interact(obj, player) || obj.interactWith(player, item)) used = true;
		return used;
	}
	
	public boolean attack(WorldObject obj) {
		if(used()) return false;
		if(item.attack(obj, player) || obj.attackedBy(player, item)) used = true;
		return used;
	}*/
	
	@NotNull
	public Item getUsableItem() { return item; }
	@Nullable
	public Item getEffectiveItem() { return item instanceof HandItem ? null : item; }
	
	public int getCount() { return count; }
}
