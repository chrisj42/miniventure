package miniventure.game.item;

import miniventure.game.item.ToolItem.Material;
import miniventure.game.world.tile.TileType;

public class Recipes {
	
	// TODO later this can be an enum, and the "crafter" type furniture will accept this enum type.... actually no, that's silly. perhaps the recipes should be defined in the crafter classes..?
	
	private Recipes() {}
	
	public static final Recipe[] recipes = new Recipe[] {
		new Recipe(new ItemStack(TileItem.get(TileType.ROCK), 2),
			new ItemStack(TileItem.get(TileType.GRASS), 3),
			new ItemStack(TileItem.get(TileType.DIRT), 3)
		),
		
		new Recipe(new ItemStack(new ToolItem(ToolType.Pickaxe, Material.Wood), 1),
			new ItemStack(TileItem.get(TileType.GRASS), 5),
			new ItemStack(TileItem.get(TileType.DIRT), 8)
		),
		
		new Recipe(new ItemStack(TileItem.get(TileType.WATER), 1),
			new ItemStack(TileItem.get(TileType.GRASS), 4)
		)
	};
	
	//private static Recipe makeRecipe(Item item, int count)
}
