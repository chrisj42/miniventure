package miniventure.game.item;

import miniventure.game.GameProtocol.SerialRecipe;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.world.tile.TileTypeEnum;

public class Recipes {
	
	private Recipes() {}
	
	public static final Recipe[] recipes = new Recipe[] {
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Flint),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Flint),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Flint),
			new ServerItemStack(ResourceType.Flint.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Flint),
			new ServerItemStack(ResourceType.Flint.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Stone),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Stone),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Stone),
			new ServerItemStack(ResourceType.Stone.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Stone),
			new ServerItemStack(ResourceType.Stone.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Iron),
			new ServerItemStack(ResourceType.Iron.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Iron),
			new ServerItemStack(ResourceType.Iron.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Iron),
			new ServerItemStack(ResourceType.Iron.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Iron),
			new ServerItemStack(ResourceType.Iron.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Tungsten),
			new ServerItemStack(ResourceType.Tungsten.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Tungsten),
			new ServerItemStack(ResourceType.Tungsten.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Tungsten),
			new ServerItemStack(ResourceType.Tungsten.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Tungsten),
			new ServerItemStack(ResourceType.Tungsten.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Pickaxe, Material.Ruby),
			new ServerItemStack(ResourceType.Ruby.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Sword, Material.Ruby),
			new ServerItemStack(ResourceType.Ruby.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Axe, Material.Ruby),
			new ServerItemStack(ResourceType.Ruby.get(), 2),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ToolItem(ToolType.Shovel, Material.Ruby),
			new ServerItemStack(ResourceType.Ruby.get(), 1),
			new ServerItemStack(ResourceType.Log.get(), 1)
		),
		
		new Recipe(new ServerItemStack(TileItem.get(TileTypeEnum.TORCH), 2),
			new ServerItemStack(ResourceType.Log.get(), 1),
			new ServerItemStack(ResourceType.Coal.get(), 1)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.CLOSED_DOOR),
			new ServerItemStack(ResourceType.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.WOOD_WALL),
			new ServerItemStack(ResourceType.Log.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.STONE_WALL),
			new ServerItemStack(ResourceType.Stone.get(), 3)
		),
		
		new Recipe(TileItem.get(TileTypeEnum.STONE_FLOOR),
			new ServerItemStack(ResourceType.Stone.get(), 1)
		),
		
		new Recipe(new ServerItemStack(TileItem.get(TileTypeEnum.STONE_PATH), 2),
			new ServerItemStack(ResourceType.Stone.get(), 1)
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
