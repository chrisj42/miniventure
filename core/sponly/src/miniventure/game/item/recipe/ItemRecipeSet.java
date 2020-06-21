package miniventure.game.item.recipe;

import miniventure.game.item.ItemStack;
import miniventure.game.item.MaterialQuality;
import miniventure.game.item.PlaceableItemType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolItem;
import miniventure.game.world.entity.mob.player.HammerItem;

public enum ItemRecipeSet implements RecipeSet<ItemRecipe> {
	
	// crafter types
	HAND(
		new ItemRecipe(new HammerItem(ObjectRecipeSet.SIMPLE),
			new ItemStack(ResourceType.Stone.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		/*new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Crude),
			new ItemStack(ResourceType.Flint.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Crude),
			new ItemStack(ResourceType.Flint.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Crude),
			new ItemStack(ResourceType.Flint.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Crude),
			new ItemStack(ResourceType.Flint.get(), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),*/
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Basic),
			new ItemStack(ResourceType.Stone.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Basic),
			new ItemStack(ResourceType.Stone.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Basic),
			new ItemStack(ResourceType.Stone.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Basic),
			new ItemStack(ResourceType.Stone.get(), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		/*new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Sturdy),
			new ItemStack(ResourceType.Iron.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Sturdy),
			new ItemStack(ResourceType.Iron.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Sturdy),
			new ItemStack(ResourceType.Iron.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Sturdy),
			new ItemStack(ResourceType.Iron.get(), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Fine),
			new ItemStack(ResourceType.Tungsten.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Fine),
			new ItemStack(ResourceType.Tungsten.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Fine),
			new ItemStack(ResourceType.Tungsten.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Fine),
			new ItemStack(ResourceType.Tungsten.get(), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Pickaxe, MaterialQuality.Superior),
			new ItemStack(ResourceType.Ruby.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Sword, MaterialQuality.Superior),
			new ItemStack(ResourceType.Ruby.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Axe, MaterialQuality.Superior),
			new ItemStack(ResourceType.Ruby.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new ItemRecipe(new ToolItem(ToolItem.ToolType.Shovel, MaterialQuality.Superior),
			new ItemStack(ResourceType.Ruby.get(), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),*/
		
		new ItemRecipe(new ItemStack(PlaceableItemType.Torch.get(), 2),
			new ItemStack(ResourceType.Log.get(), 1),
			new ItemStack(ResourceType.Coal.get(), 1)
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
	// private final SerialRecipe[] serializedRecipes;
	
	ItemRecipeSet(ItemRecipe... recipes) {
		this.recipes = recipes;
		// serializedRecipes = RecipeSet.serializeRecipes(recipes, ordinal());
	}
	
	@Override
	public ItemRecipe getRecipe(int index) {
		return recipes[index];
	}
	
	@Override
	public int getRecipeCount() {
		return recipes.length;
	}
	
	/*@Override
	public SerialRecipe[] getSerialRecipes() {
		return serializedRecipes;
	}*/
	
	public static final ItemRecipeSet[] values = ItemRecipeSet.values();
}
