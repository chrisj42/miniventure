package miniventure.game.item.recipe;

public interface RecipeSet<R extends Recipe> {
	
	R getRecipe(int index);
	
	int getRecipeCount();
	
	/*SerialRecipe[] getSerialRecipes();
	
	static SerialRecipe[] serializeRecipes(Recipe[] recipes, int setOrdinal) {
		SerialRecipe[] serial = new SerialRecipe[recipes.length];
		for (int i = 0; i < recipes.length; i++)
			serial[i] = new SerialRecipe(setOrdinal, i, recipes[i].getResult().serialize(), ArrayUtils.mapArray(recipes[i].getCosts(), SerialItemStack.class, ItemStack::serialize));
		return serial;
	}*/
}
