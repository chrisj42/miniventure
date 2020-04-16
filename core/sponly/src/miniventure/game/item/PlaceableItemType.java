package miniventure.game.item;

import miniventure.game.core.GameCore;
import miniventure.game.item.ItemDataTag.ItemDataMap;
import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.texture.FetchableTextureHolder;
import miniventure.game.texture.ItemTextureSource;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.item.PlacementAttempt.PlacementAction.tile;
import static miniventure.game.item.PlacementAttempt.PlacementCheck;
import static miniventure.game.item.PlacementAttempt.getAttempt;

public enum PlaceableItemType {
	
	Dirt(TileTypeEnum.DIRT, PlacementCheck.onTile(TileTypeEnum.HOLE)),
	
	Sand(TileTypeEnum.SAND, PlacementCheck.onTile(TileTypeEnum.DIRT)),
	
	Snow(TileTypeEnum.SNOW, PlacementCheck.groundExcluding(TileTypeEnum.SNOW)),
	
	Torch(TileTypeEnum.TORCH, PlacementCheck.GROUND,
		ItemTextureSource.Tile_Atlas.get("torch/main")
	);
	
	// here is where I can add extra TileItems that may safely conflict with the "official" TileType to TileItem mappings.
	// ...except currently it actually won't work because the load function doesn't check this.
	
	@NotNull private final PlacementAttempt placementAttempt;
	// @Nullable private final TextureHolder customTexture;
	private final PlaceableItem item;
	private final FetchableTextureHolder resultTexture;
	
	// I'll turn the tile types back into placement actions later.
	PlaceableItemType(@NotNull TileTypeEnum tileType, @NotNull PlacementCheck placementCheck) {
		this(tileType, placementCheck, null);
	}
	PlaceableItemType(@NotNull TileTypeEnum tileType, @NotNull PlacementCheck placementCheck, @Nullable FetchableTextureHolder customTexture) {
		this.placementAttempt = getAttempt(placementCheck, tile(tileType));
		String spritePrefix = tileType.name().toLowerCase()+'/';
		String spriteName = spritePrefix + "main";
		if(GameCore.tileAtlas.getRegion(spriteName) == null)
			spriteName = spritePrefix + "c00";
		resultTexture = ItemTextureSource.Tile_Atlas.get(spriteName);
		this.item = customTexture == null ? new PlaceableItem() : new PlaceableItem(customTexture);
	}
	
	@NotNull
	public ServerItem get() {
		return item;
	}
	
	public class PlaceableItem extends EnumItem {
		
		private PlaceableItem() {
			super(ItemType.Placeable, PlaceableItemType.this);
		}
		private PlaceableItem(@NotNull FetchableTextureHolder customTexture) {
			super(ItemType.Placeable, PlaceableItemType.this, customTexture);
		}
		
		@Override @NotNull
		public CursorHighlight getHighlightMode() {
			return CursorHighlight.TILE_IN_RADIUS;
		}
		
		@Override
		protected void addSerialData(ItemDataMap map) {
			super.addSerialData(map);
			map.add(ItemDataTag.CursorSprite, resultTexture);
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
