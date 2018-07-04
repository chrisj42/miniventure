package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.DestructionManager.RequiredTool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoorTile extends SurfaceTileType {
	
	static DoorTile getOpenDoor(@NotNull TileTypeEnum enumType) {
		return new DoorTile(enumType, true, new DestructionManager(enumType, new ItemDrop(TileItem.get(TileTypeEnum.DOOR_CLOSED)), new RequiredTool(ToolType.Axe)), new TransitionManager(enumType)
			.addEntranceAnimations(new TransitionAnimation("open", 3/24f, TileTypeEnum.DOOR_CLOSED))
			.addExitAnimations(new TransitionAnimation("close", 3/24f, TileTypeEnum.DOOR_CLOSED))
		) {
			@Override
			public boolean interact(@NotNull Tile tile, Player player, @Nullable Item item) {
				tile.replaceTile(TileTypeEnum.DOOR_CLOSED.getTileType(tile.getWorld()));
				return true;
			}
		};
	}
	
	static DoorTile getClosedDoor(@NotNull TileTypeEnum enumType) {
		return new DoorTile(enumType, false, new DestructionManager(enumType, new RequiredTool(ToolType.Axe)), new TransitionManager(enumType)) {
			@Override
			public boolean interact(@NotNull Tile tile, Player player, @Nullable Item item) {
				tile.replaceTile(TileTypeEnum.DOOR_OPEN.getTileType(tile.getWorld()));
				return true;
			}
		};
	}
	
	private DoorTile(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TransitionManager transitionManager) {
		super(enumType, walkable, destructionManager, new TileTypeRenderer(enumType, false, new ConnectionManager(enumType, RenderStyle.SINGLE_FRAME), OverlapManager.NONE(enumType), transitionManager));
	}
}
