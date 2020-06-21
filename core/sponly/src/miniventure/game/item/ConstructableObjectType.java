package miniventure.game.item;

import miniventure.game.texture.ItemTextureSource;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.item.PlacementAttempt.PlacementAction.tile;

public enum ConstructableObjectType implements PlacementAttempt {
	
	// if I make this class a thing, then it will essentially be an enumeration of the objects that would be given as that fancy active item for clients that shows transparent overlays over a tile
	
	// it really has the same info as placeable items, except the "item" state is very fleeting
		// used to be given to a client and nothing else
	
	Stone_Path(TileType.STONE_PATH, PlacementCheck.groundExcluding(TileType.STONE_PATH)),
	
	Dock(TileType.DOCK, PlacementCheck.groundExcluding(TileType.DOCK)),
	
	Stone_Floor(TileType.STONE_FLOOR, "c00", PlacementCheck.onTile(TileType.HOLE)),
	
	Wood_Wall(TileType.WOOD_WALL, PlacementCheck.GROUND),
	
	Stone_Wall(TileType.STONE_WALL, PlacementCheck.GROUND),
	
	Door(TileType.CLOSED_DOOR, PlacementCheck.GROUND);
	
	@NotNull private final PlacementAttempt placementAttempt;
	@NotNull private final TextureHolder texture;
	
	// for tiles
	ConstructableObjectType(@NotNull TileType tileType, @NotNull PlacementCheck placementCheck) {
		this(tileType, "main", placementCheck);
	}
	ConstructableObjectType(@NotNull TileType tileType, String tileSpriteName, @NotNull PlacementCheck placementCheck) {
		this(tile(tileType), placementCheck,
			ItemTextureSource.Tile_Atlas.get(tileType.name().toLowerCase()+'/'+tileSpriteName)
		);
	}
	
	ConstructableObjectType(@NotNull PlacementAction placementAction, @NotNull PlacementCheck placementCheck, @NotNull TextureHolder customTexture) {
		this.placementAttempt = PlacementAttempt.getAttempt(placementCheck, placementAction);
		this.texture = customTexture;
	}
	
	@Override
	public boolean tryPlace(Tile tile, Player player) {
		return placementAttempt.tryPlace(tile, player);
	}
	
	// fetched to create a special private kind of recipe
	@NotNull
	public TextureHolder getTexture() { return texture; }
	
}
