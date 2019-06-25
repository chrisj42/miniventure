package miniventure.game.item;

import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.tile.TileTypeEnum.TypeGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TilePlacement {
	
	@NotNull
	private final TileTypeEnum result;
	@NotNull
	private final PlacementCheck placementCheck;
	
	TilePlacement(@NotNull TileTypeEnum result, @NotNull PlacementCheck placementCheck) {
		this.result = result;
		this.placementCheck = placementCheck;
	}
	
	// @NotNull
	// TileTypeEnum getResult() { return result; }
	
	boolean tryPlace(ServerTile tile, Player player) {
		if(placementCheck.canPlace(tile, player)) {
			tile.addTile(ServerTileType.get(result));
			return true;
		}
		return false;
	}
	
	
	
	@FunctionalInterface
	public interface PlacementCheck {
		
		boolean canPlace(ServerTile tile, Player player);
		
		PlacementCheck NEVER = (tile, player) -> false;
		
		static PlacementCheck on(@Nullable TileTypeEnum... canPlaceOn) {
			return (tile, player) -> {
				boolean canPlace = canPlaceOn == null;
				if(!canPlace) {
					TileTypeEnum tileType = tile.getType().getTypeEnum();
					for (TileTypeEnum underType: canPlaceOn) {
						if(underType == tileType) {
							canPlace = true;
							break;
						}
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
