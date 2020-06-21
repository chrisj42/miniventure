package miniventure.game.item.recipe;

import miniventure.game.item.ConstructableObjectType;
import miniventure.game.item.ItemStack;
import miniventure.game.item.ItemType.EphemeralItem;
import miniventure.game.world.entity.mob.player.CursorHighlight;
import miniventure.game.world.entity.mob.player.HammerItem;
import miniventure.game.world.entity.mob.player.PlayerInventory;
import miniventure.game.world.tile.Tile;

import org.jetbrains.annotations.NotNull;

public class ObjectRecipe extends Recipe {
	
	private final ConstructableObjectType objectType;
	
	public ObjectRecipe(@NotNull ConstructableObjectType result, @NotNull ItemStack... costs) {
		super(new ItemStack(new BlueprintItem(result), 0), costs);
		this.objectType = result;
	}
	
	public boolean tryCraft(Tile tile, PlayerInventory inv) {
		if(!canCraft(inv))
			return false;
		
		boolean placed = objectType.tryPlace(tile, inv.getPlayer());
		if(placed)
			deductCosts(inv);
		return placed;
	}
	
	@Override
	public boolean isItemRecipe() {
		return false;
	}
	
	@Override
	public void onSelect(PlayerInventory inv) {
		inv.setHammerRecipe(this);
	}
	
	private static class BlueprintItem extends EphemeralItem {
		
		// note that is is not actually what is set as the client's active item; this class is solely for internal use by BlueprintRecipes passed to the client.
		
		// private BlueprintItem() { super(name()); }
		private BlueprintItem(@NotNull ConstructableObjectType objectType) {
			super(objectType.name(), objectType.getTexture());
		}
		
		@NotNull @Override
		public CursorHighlight getHighlightMode() {
			return HammerItem.CONSTRUCTION_CURSOR;
		}
	}
}
