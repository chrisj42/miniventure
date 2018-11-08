package miniventure.game.world.tile;

import miniventure.game.world.WorldManager;

import org.jetbrains.annotations.NotNull;

public enum TileTypeEnum {
	
	HOLE, DIRT, SAND, GRASS, STONE_PATH, SNOW,
	FLINT, WATER, COAL, IRON, TUNGSTEN, RUBY,
	STONE, STONE_FLOOR, WOOD_WALL, STONE_WALL,
	DOOR_OPEN, DOOR_CLOSED, TORCH, CACTUS,
	CARTOON_TREE, DARK_TREE, PINE_TREE, POOF_TREE;
	
	private static final TileTypeEnum[] values = TileTypeEnum.values();
	public static TileTypeEnum value(int ord) { return values[ord]; }
	
	public TileType getTypeInstance(@NotNull WorldManager world) {
		return world.getTileType(this);
	}
}
