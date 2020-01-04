package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.EphemeralItem;
import miniventure.game.item.PlacementAttempt.PlacementAction;
import miniventure.game.item.PlacementAttempt.PlacementCheck;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.item.PlacementAttempt.PlacementAction.tile;

public enum ConstructableObjectType implements PlacementAttempt {
	
	// if I make this class a thing, then it will essentially be an enumeration of the objects that would be given as that fancy active item for clients that shows transparent overlays over a tile
	
	// it really has the same info as placeable items, except the "item" state is very fleeting
		// used to be given to a client and nothing else
	
	Stone_Path(TileTypeEnum.STONE_PATH, PlacementCheck.groundExcluding(TileTypeEnum.STONE_PATH)),
	
	Dock(TileTypeEnum.DOCK, PlacementCheck.groundExcluding(TileTypeEnum.DOCK)),
	
	Stone_Floor(TileTypeEnum.STONE_FLOOR, PlacementCheck.onTile(TileTypeEnum.HOLE)),
	
	Wood_Wall(TileTypeEnum.WOOD_WALL, PlacementCheck.GROUND),
	
	Stone_Wall(TileTypeEnum.STONE_WALL, PlacementCheck.GROUND),
	
	Door(TileTypeEnum.CLOSED_DOOR, PlacementCheck.GROUND);
	
	@NotNull private final PlacementAttempt placementAttempt;
	@NotNull private final ServerItem item;
	
	// for tiles
	ConstructableObjectType(@NotNull TileTypeEnum tileType, @NotNull PlacementCheck placementCheck) {
		this(tileType, true, placementCheck);
	}
	ConstructableObjectType(@NotNull TileTypeEnum tileType, boolean useLargeSprite, @NotNull PlacementCheck placementCheck) {
		this(tile(tileType), placementCheck,
			(useLargeSprite ? GameCore.tileAtlas : GameCore.descaledTileAtlas).getRegion(tileType.name().toLowerCase()+"/c00")
		);
	}
	
	// for entities
	
	
	// general
	ConstructableObjectType(@NotNull PlacementAction placementAction, @NotNull PlacementCheck placementCheck) {
		this(placementAction, placementCheck, null);
	}
	ConstructableObjectType(@NotNull PlacementAction placementAction, @NotNull PlacementCheck placementCheck, @Nullable TextureHolder customTexture) {
		this.placementAttempt = PlacementAttempt.getAttempt(placementCheck, placementAction);
		this.item = customTexture == null ? new BlueprintItem() : new BlueprintItem(customTexture);
	}
	
	@Override
	public boolean tryPlace(ServerTile tile, ServerPlayer player) {
		return placementAttempt.tryPlace(tile, player);
	}
	
	// should only be fetched to pass along a basic reference to the client about this ConstructableObject, in the form of an item since that's what recipes want.
	@NotNull
	ServerItem getItem() { return item; }
	
	public class BlueprintItem extends EphemeralItem {
		
		// note that is is not actually what is set as the client's active item; this class is solely for internal use by BlueprintRecipes passed to the client.
		
		private BlueprintItem() { super(name()); }
		private BlueprintItem(@NotNull TextureHolder texture) {
			super(name(), texture);
		}
		
		@NotNull @Override
		public Player.CursorHighlight getHighlightMode() {
			return CursorHighlight.TILE_IN_RADIUS;
		}
	}
	
}