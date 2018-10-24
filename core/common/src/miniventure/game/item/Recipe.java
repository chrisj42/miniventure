package miniventure.game.item;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	
	// returns any items that couldn't be added to the inventory, or null if the inventory doesn't have the items required for crafting.
	@Nullable
	public Item[] tryCraft(Inventory inv) {
		if(!canCraft(inv))
			return null;
		
		Array<Item> leftover = new Array<>(Item.class);
		
		for(ItemStack cost: costs) 
			for(int i = 0; i < cost.count; i++)
				inv.removeItem(cost.item);
		
		for(int i = 0; i < result.count; i++) {
			if(!inv.addItem(result.item))
				leftover.add(result.item);
		}
		
		return leftover.shrink();
	}
}
