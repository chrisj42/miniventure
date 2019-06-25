package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.TilePlacement.PlacementCheck;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public enum TileBlueprintType {
	
	Stone_Path(TileTypeEnum.STONE_PATH, PlacementCheck.groundExcluding(TileTypeEnum.STONE_PATH)),
	
	Dock(TileTypeEnum.DOCK, PlacementCheck.groundExcluding(TileTypeEnum.DOCK)),
	
	Stone_Floor(TileTypeEnum.STONE_FLOOR, PlacementCheck.on(TileTypeEnum.HOLE)),
	
	Wood_Wall(TileTypeEnum.WOOD_WALL, PlacementCheck.GROUND),
	
	Stone_Wall(TileTypeEnum.STONE_WALL, PlacementCheck.GROUND),
	
	Door(TileTypeEnum.CLOSED_DOOR, PlacementCheck.GROUND);
	
	@NotNull
	private final TilePlacement tilePlacement;
	@NotNull
	private final TextureHolder texture;
	
	TileBlueprintType(@NotNull TileTypeEnum result, @NotNull PlacementCheck placementCheck) {
		this(result, placementCheck, true);
	}
	TileBlueprintType(@NotNull TileTypeEnum result, @NotNull PlacementCheck placementCheck, boolean useDescaledAtlas) {
		this.tilePlacement = new TilePlacement(result, placementCheck);
		texture = (useDescaledAtlas ? GameCore.descaledTileAtlas : GameCore.tileAtlas)
			.getRegion(result.name().toLowerCase()+"/c00");
	}
	
}
