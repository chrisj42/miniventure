package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialItemStack;
import miniventure.game.network.GameProtocol.SerialRecipe;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.function.MapFunction;

public interface RecipeSet<R extends Recipe> {
	
	R getRecipe(int index);
	
	SerialRecipe[] getSerialRecipes();
	
	static SerialRecipe[] serializeRecipes(Recipe[] recipes, int setOrdinal) {
		SerialRecipe[] serial = new SerialRecipe[recipes.length];
		for (int i = 0; i < recipes.length; i++)
			serial[i] = new SerialRecipe(setOrdinal, i, recipes[i].getResult().serialize(), ArrayUtils.mapArray(recipes[i].getCosts(), SerialItemStack.class, ServerItemStack::serialize));
		return serial;
	}
}
