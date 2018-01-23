package miniventure.game.item.crafting;

import miniventure.game.item.Inventory;
import miniventure.game.item.ItemStack;

public class Recipe {
	
	private final ItemStack result;
	private final ItemStack[] costs;
	
	public Recipe(ItemStack result, ItemStack... costs) {
		this.result = result;
		this.costs = costs;
	}
	
	public boolean tryCraft(Inventory inv) {
		//boolean canCraft = true;
		for(ItemStack cost: costs) {
			//int req = cost.count;
			if(!inv.hasItem(cost.item, cost.count))
				return false;
			/*for(Item item: inventory.) {
				if(item.equals(cost.item)) {
					req -= item.getStackSize();
					if(req <= 0) break;
				}
			}*/
			/*if(req > 0) {
				canCraft = false;
				break;
			}*/
		}
		
		//if(!canCraft) return false;
		
		for(ItemStack cost: costs) {
			/*int req = cost.getStackSize();
			for(Item item: inventory) {
				if(item.equals(cost)) {
					
					req -= item.getStackSize();
					if(req <= 0) break;
				}
			}
			if(req > 0) {
				canCraft = false;
				break;
			}*/
			inv.removeItem(cost.item, cost.count);
		}
		
		inv.addItem(result.item, result.count);
		
		return true;
	}
}
