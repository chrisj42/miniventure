package miniventure.game.item;

import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTile;

import org.jetbrains.annotations.NotNull;

public class ObjectRecipe extends Recipe {
	
	private final ConstructableObjectType objectType;
	
	public ObjectRecipe(@NotNull ConstructableObjectType result, @NotNull ServerItemStack... costs) {
		super(new ServerItemStack(result.getItem(), 0), costs);
		this.objectType = result;
	}
	
	public boolean tryCraft(ServerTile tile, ServerPlayer player, ServerInventory inv) {
		if(!canCraft(inv))
			return false;
		
		boolean placed = objectType.tryPlace(tile, player);
		if(placed)
			deductCosts(inv);
		return placed;
	}
	
}
