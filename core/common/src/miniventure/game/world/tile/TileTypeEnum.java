package miniventure.game.world.tile;

import java.awt.Color;

import miniventure.game.util.MyUtils;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.worldgen.level.ProtoTile;
import miniventure.game.world.worldgen.level.processing.TileProcessor;

import org.jetbrains.annotations.NotNull;

import static miniventure.game.world.tile.TileLayer.DecLayer;
import static miniventure.game.world.tile.TileLayer.GroundLayer;
import static miniventure.game.world.tile.TileLayer.ObjectLayer;

public enum TileTypeEnum implements TileProcessor {
	
	HOLE(GroundLayer, true),
	DIRT(GroundLayer, true, new Color(200, 100, 0)),
	SAND(GroundLayer, true, Color.YELLOW),
	GRASS(GroundLayer, true, Color.GREEN, P.UNDER.as(DIRT)),
	STONE_PATH(DecLayer, true),
	SNOW(DecLayer, true, Color.WHITE),
	SMALL_STONE(DecLayer, true, Color.LIGHT_GRAY),
	STONE_FLOOR(GroundLayer, true),
	WATER(GroundLayer, true, Color.BLUE.darker(), P.SPEED.as(0.6f)),
	DOCK(DecLayer, true),
	COAL_ORE(ObjectLayer, false),
	IRON_ORE(ObjectLayer, false),
	TUNGSTEN_ORE(ObjectLayer, false),
	RUBY_ORE(ObjectLayer, false),
	STONE(ObjectLayer, false, Color.GRAY),
	WOOD_WALL(ObjectLayer, false),
	STONE_WALL(ObjectLayer, false),
	OPEN_DOOR(ObjectLayer, true),
	CLOSED_DOOR(ObjectLayer, false),
	TORCH(ObjectLayer, true),
	CACTUS(ObjectLayer, false, Color.GREEN.darker().darker()),
	CARTOON_TREE(ObjectLayer, false, Color.GREEN.darker().darker()),
	DARK_TREE(ObjectLayer, false, Color.GREEN.darker().darker()),
	PINE_TREE(ObjectLayer, false, Color.GREEN.darker().darker()),
	POOF_TREE(ObjectLayer, false, Color.GREEN.darker().darker()),
	AIR(ObjectLayer, true);
	
	private interface P {
		Param<Float> SPEED = new Param<>(1f);
		// Param<Color> COLOR = new Param<>(Color.BLACK);
		Param<TileTypeEnum> UNDER = new Param<>(null);
	}
	
	public final TileLayer layer;
	public final boolean walkable;
	public final float speedRatio;
	public final Color color;
	private final TileTypeEnum underType;
	
	TileTypeEnum(TileLayer layer, boolean walkable, Value<?>... params) {
		this(layer, walkable, Color.BLACK, params);
	}
	TileTypeEnum(TileLayer layer, boolean walkable, Color color, Value<?>... params) {
		this.layer = layer;
		this.walkable = walkable;
		ParamMap map = new ParamMap(params);
		this.speedRatio = map.get(P.SPEED);
		this.color = color;
		this.underType = map.get(P.UNDER);
		MyUtils.debug("Initialized TileType "+this);
	}
	
	private static final TileTypeEnum[] values = TileTypeEnum.values();
	public static TileTypeEnum value(int ord) { return values[ord]; }
	
	public TileType getTypeInstance(@NotNull WorldManager world) {
		return world.getTileType(this);
	}
	
	public TileTypeEnum getUnderType() {
		return underType == null ? layer == GroundLayer ? HOLE : null : underType;
	}
	
	@Override
	// adds this tile type to the tile stack.
	public void processTile(ProtoTile tile) {
		tile.addLayer(this);
	}
	
	/*public enum TypeGroup {
		GROUND(DIRT, GRASS, SAND, STONE_PATH, STONE_FLOOR, SNOW, DOCK);
		
		private final EnumSet<TileTypeEnum> types;
		
		TypeGroup(TileTypeEnum... types) {
			this.types = EnumSet.copyOf(Arrays.asList(types));
		}
		
		public boolean contains(TileTypeEnum type) {
			return types.contains(type);
		}
	}*/
	
	/*
		- tile types fit into layers
			- ground
			- decoration
			- object
	 */
}
