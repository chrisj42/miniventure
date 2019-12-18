package miniventure.game.item;

import java.util.Collection;
import java.util.LinkedList;

import miniventure.game.network.GameProtocol.SerialRecipe;

public enum ObjectRecipeSet implements RecipeSet<ObjectRecipe> {
	
	HAND(
		new ObjectRecipe(ConstructableObjectType.Stone_Path,
			new ServerItemStack(ResourceType.Stone.get(), 2)
		),
		new ObjectRecipe(ConstructableObjectType.Door,
			new ServerItemStack(ResourceType.Log.get(), 2)
		)
	); // hammer types
	
	private final ObjectRecipe[] recipes;
	private final SerialRecipe[] serializedRecipes;
	
	ObjectRecipeSet(ObjectRecipe... recipes) {
		this.recipes = recipes;
		serializedRecipes = RecipeSet.serializeRecipes(ordinal(), true, recipes, type -> type.getItem().serialize());
	}
	
	@Override
	public ObjectRecipe getRecipe(int index) {
		return recipes[index];
	}
	
	@Override
	public SerialRecipe[] getSerialRecipes() { return serializedRecipes; }
	
	public static final ObjectRecipeSet[] values = ObjectRecipeSet.values();
}
