package miniventure.game.item;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Recipe {
	
	@NotNull private final ServerItemStack result;
	@NotNull private final ServerItemStack[] costs;
	
	public Recipe(@NotNull ServerItem result, @NotNull ServerItemStack... costs) {
		this(new ServerItemStack(result, 1), costs);
	}
	public Recipe(@NotNull ServerItemStack result, @NotNull ServerItemStack... costs) {
		this.result = result;
		this.costs = costs;
	}
	
	@NotNull ServerItemStack getResult() { return result; }
	@NotNull ServerItemStack[] getCosts() { return costs; }
	
	public boolean canCraft(ServerInventory inv) {
		for(ServerItemStack cost: costs)
			if(!inv.hasItem(cost.item, cost.count))
				return false;
		
		return true;
	}
	
	// returns any items that couldn't be added to the inventory, or null if the inventory doesn't have the items required for crafting.
	@Nullable
	public ServerItem[] tryCraft(ServerInventory inv) {
		if(!canCraft(inv))
			return null;
		
		Array<ServerItem> leftover = new Array<>(ServerItem.class);
		
		for(ServerItemStack cost: costs)
			for(int i = 0; i < cost.count; i++)
				inv.removeItem(cost.item);
		
		for(int i = 0; i < result.count; i++) {
			if(!inv.addItem(result.item))
				leftover.add(result.item);
		}
		
		return leftover.shrink();
	}
	
	@Override
	public String toString() {
		return result.toString()+" Recipe";
	}
}
