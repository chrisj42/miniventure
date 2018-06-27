package miniventure.game.item;

import miniventure.game.item.ToolItem.Material;
import miniventure.game.world.tile.TileType;

public class Recipes {
	
	private Recipes() {}
	
	public static final Recipe[] recipes = new Recipe[] {
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Wood),
			new ItemStack(ResourceItem.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Stone),
			new ItemStack(TileItem.get(TileType.STONE), 1),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Wood),
			new ItemStack(ResourceItem.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Stone),
			new ItemStack(TileItem.get(TileType.STONE), 2),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Wood),
			new ItemStack(ResourceItem.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Stone),
			new ItemStack(TileItem.get(TileType.STONE), 2),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Wood),
			new ItemStack(ResourceItem.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Stone),
			new ItemStack(TileItem.get(TileType.STONE), 1),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(new ItemStack(TileItem.get(TileType.TORCH), 2),
			new ItemStack(ResourceItem.Log.get(), 1)
		),
		
		new Recipe(TileItem.get(TileType.DOOR_CLOSED),
			new ItemStack(ResourceItem.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileType.WOOD_WALL),
			new ItemStack(ResourceItem.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileType.STONE_WALL),
			new ItemStack(TileItem.get(TileType.STONE), 3)
		),
		
		new Recipe(TileItem.get(TileType.STONE_FLOOR),
			new ItemStack(TileItem.get(TileType.STONE), 1)
		)
	};
}
