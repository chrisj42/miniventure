package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import org.jetbrains.annotations.NotNull;

public class Hands {
	
	/// This is a wrapper class for items, when being currently held by the player. Perhaps *this* should be extended..?
	
	private class HandItem extends Item {
		HandItem() {
			super("", GameCore.icons.get("blank"));
		}
		
		@Override
		public Item use() { return this; }
		
		@Override
		public void drawItem(int stackSize, Batch batch, BitmapFont font, float x, float y) {}
	}
	
	@NotNull private Item item;
	private int count = 1;
	private final Player player;
	private boolean used = false;
	
	public Hands(Player player) {
		this.player = player;
		item = new HandItem();
	}
	
	public void setItem(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
		used = false;
	}
	
	public boolean addItem(@NotNull Item other) {
		if(item instanceof HandItem)
			item = other;
		else if(item.equals(other))
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
		if(!used) return;
		
		used = false;
		if(count <= 0) { // this shouldn't happen
			item = new HandItem();
			count = 1;
			return;
		}
		Item newItem = item.use();
		
		player.changeStat(Stat.Stamina, -item.getStaminaUsage());
		//if(!player.payStamina(item.getStaminaUsage()))
		//	return; // don't "use" the item.
		
		if(count == 1)
			item = newItem == null ? new HandItem() : newItem;
		else {
			count--;
			if(newItem != null && !player.takeItem(newItem)) // will add it to hand, or inventory, whichever fits.
				count++; // if there was a new item, and it couldn't be picked up, then the count is not decreased.
		}
	}
	
	private boolean used() { return used || count <= 0 || player.getStat(Stat.Stamina) < item.getStaminaUsage(); }
	
	// reflexive usage
	public boolean interact() {
		if(used()) return false;
		if(item.interact(player) || player.interactWith(item)) used = true;
		return used;
	}
	
	public boolean interact(WorldObject obj) {
		if(used()) return false;
		if(item.interact(obj, player) || obj.interactWith(player, item)) used = true;
		return used;
	}
	
	public boolean attack(WorldObject obj) {
		//System.out.println("attacking with " + item);
		if(used()) return false;
		//System.out.println("not used");
		if(item.attack(obj, player) || obj.attackedBy(player, item)) used = true;
		//System.out.println("item used: " + used);
		return used;
	}
	
	@NotNull
	public Item getItem() { return item; }
	public int getCount() { return count; }
}
