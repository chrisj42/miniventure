package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialRecipe;
import miniventure.game.util.function.MapFunction;

public interface RecipeSet<R extends Recipe<?>> {
	
	static <T> SerialRecipe[] serializeRecipes(int setOrdinal, boolean isBlueprint, Recipe<T>[] recipes, MapFunction<T, String[]> resultDataFetcher) {
		SerialRecipe[] serial = new SerialRecipe[recipes.length];
		for(int i = 0; i < recipes.length; i++) {
			Recipe<T> r = recipes[i];
			serial[i] = new SerialRecipe(setOrdinal, i, resultDataFetcher.get(r.getResult()), r.getCosts(), isBlueprint);
		}
		
		return serial;
	}
	
	R getRecipe(int index);
	
	SerialRecipe[] getSerialRecipes();
}
