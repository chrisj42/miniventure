package miniventure.game.world.tile;

import miniventure.game.world.WorldManager;

import org.jetbrains.annotations.NotNull;

public enum TileTypeEnum {
	
	HOLE(true),
	DIRT(true),
	SAND(true),
	GRASS(true),
	STONE_PATH(true),
	SNOW(true),
	FLINT(true),
	WATER(true, 0.6f),
	COAL_ORE(false),
	IRON_ORE(false),
	TUNGSTEN_ORE(false),
	RUBY_ORE(false),
	STONE(false),
	STONE_FLOOR(true),
	WOOD_WALL(false),
	STONE_WALL(false),
	OPEN_DOOR(true),
	CLOSED_DOOR(false),
	TORCH(true),
	CACTUS(false),
	CARTOON_TREE(false),
	DARK_TREE(false),
	PINE_TREE(false),
	POOF_TREE(false);
	
	public final boolean walkable;
	public final float speedRatio;
	
	TileTypeEnum(boolean walkable) { this(walkable, 1); }
	TileTypeEnum(boolean walkable, float speedRatio) {
		this.walkable = walkable;
		this.speedRatio = speedRatio;
	}
	
	private static final TileTypeEnum[] values = TileTypeEnum.values();
	public static TileTypeEnum value(int ord) { return values[ord]; }
	
	public TileType getTypeInstance(@NotNull WorldManager world) {
		return world.getTileType(this);
	}
}
