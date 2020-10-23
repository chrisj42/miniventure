package miniventure.game.world.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;

import miniventure.game.util.MyUtils;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.Point;
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
	SMALL_STONE(true, Color.LIGHT_GRAY),
	FLOWER(true, Color.WHITE),
	STICK(true),
	WATER(true, 0.6f, Color.BLUE.darker()),
	REEDS(true, new Color(184, 101, 14)),
	DOCK(true, P.SIZE.as(Point.TWO)),
	COAL_ORE(false),
	IRON_ORE(false),
	TUNGSTEN_ORE(false),
	RUBY_ORE(false),
	STONE(false, Color.GRAY),
	STONE_FLOOR(true),
	WOOD_WALL(false),
	STONE_WALL(false),
	OPEN_DOOR(true, P.SIZE.as(Point.TWO)),
	CLOSED_DOOR(false, P.SIZE.as(Point.TWO)),
	TORCH(true),
	CACTUS(false, Color.GREEN.darker().darker()),
	CARTOON_TREE(false, 1, Color.GREEN.darker().darker(), Point.TWO),
	DARK_TREE(false, 1, Color.GREEN.darker().darker(), Point.TWO),
	PINE_TREE(false, 1, Color.GREEN.darker().darker(), Point.TWO),
	POOF_TREE(false, 1, Color.GREEN.darker().darker(), Point.TWO),
	AIR(true);
	
	private interface P {
		Param<Float> SPEED = new Param<>(1f);
		Param<Color> COLOR = new Param<>(Color.BLACK);
		Param<Point> SIZE = new Param<>(new Point(0, 0));
	}
	
	public final boolean walkable;
	public final float speedRatio;
	public final Color color;
	public final Point size;
	// public final boolean multi;
	
	TileTypeEnum(boolean walkable, Value<?>... params) {
		this.walkable = walkable;
		ParamMap map = new ParamMap(params);
		speedRatio = map.get(P.SPEED);
		color = map.get(P.COLOR);
		size = map.get(P.SIZE);
		// multi = size.x > 1 || size.y > 1;
		MyUtils.debug("Initialized TileType "+this);
	}
	TileTypeEnum(boolean walkable, Color color) { this(walkable, P.COLOR.as(color)); }
	TileTypeEnum(boolean walkable, float speedRatio) { this(walkable, P.SPEED.as(speedRatio)); }
	TileTypeEnum(boolean walkable, float speedRatio, Color color) {
		this(walkable, P.COLOR.as(color), P.SPEED.as(speedRatio));
	}
	TileTypeEnum(boolean walkable, float speedRatio, Color color, Point size) {
		this(walkable, P.COLOR.as(color), P.SPEED.as(speedRatio), P.SIZE.as(size));
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
