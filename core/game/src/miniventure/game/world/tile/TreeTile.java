package miniventure.game.world.tile;

import miniventure.game.item.FoodType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.PropertyTag;

import org.jetbrains.annotations.NotNull;

public class TreeTile extends TileType {
	
	TreeTile(@NotNull TileTypeEnum enumType) {
		super(enumType, false,
			new DataMap(PropertyTag.ZOffset.as(0.25f)),
			
			new DestructionManager(enumType, 24,
				new PreferredTool(ToolType.Axe, 2),
				new ItemDrop(ResourceType.Log.get(), 1, 2),
				new ItemDrop(FoodType.Apple.get(), 0, 2, 0.2f)
			),
			
			new TileTypeRenderer(enumType, false,
				new ConnectionManager(enumType, RenderStyle.SINGLE_FRAME, enumType)
			),
			
			new UpdateManager(enumType)
		);
	}
	
}
