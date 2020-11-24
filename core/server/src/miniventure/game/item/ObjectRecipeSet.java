package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialRecipe;

public enum ObjectRecipeSet implements RecipeSet<ObjectRecipe> {
	
	// toolkit types
	
	HAND(
		
	),
	
	BASIC(
		new ObjectRecipe(ConstructableObjectType.Stone_Path,
			ResourceType.Stone.stack(2)
		),
		new ObjectRecipe(ConstructableObjectType.Door,
			ResourceType.Plank.stack(2)
		),
		new ObjectRecipe(ConstructableObjectType.Wood_Wall,
			ResourceType.Plank.stack(3)
		),
		new ObjectRecipe(ConstructableObjectType.Stone_Wall,
			ResourceType.Stone.stack(3)
		),
		new ObjectRecipe(ConstructableObjectType.Stone_Floor,
			ResourceType.Stone.stack(3)
		)
	)
	;
	
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
