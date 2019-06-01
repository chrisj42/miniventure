package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.EnumItemType.EnumItem;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.tile.TileTypeEnum.TypeGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileItem extends ServerItem {
	
	public enum TileItemType {
		; // here is where I can add extra TileItems that may safely conflict with the "official" TileType to TileItem mappings.
		// ...except currently it actually won't work because the load function doesn't check this.
		
		final TileItem item;
		
		TileItemType(TileItem item) {
			this.item = item;
			EnumItem template = new EnumItem(EnumItemType.Tile, this);
			item.saveData = template.save();
		}
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
	
	
	
	@NotNull private final TileTypeEnum result;
	@NotNull private final PlacementCheck placementCheck;
	
	private String[] saveData;
	
	public TileItem(@NotNull TileTypeEnum result, boolean useIconAtlas, @NotNull PlacementCheck placementCheck) {
		this(MyUtils.toTitleCase(result.name()).replaceAll("_", " "), result, useIconAtlas, placementCheck);
	}
	public TileItem(String name, @NotNull TileTypeEnum result, boolean useIconAtlas, @NotNull PlacementCheck placementCheck) {
		this(name,
			useIconAtlas ? GameCore.icons.get("items/tile/"+name.toLowerCase()) : 
				GameCore.descaledTileAtlas.getRegion(result.name().toLowerCase()+"/c00"),
			result, placementCheck);
	}
	public TileItem(String name, @NotNull TextureHolder texture, @NotNull TileTypeEnum result, @NotNull PlacementCheck placementCheck) {
		super(ItemType.Tile, name, texture);
		this.result = result;
		this.placementCheck = placementCheck;
		saveData = new String[] {getType().name(), result.name()};
	}
	
	@Override
	public Result interact(WorldObject obj, Player player) {
		if(obj instanceof ServerTile) {
			ServerTile tile = (ServerTile) obj;
			if(placementCheck.canPlace(tile, player) && tile.addTile(ServerTileType.get(result)))
				return Result.USED;
		}
		
		return obj.interactWith(player, this);
	}
	
	@NotNull TileTypeEnum getResult() { return result; }
	
	@Override
	public String[] save() {
		return saveData;
	}
}
