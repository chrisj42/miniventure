package miniventure.game.item.recipe;

import miniventure.game.core.GameCore;
import miniventure.game.item.ItemStack;
import miniventure.game.item.inventory.Inventory;
import miniventure.game.world.entity.mob.player.PlayerInventory;

import org.jetbrains.annotations.NotNull;

public abstract class Recipe {
	
	@NotNull private final ItemStack result;
	@NotNull private final ItemStack[] costs;
	
	protected Recipe(@NotNull ItemStack result, @NotNull ItemStack... costs) {
		this.result = result;
		this.costs = costs;
	}
	
	@NotNull
	public ItemStack getResult() { return result; }
	
	@NotNull
	public ItemStack[] getCosts() { return costs; }
	
	public boolean canCraft(Inventory inv) {
		for(ItemStack cost: costs)
			if(!inv.hasItem(cost.item, cost.count))
				return false;
		
		return true;
	}
	
	protected void deductCosts(Inventory inv) {
		if(!GameCore.debug) {
			for(ItemStack cost : costs)
				for(int i = 0; i < cost.count; i++)
					inv.removeItem(cost.item);
		}
	}
	
	public abstract boolean isItemRecipe();
	public abstract void onSelect(PlayerInventory inv);
	
	@Override
	public String toString() {
		return getResult().toString()+' '+getClass().getSimpleName();
	}
}
