package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.item.TilePlacement.PlacementCheck;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TileItemType {
	
	Dirt(TileTypeEnum.DIRT, PlacementCheck.on(TileTypeEnum.HOLE)),
	
	Sand(TileTypeEnum.SAND, PlacementCheck.on(TileTypeEnum.DIRT)),
	
	Snow(TileTypeEnum.SNOW, PlacementCheck.groundExcluding(TileTypeEnum.SNOW)),
	
	Torch(new TilePlacement(TileTypeEnum.TORCH, PlacementCheck.GROUND),
		GameCore.tileAtlas.getRegion("torch/c00")
	);
	
	// here is where I can add extra TileItems that may safely conflict with the "official" TileType to TileItem mappings.
	// ...except currently it actually won't work because the load function doesn't check this.
	
	@NotNull private final TilePlacement tilePlacement;
	@Nullable private final TextureHolder customTexture;
	
	TileItemType(@NotNull TileTypeEnum result, @NotNull PlacementCheck placementCheck) {
		this(new TilePlacement(result, placementCheck), null);
	}
	TileItemType(@NotNull TilePlacement tilePlacement, @Nullable TextureHolder customTexture) {
		this.tilePlacement = tilePlacement;
		this.customTexture = customTexture;
	}
	
	@NotNull
	public ServerItem get() {
		return customTexture == null ? new TileItem() : new TileItem(customTexture);
	}
	
	public class TileItem extends EnumItem {
		
		private TileItem() {
			super(ItemType.Tile, TileItemType.this);
		}
		private TileItem(@NotNull TextureHolder customTexture) {
			super(ItemType.Tile, TileItemType.this, customTexture);
		}
		
		@Override @NotNull
		public CursorHighlight getHighlightMode() {
			return CursorHighlight.TILE_IN_RADIUS;
		}
		
		@Override
		public Result interact(WorldObject obj, Player player) {
			if(obj instanceof ServerTile) {
				ServerTile tile = (ServerTile) obj;
				if(tilePlacement.tryPlace(tile, player))
					return Result.USED;
			}
			
			return obj.interactWith(player, this);
		}
	}
	
}
