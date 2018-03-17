package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;
import miniventure.game.world.entity.particle.ItemEntity;

import com.badlogic.gdx.graphics.g2d.Batch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Hands {
	
	/// This is a wrapper class for items, when being currently held by the player. Perhaps *this* should be extended..?
	
	private static class HandItem extends Item {
		HandItem() {
			super(ItemType.Misc, "Hand", GameCore.icons.get("blank"));
		}
		
		@Override
		public String[] save() { return new String[] {"Hands.HandItem"}; }
		
		public static Item load(String[] data) { return new HandItem(); }
		
		@Override public Item getUsedItem() { return this; }
		@Override public Item copy() { return new HandItem(); }
		
		@Override public boolean interact(WorldObject obj, Player player) {
			boolean success = obj.interactWith(player, null);
			if(success) use();
			return success;
		}
		@Override public boolean attack(WorldObject obj, Player player) {
			boolean success = obj.attackedBy(player, null, 1);
			if(success) use();
			return success;
		}
		
		@Override
		public void drawItem(int stackSize, Batch batch, float x, float y) {}
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
		if(inv != null && count > 0 && !(item instanceof HandItem)) {
			int added = inv.addItem(item, count);
			if(added != count) {
				ServerLevel level = player.getServerLevel();
				if(level != null)
					for(int i = 0; i < count-added; i++)
						level.addEntity(new ItemEntity(item, null), player.getPosition(), true);
			}
		}
		
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
				ServerLevel level = player.getServerLevel();
				if(level != null)
					level.dropItem(newItem, player.getCenter(), null);//count++; // if there was a new item, and it couldn't be picked up, then the count is not decreased.
			}
		}
	}
	
	public boolean hasUsableItem() { return !(item.isUsed() || count <= 0 || player.getStat(Stat.Stamina) < item.getStaminaUsage()); }
	
	@NotNull
	public Item getUsableItem() { return item; }
	@Nullable
	public Item getEffectiveItem() { return item instanceof HandItem ? null : item; }
	
	public int getCount() { return count; }
	
	public String[] save() {
		return ItemStack.save(getUsableItem(), getCount());
	}
	
	public void loadItem(String[] data) {
		ItemStack stack = ItemStack.load(data);
		count = stack.count;
		item = stack.item;
	}
}
