package miniventure.game.item;

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
	
}
