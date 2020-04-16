package miniventure.game.item;

import java.util.Objects;

import miniventure.game.item.ItemDataTag.ItemDataMap;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HammerItem extends ServerItem {
	
	public static final CursorHighlight CONSTRUCTION_CURSOR = CursorHighlight.TILE_IN_RADIUS;
	
	@Nullable
	private final Integer selection;
	
	private final ObjectRecipeSet recipeSet;
	
	public HammerItem(@NotNull ObjectRecipeSet recipeSet) { this(recipeSet, null); }
	public HammerItem(@NotNull ObjectRecipeSet recipeSet, @Nullable Integer selection) {
		super(ItemType.Hammer, recipeSet.name().toLowerCase()+"_hammer", "tools");
		this.recipeSet = recipeSet;
		this.selection = selection;
	}
	
	public ObjectRecipeSet getRecipeSet() { return recipeSet; }
	
	@NotNull @Override
	public Player.CursorHighlight getHighlightMode() {
		return selection == null ? CursorHighlight.INVISIBLE : CONSTRUCTION_CURSOR;
	}
	
	// this might change later if I give the hammer durability, but for now this is how it goes.
	@Override
	public ServerItem getUsedItem() {
		return this;
	}
	
	@Override
	public Result attack(WorldObject obj, ServerPlayer player) {
		if(selection == null || !(obj instanceof ServerTile))
			return Result.NONE;
		
		ObjectRecipe recipe = recipeSet.getRecipe(selection);
		ServerTile tile = (ServerTile) obj;
		
		return player.tryBuild(recipe, tile) ? Result.USED : Result.INTERACT;
	}
	
	@Override
	public Result interact(ServerPlayer player) {
		player.useHammer(this);
		return Result.INTERACT;
	}
	
	@Override
	protected void addSerialData(ItemDataMap map) {
		super.addSerialData(map);
		if(selection != null)
			map.add(ItemDataTag.CursorSprite, recipeSet.getRecipe(selection).getResult().item.getFetchableTexture());
	}
	
	@Override
	protected String[] save() {
		return new String[] {
			getType().name(),
			recipeSet.name(),
			selection == null ? "null" : String.valueOf(selection)
		};
	}
	
	public HammerItem setSelection(@Nullable Integer selection) {
		if(Objects.equals(selection, this.selection))
			return this;
		return new HammerItem(recipeSet, selection);
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
