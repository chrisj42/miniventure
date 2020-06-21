package miniventure.game.item;

import miniventure.game.core.GdxCore;
import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.texture.ItemTextureSource;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.CursorHighlight;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.item.PlacementAttempt.PlacementAction.tile;
import static miniventure.game.item.PlacementAttempt.PlacementCheck;
import static miniventure.game.item.PlacementAttempt.getAttempt;

public enum PlaceableItemType implements ItemEnum {
	
	Dirt(TileType.DIRT, PlacementCheck.onTile(TileType.HOLE)),
	
	Sand(TileType.SAND, PlacementCheck.onTile(TileType.DIRT)),
	
	Snow(TileType.SNOW, PlacementCheck.groundExcluding(TileType.SNOW)),
	
	Torch(TileType.TORCH, PlacementCheck.GROUND,
		ItemTextureSource.Tile_Atlas.get("torch/main")
	);
	
	// here is where I can add extra TileItems that may safely conflict with the "official" TileType to TileItem mappings.
	// ...except currently it actually won't work because the load function doesn't check this.
	
	@NotNull private final PlacementAttempt placementAttempt;
	// @Nullable private final TextureHolder customTexture;
	private final PlaceableItem item;
	private final TextureHolder resultTexture;
	
	// I'll turn the tile types back into placement actions later.
	PlaceableItemType(@NotNull TileType tileType, @NotNull PlacementCheck placementCheck) {
		this(tileType, placementCheck, null);
	}
	PlaceableItemType(@NotNull TileType tileType, @NotNull PlacementCheck placementCheck, @Nullable TextureHolder customTexture) {
		this.placementAttempt = getAttempt(placementCheck, tile(tileType));
		String spritePrefix = tileType.name().toLowerCase()+'/';
		String spriteName = spritePrefix + "main";
		if(GdxCore.tileAtlas.getRegion(spriteName) == null)
			spriteName = spritePrefix + "c00";
		resultTexture = ItemTextureSource.Tile_Atlas.get(spriteName);
		this.item = customTexture == null ? new PlaceableItem() : new PlaceableItem(customTexture);
	}
	
	@Override @NotNull
	public Item get() {
		return item;
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
		
		/*@Override
		protected void addSerialData(ItemDataMap map) {
			super.addSerialData(map);
			map.add(ItemDataTag.CursorSprite, resultTexture);
		}*/
		
		@Override @Nullable
		public TextureHolder getCursorTexture() {
			return resultTexture;
		}
		
		@Override
		public Result interact(WorldObject obj, Player player) {
			if(obj instanceof Tile) {
				Tile tile =  (Tile) obj;
				if(placementAttempt.tryPlace(tile, player))
					return Result.USED;
			}
			
			return super.interact(obj, player);
		}
	}
	
}
