package miniventure.game.world.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;

import miniventure.game.world.management.WorldManager;
import miniventure.game.world.worldgen.island.ProtoTile;
import miniventure.game.world.worldgen.island.TileProcessor;

import org.jetbrains.annotations.NotNull;

public enum TileTypeEnum implements TileProcessor {
	
	HOLE(true),
	DIRT(true, new Color(200, 100, 0)),
	SAND(true, Color.YELLOW),
	GRASS(true, Color.GREEN),
	STONE_PATH(true),
	SNOW(true, Color.WHITE),
	FLINT(true),
	WATER(true, 0.6f, Color.BLUE.darker()),
	DOCK(true),
	COAL_ORE(false),
	IRON_ORE(false),
	TUNGSTEN_ORE(false),
	RUBY_ORE(false),
	STONE(false, Color.GRAY),
	STONE_FLOOR(true),
	WOOD_WALL(false),
	STONE_WALL(false),
	OPEN_DOOR(true),
	CLOSED_DOOR(false),
	TORCH(true),
	CACTUS(false, Color.GREEN.darker().darker()),
	CARTOON_TREE(false, Color.GREEN.darker().darker()),
	DARK_TREE(false, Color.GREEN.darker().darker()),
	PINE_TREE(false, Color.GREEN.darker().darker()),
	POOF_TREE(false, Color.GREEN.darker().darker()),
	AIR(true);
	
	public final boolean walkable;
	public final float speedRatio;
	public final Color color;
	
	TileTypeEnum(boolean walkable) { this(walkable, 1); }
	TileTypeEnum(boolean walkable, Color color) { this(walkable, 1, color); }
	TileTypeEnum(boolean walkable, float speedRatio) { this(walkable, speedRatio, Color.BLACK); }
	TileTypeEnum(boolean walkable, float speedRatio, Color color) {
		this.walkable = walkable;
		this.speedRatio = speedRatio;
		this.color = color;
	}
	
	private static final TileTypeEnum[] values = TileTypeEnum.values();
	public static TileTypeEnum value(int ord) { return values[ord]; }
	
	public TileType getTypeInstance(@NotNull WorldManager world) {
		return world.getTileType(this);
	}
	
	@Override
	// adds this tile type to the tile stack.
	public void processTile(ProtoTile tile) {
		tile.addLayer(this);
	}
	
	public enum TypeGroup {
		GROUND(DIRT, GRASS, SAND, STONE_PATH, STONE_FLOOR, SNOW, DOCK);
		
		private final EnumSet<TileTypeEnum> types;
		
		TypeGroup(TileTypeEnum... types) {
			this.types = EnumSet.copyOf(Arrays.asList(types));
		}
		
		public boolean contains(TileTypeEnum type) {
			return types.contains(type);
		}
	}
}
