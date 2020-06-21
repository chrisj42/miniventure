package miniventure.game.world.entity.mob.player;

import java.util.Objects;

import miniventure.game.item.Item;
import miniventure.game.item.ItemType;
import miniventure.game.item.Result;
import miniventure.game.item.recipe.ObjectRecipe;
import miniventure.game.item.recipe.ObjectRecipeSet;
import miniventure.game.world.WorldObject;
import miniventure.game.world.tile.Tile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HammerItem extends Item {
	
	// this class is the only item class so far that has mutable data.
	// I'm allowing it because 
	
	
	public static final CursorHighlight CONSTRUCTION_CURSOR = CursorHighlight.TILE_IN_RADIUS;
	
	@Nullable
	private final ObjectRecipe selectedRecipe;
	
	private final ObjectRecipeSet recipeSet;
	
	public HammerItem(@NotNull ObjectRecipeSet recipeSet) { this(recipeSet, null); }
	public HammerItem(@NotNull ObjectRecipeSet recipeSet, @Nullable ObjectRecipe selectedRecipe) {
		super(ItemType.Hammer, recipeSet.name().toLowerCase()+"_hammer", "tools");
		this.recipeSet = recipeSet;
		this.selectedRecipe = selectedRecipe;
	}
	
	public ObjectRecipeSet getRecipeSet() { return recipeSet; }
	
	@NotNull @Override
	public CursorHighlight getHighlightMode() {
		return selectedRecipe == null ? CursorHighlight.INVISIBLE : CONSTRUCTION_CURSOR;
	}
	
	// this might change later if I give the hammer durability, but for now this is how it goes.
	@Override
	public Item getUsedItem() {
		return this;
	}
	
	@Override
	public Result attack(WorldObject obj, Player player) {
		if(selectedRecipe == null || !(obj instanceof Tile))
			return Result.NONE;
		
		Tile tile =  (Tile) obj;
		return selectedRecipe.tryCraft(tile, player.getInv()) ? Result.USED : Result.INTERACT;
	}
	
	@Override
	public Result interact(Player player) {
		player.getInv().useHammer(this);
		return Result.INTERACT;
	}
	
	/*@Override
	protected void addSerialData(ItemDataMap map) {
		super.addSerialData(map);
		if(selection != null)
			map.add(ItemDataTag.CursorSprite, recipeSet.getRecipe(selection).getResult().item.getFetchableTexture());
	}*/
	
	@Override
	protected String compileSaveData() {
		return recipeSet.name();
	}
	
	public HammerItem setRecipe(@Nullable ObjectRecipe selectedRecipe) {
		if(Objects.equals(selectedRecipe, this.selectedRecipe))
			return this;
		return new HammerItem(recipeSet, selectedRecipe);
	}
	
	// to ensure the proper item is selected, we'll use strict object equality.
	@Override
	public boolean equals(Object other) {
		return this == other;
	}
	
	// the only requirement for hash code is that equal objects produce equal hash codes; since an object is equal only to itself, it really doesn't matter what the hash code is.
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
