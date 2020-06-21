package miniventure.game.item.recipe;

import miniventure.game.item.ConstructableObjectType;
import miniventure.game.item.Item;
import miniventure.game.item.ItemEnum;
import miniventure.game.item.ItemStack;
import miniventure.game.item.ResourceType;

public enum ObjectRecipeSet implements RecipeSet<ObjectRecipe>/*, ItemEnum*/ {
	
	SIMPLE(
		new ObjectRecipe(ConstructableObjectType.Stone_Path,
			new ItemStack(ResourceType.Stone.get(), 2)
		),
		new ObjectRecipe(ConstructableObjectType.Door,
			new ItemStack(ResourceType.Log.get(), 2)
		),
		new ObjectRecipe(ConstructableObjectType.Wood_Wall,
			new ItemStack(ResourceType.Log.get(), 3)
		),
		new ObjectRecipe(ConstructableObjectType.Stone_Wall,
			new ItemStack(ResourceType.Stone.get(), 3)
		),
		new ObjectRecipe(ConstructableObjectType.Stone_Floor,
			new ItemStack(ResourceType.Stone.get(), 3)
		)
	); // hammer types
	
	private final ObjectRecipe[] recipes;
	// private final SerialRecipe[] serializedRecipes;
	
	ObjectRecipeSet(ObjectRecipe... recipes) {
		this.recipes = recipes;
		// serializedRecipes = RecipeSet.serializeRecipes(recipes, ordinal());
	}
	
	@Override
	public ObjectRecipe getRecipe(int index) {
		return recipes[index];
	}
	
	@Override
	public int getRecipeCount() {
		return recipes.length;
	}
	
	// @Override
	// public SerialRecipe[] getSerialRecipes() { return serializedRecipes; }
	
	public static final ObjectRecipeSet[] values = ObjectRecipeSet.values();
	
	/*@Override
	public Item get() {
		return null;
	}*/
}
