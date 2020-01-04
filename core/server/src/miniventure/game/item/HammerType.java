package miniventure.game.item;

import java.util.Arrays;
import java.util.LinkedList;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.network.GameProtocol.SerialRecipe;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum HammerType {
	
	Simple(ObjectRecipeSet.HAND);
	
	private static SerialRecipe[] fetchRecipes(ObjectRecipeSet maxRecipeSet) {
		// add the hand-craftable items
		LinkedList<SerialRecipe> recipes = new LinkedList<>(Arrays.asList(ItemRecipeSet.HAND.getSerialRecipes()));
		// add the objects for this and previous object recipe sets
		for(int i = 0; i <= maxRecipeSet.ordinal(); i++)
			recipes.addAll(Arrays.asList(ObjectRecipeSet.values[i].getSerialRecipes()));
		return recipes.toArray(new SerialRecipe[0]);
	}
	
	private static final SerialRecipe[] NO_HAMMER = fetchRecipes(ObjectRecipeSet.HAND);
	public static SerialRecipe[] getHandRecipes() { return NO_HAMMER; }
	
	private final SerialRecipe[] availableRecipes;
	
	HammerType(ObjectRecipeSet maxRecipeSet) {
		this.availableRecipes = fetchRecipes(maxRecipeSet);
	}
	
	public SerialRecipe[] getRecipes() { return availableRecipes; }
	
	public HammerItem get() { return new HammerItem(); }
	
	public class HammerItem extends EnumItem {
		
		private HammerItem() {
			super(ItemType.Hammer, HammerType.this, GameCore.icons.get("items/tool/"+HammerType.this.name().toLowerCase()+"_hammer"));
		}
		
		@NotNull @Override
		public Player.CursorHighlight getHighlightMode() {
			return CursorHighlight.INVISIBLE;
		}
		
		@Override
		public EquipmentSlot getEquipmentType() {
			return EquipmentSlot.HAMMER;
		}
		
		// todo on interaction, equip the hammer.
	}
}
