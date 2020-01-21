package miniventure.game.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemRecipe extends Recipe<ServerItemStack> {
	
	public ItemRecipe(@NotNull ServerItem result, @NotNull ServerItemStack... costs) {
		this(new ServerItemStack(result, 1), costs);
	}
	public ItemRecipe(@NotNull ServerItemStack result, @NotNull ServerItemStack... costs) {
		super(result, costs);
	}
	
	// returns the number of items that couldn't be added to the inventory, or null if the inventory doesn't have the items required for crafting.
	@Nullable
	public Integer tryCraft(ServerInventory inv) {
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
}
