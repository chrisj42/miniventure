package miniventure.game.item;

import miniventure.game.item.ToolItem.Material;
import miniventure.game.world.tile.TileType.TileTypeEnum;

public class Recipes {
	
	private Recipes() {}
	
	public static final Recipe[] recipes = new Recipe[] {
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Wood),
			new ItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Stone),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Wood),
			new ItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Stone),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Wood),
			new ItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Stone),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Wood),
			new ItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Stone),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 1),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ItemStack(TileItem.get(TileTypeEnum.TORCH), 2),
			new ItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.DOOR_CLOSED),
			new ItemStack(ResourceType.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.WOOD_WALL),
			new ItemStack(ResourceType.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.STONE_WALL),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.STONE_FLOOR),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 1)
		),
		
		new Recipe(new ItemStack(TileItem.get(TileTypeEnum.PATH_STONE), 2),
			new ItemStack(TileItem.get(TileTypeEnum.STONE), 1)
		)
	};
}
