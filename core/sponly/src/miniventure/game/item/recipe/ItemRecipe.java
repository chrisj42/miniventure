package miniventure.game.item.recipe;

import miniventure.game.item.Item;
import miniventure.game.item.ItemStack;
import miniventure.game.item.inventory.Inventory;
import miniventure.game.world.entity.mob.player.PlayerInventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemRecipe extends Recipe {
	
	public ItemRecipe(@NotNull Item result, @NotNull ItemStack... costs) {
		this(new ItemStack(result, 1), costs);
	}
	public ItemRecipe(@NotNull ItemStack result, @NotNull ItemStack... costs) {
		super(result, costs);
	}
	
	// returns the number of items that couldn't be added to the inventory, or null if the inventory doesn't have the items required for crafting.
	@Nullable
	public Integer tryCraft(Inventory inv) {
		if(!canCraft(inv))
			return null;
		
		deductCosts(inv);
		
		int leftover = 0;
		for(int i = 0; i < getResult().count; i++) {
			if(!inv.addItem(getResult().item))
				leftover++;
		}
		
		return leftover;
	}
	
	@Override
	public boolean isItemRecipe() {
		return true;
	}
	
	@Override
	public void onSelect(PlayerInventory inv) {
		Integer left = tryCraft(inv);
		if (left != null) {
			for (int i = 0; i < left; i++)
				inv.getPlayer().getLevel().dropItem(getResult().item, inv.getPlayer().getPosition(), null);
		}
	}
}
