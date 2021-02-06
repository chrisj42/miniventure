package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialRecipe;

public enum ItemRecipeSet implements RecipeSet<ItemRecipe> {
	
	// crafter types
	HAND(
		// OTHER
		
		new ItemRecipe(new HammerItem(ObjectRecipeSet.BASIC),
			ResourceType.Stick.stack(2),
			ResourceType.Stone.stack(2),
			ResourceType.Reed.stack(2)
		),
		
		new ItemRecipe(PlaceableItemType.Torch,
			ResourceType.Stick,
			ResourceType.Coal
		)
	),
	
	WORKBENCH(
		// CRUDE
		
		new ItemRecipe(ToolType.Crude_Axe,
			ResourceType.Stick.stack(2),
			ResourceType.Reed.stack(3),
			ResourceType.Stone
		),
		
		new ItemRecipe(ToolType.Crude_Pickaxe,
			ResourceType.Stick.stack(2),
			ResourceType.Reed.stack(3),
			ResourceType.Stone.stack(2)
		),
		
		new ItemRecipe(ToolType.Crude_Shovel,
			ResourceType.Stick.stack(2),
			ResourceType.Reed.stack(3),
			ResourceType.Stone
		),
		
		new ItemRecipe(ToolType.Club,
			ResourceType.Stick.stack(3),
			ResourceType.Reed.stack(3)
		)
	),
	
	ANVIL(
		// metal tools and any other metal things
		
		new ItemRecipe(ToolType.Iron_Axe,
			ResourceType.Iron,
			ResourceType.Plank
		),
		
		new ItemRecipe(ToolType.Iron_Pickaxe,
			ResourceType.Iron.stack(2),
			ResourceType.Plank
		),
		
		new ItemRecipe(ToolType.Iron_Shovel,
			ResourceType.Iron,
			ResourceType.Plank
		),
		
		new ItemRecipe(ToolType.Sword,
			ResourceType.Iron.stack(3),
			ResourceType.Stone,
			ResourceType.Reed.stack(5)
		)
	),
	
	OVEN(
		// food types
		new ItemRecipe(FoodType.Cooked_Meat,
			FoodType.Raw_Meat
		),
		new ItemRecipe(FoodType.Cooked_Bacon,
			FoodType.Raw_Bacon
		)
	),
	
	FURNACE(
		// ores
	),
	
	LAB(
		// lazors
	);
	
	private final ItemRecipe[] recipes;
	private final SerialRecipe[] serializedRecipes;
	
	ItemRecipeSet(ItemRecipe... recipes) {
		this.recipes = recipes;
		serializedRecipes = RecipeSet.serializeRecipes(recipes, ordinal());
	}
	
	@Override
	public ItemRecipe getRecipe(int index) {
		return recipes[index];
	}
	
	@Override
	public SerialRecipe[] getSerialRecipes() {
		return serializedRecipes;
	}
	
	public static final ItemRecipeSet[] values = ItemRecipeSet.values();
}
