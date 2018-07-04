package miniventure.game.world.tile;

import miniventure.game.item.FoodItem;
import miniventure.game.item.ResourceItem;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.DestructionManager.PreferredTool;

import org.jetbrains.annotations.NotNull;

public class TreeTile extends SurfaceTileType {
	
	TreeTile(@NotNull TileTypeEnum enumType) {
		super(enumType, false, new DestructionManager(enumType, 24, new PreferredTool(ToolType.Axe, 2), new ItemDrop(ResourceItem.Log.get()/*, 1, 2, 0.25f*/), new ItemDrop(FoodItem.Apple.get(), 0, 2, 0.2f)), new TileTypeRenderer(enumType, false, new ConnectionManager(enumType, RenderStyle.SINGLE_FRAME, enumType)));
	}
	
}
