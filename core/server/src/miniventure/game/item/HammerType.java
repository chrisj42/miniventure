package miniventure.game.item;

import java.util.Arrays;
import java.util.LinkedList;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.network.GameProtocol.SerialRecipe;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public enum HammerType {
	
	Simple_Hammer(ObjectRecipeSet.SIMPLE);
	
	private static SerialRecipe[] fetchRecipes(ObjectRecipeSet maxRecipeSet) {
		// add the hand-craftable items
		LinkedList<SerialRecipe> recipes = new LinkedList<>(Arrays.asList(ItemRecipeSet.HAND.getSerialRecipes()));
		// add the objects for this and previous object recipe sets
		for(int i = 0; i <= maxRecipeSet.ordinal(); i++)
			recipes.addAll(Arrays.asList(ObjectRecipeSet.values[i].getSerialRecipes()));
		return recipes.toArray(new SerialRecipe[0]);
	}
	
	private static SerialRecipe[] NO_HAMMER = null;
	public static SerialRecipe[] getHandRecipes() {
		if(NO_HAMMER == null)
			NO_HAMMER = fetchRecipes(ObjectRecipeSet.HAND);
		return NO_HAMMER;
	}
	
	private SerialRecipe[] availableRecipes = null;
	private final ObjectRecipeSet maxRecipeSet;
	
	HammerType(ObjectRecipeSet maxRecipeSet) {
		this.maxRecipeSet = maxRecipeSet;
	}
	
	public SerialRecipe[] getRecipes() {
		if(availableRecipes == null)
			availableRecipes = fetchRecipes(maxRecipeSet);
		return availableRecipes;
	}
	
	public HammerItem get() { return new HammerItem(); }
	
	public class HammerItem extends EnumItem {
		
		private HammerItem() {
			super(ItemType.Hammer, HammerType.this, GameCore.icons.get("items/tools/"+HammerType.this.name().toLowerCase()));
		}
		
		public HammerType getHammerType() { return HammerType.this; }
		
		@NotNull @Override
		public Player.CursorHighlight getHighlightMode() {
			return CursorHighlight.INVISIBLE;
		}
		
		@Override
		public EquipmentSlot getEquipmentType() {
			return EquipmentSlot.HAMMER;
		}
	}
}
