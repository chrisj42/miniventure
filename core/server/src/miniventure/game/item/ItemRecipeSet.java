package miniventure.game.item;

import miniventure.game.network.GameProtocol.SerialRecipe;

public enum ItemRecipeSet implements RecipeSet<ItemRecipe> {
	
	// crafter types
	HAND(
		new ItemRecipe(HammerType.Simple_Hammer.get(),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		/*new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Crude),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Crude),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Crude),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Crude),
			new ServerItemStack(ResourceType.Flint.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),*/
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Basic),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Basic),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Basic),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Basic),
			new ServerItemStack(ResourceType.Stone.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		/*new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Sturdy),
			new ServerItemStack(ResourceType.Iron.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Sturdy),
			new ServerItemStack(ResourceType.Iron.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Sturdy),
			new ServerItemStack(ResourceType.Iron.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Sturdy),
			new ServerItemStack(ResourceType.Iron.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Fine),
			new ServerItemStack(ResourceType.Tungsten.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Fine),
			new ServerItemStack(ResourceType.Tungsten.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Fine),
			new ServerItemStack(ResourceType.Tungsten.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Fine),
			new ServerItemStack(ResourceType.Tungsten.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Superior),
			new ServerItemStack(ResourceType.Ruby.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Superior),
			new ServerItemStack(ResourceType.Ruby.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Superior),
			new ServerItemStack(ResourceType.Ruby.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Superior),
			new ServerItemStack(ResourceType.Ruby.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),*/
		
		new ItemRecipe(new ServerItemStack(PlaceableItemType.Torch.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1),
			new ServerItemStack(ResourceType.Coal.get(), 1)
		)
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
	
	private final ItemRecipe[] recipes;
	private final SerialRecipe[] serializedRecipes;
	
	ItemRecipeSet(ItemRecipe... recipes) {
		this.recipes = recipes;
		serializedRecipes = RecipeSet.serializeRecipes(ordinal(), false, recipes, ItemStack::serialize);
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
