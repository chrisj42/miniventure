package miniventure.game.item;

import miniventure.game.core.GameCore;
import miniventure.game.util.ArrayUtils;

import org.jetbrains.annotations.NotNull;

public abstract class Recipe {
	
	@NotNull private final ServerItemStack result;
	@NotNull private final ServerItemStack[] costs;
	
	protected Recipe(@NotNull ServerItemStackSource result, @NotNull ServerItemStackSource... costs) {
		this.result = result.getStack();
		this.costs = ArrayUtils.mapArray(costs, ServerItemStack.class, ServerItemStackSource::getStack);
	}
	
	@NotNull
	public ServerItemStack getResult() { return result; }
	
	@NotNull
	public ServerItemStack[] getCosts() { return costs; }
	
	public boolean canCraft(ServerInventory inv) {
		for(ServerItemStack cost: costs)
			if(!inv.hasItem(cost.item, cost.count))
				return false;
		
		return true;
	}
	
	protected void deductCosts(ServerInventory inv) {
		if(!GameCore.debug) {
			for(ServerItemStack cost : costs)
				for(int i = 0; i < cost.count; i++)
					inv.removeItem(cost.item);
		}
	}
	
	@Override
	public String toString() {
		return getResult().toString()+' '+getClass().getSimpleName();
	}
}
