package miniventure.game.item;

import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTile;

import org.jetbrains.annotations.NotNull;

public class ObjectRecipe extends Recipe<ConstructableObjectType> {
	
	public ObjectRecipe(@NotNull ConstructableObjectType result, @NotNull ServerItemStack... costs) {
		super(result, costs);
	}
	
	public boolean tryCraft(ServerTile tile, ServerPlayer player, ServerInventory inv) {
		if(!canCraft(inv))
			return false;
		
		boolean placed = getResult().tryPlace(tile, player);
		if(placed)
			deductCosts(inv);
		return placed;
	}
	
}
