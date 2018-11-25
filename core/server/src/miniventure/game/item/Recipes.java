package miniventure.game.item;

import miniventure.game.GameProtocol.SerialRecipe;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.world.tile.TileTypeEnum;

public class Recipes {
	
	private Recipes() {}
	
	public static final Recipe[] recipes = new Recipe[] {
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Flint),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Stone),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Flint),
			new ServerItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Stone),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Flint),
			new ServerItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Stone),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Flint),
			new ServerItemStack(ResourceType.Log.get(), 2)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Stone),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ServerItemStack(TileItem.get(TileTypeEnum.TORCH), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.CLOSED_DOOR),
			new ServerItemStack(ResourceType.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.WOOD_WALL),
			new ServerItemStack(ResourceType.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.STONE_WALL),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.STONE_FLOOR),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 1)
		),
		
		new Recipe(new ServerItemStack(TileItem.get(TileTypeEnum.STONE_PATH), 2),
			new ServerItemStack(TileItem.get(TileTypeEnum.STONE), 1)
		)
	};
	
	public static final SerialRecipe[] serializeRecipes() {
		SerialRecipe[] serialRecipes = new SerialRecipe[Recipes.recipes.length];
		for(int i = 0; i < recipes.length; i++) {
			Recipe r = recipes[i];
			serialRecipes[i] = new SerialRecipe(r.getResult(), r.getCosts());
		}
		
		return serialRecipes;
	}
}
