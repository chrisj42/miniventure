package miniventure.game.item;

public class Recipe {
	
	private final ItemStack result;
	private final ItemStack[] costs;
	
	public Recipe(ItemStack result, ItemStack... costs) {
		this.result = result;
		this.costs = costs;
	}
	
	ItemStack getResult() { return result; }
	ItemStack[] getCosts() { return costs; }
	
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
