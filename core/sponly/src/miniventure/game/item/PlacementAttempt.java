package miniventure.game.item;

import miniventure.game.util.function.FetchFunction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.Level;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileType.TypeGroup;

import org.jetbrains.annotations.NotNull;

public interface PlacementAttempt {
	
	boolean tryPlace(Tile tile, Player player);
	
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
		void doPlace(Tile tile, Player player);
		
		static PlacementAction tile(@NotNull TileType tileType) {
			return (tile, player) -> tile.addTile(tileType);
		}
		
		static PlacementAction entity(@NotNull FetchFunction<Entity> entityTemplate) {
			return (tile, player) -> {
				Level level = tile.getLevel();
				Entity e = entityTemplate.get();
				e.moveTo(tile);
				level.addEntity(e);
			};
		}
	}
	
	@FunctionalInterface
	interface PlacementCheck {
		
		boolean canPlace(Tile tile, Player player);
		
		PlacementCheck ALWAYS = (tile, player) -> true;
		PlacementCheck NEVER = (tile, player) -> false;
		
		static PlacementCheck onTile(TileType... canPlaceOn) {
			return (tile, player) -> {
				boolean canPlace = false;
				TileType tileType = tile.getType();
				for (TileType underType: canPlaceOn) {
					if(underType == tileType) {
						canPlace = true;
						break;
					}
				}
				return canPlace;
			};
		}
		
		PlacementCheck GROUND = (tile, player) -> TypeGroup.GROUND.contains(tile.getType());
		
		static PlacementCheck groundExcluding(TileType exclude) {
			return (tile, player) -> tile.getType() != exclude && GROUND.canPlace(tile, player);
		}
	}
}
