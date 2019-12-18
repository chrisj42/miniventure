package miniventure.game.item;

import miniventure.game.util.function.FetchFunction;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.tile.TileTypeEnum.TypeGroup;

import org.jetbrains.annotations.NotNull;

public interface PlacementAttempt {
	
	boolean tryPlace(ServerTile tile, ServerPlayer player);
	
	static PlacementAttempt getAttempt(PlacementCheck check, PlacementAction action) {
		return (tile, player) -> {
			boolean success = check.canPlace(tile, player);
			if(success)
				action.doPlace(tile, player);
			return success;
		};
	}
	
	@FunctionalInterface
	interface PlacementAction {
		void doPlace(ServerTile tile, ServerPlayer player);
		
		static PlacementAction tile(@NotNull TileTypeEnum tileType) {
			return (tile, player) -> tile.addTile(ServerTileType.get(tileType));
		}
		
		static PlacementAction entity(@NotNull FetchFunction<ServerEntity> entityTemplate) {
			return (tile, player) -> {
				ServerLevel level = tile.getLevel();
				ServerEntity e = entityTemplate.get();
				e.moveTo(tile);
				level.addEntity(e);
			};
		}
	}
	
	@FunctionalInterface
	interface PlacementCheck {
		
		boolean canPlace(ServerTile tile, ServerPlayer player);
		
		PlacementCheck ALWAYS = (tile, player) -> true;
		PlacementCheck NEVER = (tile, player) -> false;
		
		static PlacementCheck onTile(TileTypeEnum... canPlaceOn) {
			return (tile, player) -> {
				boolean canPlace = false;
				TileTypeEnum tileType = tile.getType().getTypeEnum();
				for (TileTypeEnum underType: canPlaceOn) {
					if(underType == tileType) {
						canPlace = true;
						break;
					}
				}
				return canPlace;
			};
		}
		
		PlacementCheck GROUND = (tile, player) -> TypeGroup.GROUND.contains(tile.getType().getTypeEnum());
		
		static PlacementCheck groundExcluding(TileTypeEnum exclude) {
			return (tile, player) -> tile.getType().getTypeEnum() != exclude && GROUND.canPlace(tile, player);
		}
	}
}
