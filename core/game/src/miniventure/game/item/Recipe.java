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
			for(int i = 0; i < cost.count; i++)
				inv.removeItem(cost.item);
		
		for(int i = 0; i < result.count; i++)
			inv.addItem(result.item);
		
		// TODO possibly check to make sure all items could be put in inventory?
		
		return true;
	}
}
