package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialRecipe;

public enum ObjectRecipeSet implements RecipeSet<ObjectRecipe> {
	
	SIMPLE(
		new ObjectRecipe(ConstructableObjectType.Stone_Path,
			new ServerItemStack(ResourceType.Stone.get(), 2)
		),
		new ObjectRecipe(ConstructableObjectType.Door,
			new ServerItemStack(ResourceType.Log.get(), 2)
		),
		new ObjectRecipe(ConstructableObjectType.Wood_Wall,
			new ServerItemStack(ResourceType.Log.get(), 3)
		),
		new ObjectRecipe(ConstructableObjectType.Stone_Wall,
			new ServerItemStack(ResourceType.Stone.get(), 3)
		),
		new ObjectRecipe(ConstructableObjectType.Stone_Floor,
			new ServerItemStack(ResourceType.Stone.get(), 3)
		)
	); // hammer types
	
	private final ObjectRecipe[] recipes;
	private final SerialRecipe[] serializedRecipes;
	
	ObjectRecipeSet(ObjectRecipe... recipes) {
		this.recipes = recipes;
		serializedRecipes = RecipeSet.serializeRecipes(recipes, ordinal());
	}
	
	@Override
	public ObjectRecipe getRecipe(int index) {
		return recipes[index];
	}
	
	@Override
	public SerialRecipe[] getSerialRecipes() { return serializedRecipes; }
	
	public static final ObjectRecipeSet[] values = ObjectRecipeSet.values();
}
