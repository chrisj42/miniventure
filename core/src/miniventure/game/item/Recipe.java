package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public class Recipe {
	
	@NotNull private final ItemStack result;
	@NotNull private final ItemStack[] costs;
	
	public Recipe(@NotNull Item result, @NotNull ItemStack... costs) {
		this(new ItemStack(result, 1), costs);
	}
	public Recipe(@NotNull ItemStack result, @NotNull ItemStack... costs) {
		this.result = result;
		this.costs = costs;
	}
	
	@NotNull ItemStack getResult() { return result; }
	@NotNull ItemStack[] getCosts() { return costs; }
	
	public boolean canCraft(Inventory inv) {
		for(ItemStack cost: costs)
			if(!inv.hasItem(cost.item, cost.count))
				return false;
		
		return true;
	}
	
	public boolean tryCraft(Inventory inv) {
		if(!canCraft(inv))
			return false;
		
		for(ItemStack cost: costs) 
			inv.removeItem(cost.item, cost.count);
		
		inv.addItem(result.item, result.count);
		
		return true;
	}
}
