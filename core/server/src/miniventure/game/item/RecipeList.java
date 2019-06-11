package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialRecipe;

// sorts recipes into various recipe lists
public enum RecipeList {
	
	Personal(
		// torch
		// all the other crafters
		// any other simple recipes that shouldn't need some sort of crafter to make
	),
	
	Workbench(
		// pre-metal tools mainly
	),
	
	Anvil(
		// metal tools and any other metal things
	),
	
	ArchitectTable(
		// doors, walls, floors; everything relating to building construction
	),
	
	Oven(
		// food types
	),
	
	Furnace(
		// ores
	),
	
	SciLab(
		// lazors
	);
	
	public final Recipe[] recipes;
	
	RecipeList(Recipe... recipes) {
		this.recipes = recipes;
	}
	
	public SerialRecipe[] serializeList() {
		SerialRecipe[] serialRecipes = new SerialRecipe[recipes.length];
		final int ord = ordinal();
		for(int i = 0; i < recipes.length; i++) {
			Recipe r = recipes[i];
			serialRecipes[i] = new SerialRecipe(ord, i, r.getResult(), r.getCosts(),
				r instanceof Blueprint ? ((TileItem)r.getResult().item).getResult() : null
			);
		}
		
		return serialRecipes;
	}
}
