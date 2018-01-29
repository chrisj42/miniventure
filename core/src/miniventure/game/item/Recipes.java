package miniventure.game.item;

import miniventure.game.item.ToolItem.Material;
import miniventure.game.world.tile.TileType;

public class Recipes {
	
	// TODO later this can be an enum, and the "crafter" type furniture will accept this enum type.... actually no, that's silly. perhaps the recipes should be defined in the crafter classes..?
	
	private Recipes() {}
	
	public static final Recipe[] recipes = new Recipe[] {
		new Recipe(new ItemStack(new ToolItem(ToolType.Pickaxe, Material.Wood), 1),
			new ItemStack(ResourceItem.Log.get(), 5)
		),
		
		new Recipe(new ItemStack(new ToolItem(ToolType.Pickaxe, Material.Stone), 1),
			new ItemStack(TileItem.get(TileType.STONE), 4),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(new ItemStack(new ToolItem(ToolType.Axe, Material.Wood), 1),
			new ItemStack(ResourceItem.Log.get(), 4)
		),
		
		new Recipe(new ItemStack(new ToolItem(ToolType.Axe, Material.Stone), 1),
			new ItemStack(TileItem.get(TileType.STONE), 3),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(new ItemStack(TileItem.get(TileType.WATER), 1),
			new ItemStack(TileItem.get(TileType.SAND), 1),
			new ItemStack(TileItem.get(TileType.DIRT), 1),
			new ItemStack(TileItem.get(TileType.GRASS), 1)
		)
	};
	
	//private static Recipe makeRecipe(Item item, int count)
}
