package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.item.PlacementAttempt.*;
import static miniventure.game.item.PlacementAttempt.PlacementAction.tile;

public enum PlaceableItemType {
	
	Dirt(tile(TileTypeEnum.DIRT), PlacementCheck.onTile(TileTypeEnum.HOLE)),
	
	Sand(tile(TileTypeEnum.SAND), PlacementCheck.onTile(TileTypeEnum.DIRT)),
	
	Snow(tile(TileTypeEnum.SNOW), PlacementCheck.groundExcluding(TileTypeEnum.SNOW)),
	
	Torch(tile(TileTypeEnum.TORCH), PlacementCheck.GROUND,
		GameCore.tileAtlas.getRegion("torch/c00")
	);
	
	// here is where I can add extra TileItems that may safely conflict with the "official" TileType to TileItem mappings.
	// ...except currently it actually won't work because the load function doesn't check this.
	
	@NotNull private final PlacementAttempt placementAttempt;
	@Nullable private final TextureHolder customTexture;
	
	PlaceableItemType(@NotNull PlacementAction placementAction, @NotNull PlacementCheck placementCheck) {
		this(placementAction, placementCheck, null);
	}
	PlaceableItemType(@NotNull PlacementAction placementAction, @NotNull PlacementCheck placementCheck, @Nullable TextureHolder customTexture) {
		this.placementAttempt = getAttempt(placementCheck, placementAction);
		this.customTexture = customTexture;
	}
	
	@NotNull
	public ServerItem get() {
		return customTexture == null ? new PlaceableItem() : new PlaceableItem(customTexture);
	}
	
	public class PlaceableItem extends EnumItem {
		
		private PlaceableItem() {
			super(ItemType.Placeable, PlaceableItemType.this);
		}
		private PlaceableItem(@NotNull TextureHolder customTexture) {
			super(ItemType.Placeable, PlaceableItemType.this, customTexture);
		}
		
		@Override @NotNull
		public CursorHighlight getHighlightMode() {
			return CursorHighlight.TILE_IN_RADIUS;
		}
		
		@Override
		public Result interact(WorldObject obj, ServerPlayer player) {
			if(obj instanceof ServerTile) {
				ServerTile tile = (ServerTile) obj;
				if(placementAttempt.tryPlace(tile, player))
					return Result.USED;
			}
			
			return super.interact(obj, player);
		}
	}
	
}
