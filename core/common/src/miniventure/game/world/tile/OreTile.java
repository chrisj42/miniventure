package miniventure.game.world.tile;

import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.DestructionManager.PreferredTool;

public class OreTile extends TileType {
	
	OreTile(TileTypeEnum type, int health) {
		super(type, false,
			new DestructionManager(type, health,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(ResourceType.Iron.get(), 3, 4)
			),
			
			new TileTypeRenderer(type, false,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		);
	}
	
}
