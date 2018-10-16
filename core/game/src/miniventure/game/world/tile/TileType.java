package miniventure.game.world.tile;

import java.awt.Color;

import miniventure.game.item.Item;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MonoValueFunction;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.SpreadUpdateAction.FloatFetcher;
import miniventure.game.world.tile.data.CacheTag;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.PropertyTag;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileType {
	
	private static final Color BROWN = new Color(138, 66, 16);
	
	public enum TileTypeEnum {
		
		HOLE(BROWN.darker(), type -> new TileType(type, true,
			new DataMap(PropertyTag.Swim.as(new SwimAnimation(type, 0.75f))),
			
			DestructionManager.INDESTRUCTIBLE(type),
			
			new TileTypeRenderer(type, true,
				new ConnectionManager(type, RenderStyle.SINGLE_FRAME, type, TileTypeEnum.valueOf("WATER"))
			)
		)),
		
		DIRT(BROWN, type -> new TileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel)),
			
			new TileTypeRenderer(type, true)
		)),
		
		SAND(Color.YELLOW, type -> new TileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel)),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			))
		),
		
		GRASS(Color.GREEN, type -> new TileType(type, true, new DataMap(),
			new DestructionManager.DestructibleBuilder(type, 1, false)
				.require(new RequiredTool(ToolType.Shovel))
				.make(),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			),
			
			new UpdateManager(type,
				new SpreadUpdateAction(type, FloatFetcher.random(5, 30, 60), .95f,
					(newType, tile) -> tile.addTile(newType),
					DIRT
				)
			)
		)),
		
		PATH_STONE(Color.GRAY, type -> new TileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Pickaxe)),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		)),
		
		SNOW(Color.WHITE, type -> new TileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel)),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		)),
		
		WATER(Color.BLUE, type -> new TileType(type, true,
			new DataMap(PropertyTag.SpeedRatio.as(0.6f))
			.add(PropertyTag.Swim.as(new SwimAnimation(type))),
			
			DestructionManager.INDESTRUCTIBLE(type),
			
			new TileTypeRenderer(type, true,
				new ConnectionManager(type, new RenderStyle(PlayMode.LOOP_RANDOM, 0.2f)),
				new OverlapManager(type, new RenderStyle(true, 1/24f))
			),
			new UpdateManager(type,
				new SpreadUpdateAction(type, 0.33f, (newType, tile) -> tile.addTile(newType), HOLE)
			)
		)),
		
		STONE(Color.GRAY, type -> new TileType(type, false,
			new DestructionManager(type, 40,
				new PreferredTool(ToolType.Pickaxe, 5)
			),
			
			new TileTypeRenderer(type, true, 
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		)),
		
		STONE_FLOOR(Color.LIGHT_GRAY, type -> new FloorTile(type, ToolType.Pickaxe)),
		
		WOOD_WALL(BROWN.brighter(), type -> new WallTile(type, 20, new PreferredTool(ToolType.Axe, 3))),
		
		STONE_WALL(Color.LIGHT_GRAY, type -> new WallTile(type, 40, new PreferredTool(ToolType.Pickaxe, 5))),
		
		DOOR_OPEN(BROWN.brighter(), DoorTile::getOpenDoor),
		
		DOOR_CLOSED(BROWN.brighter(), DoorTile::getClosedDoor),
		
		TORCH(Color.ORANGE, type -> new TileType(type, true,
			new DataMap(PropertyTag.LightRadius.as(2f)),
			
			new DestructionManager(type),
			
			new TileTypeRenderer(type, false,
				new ConnectionManager(type, new RenderStyle(1/12f)),
				OverlapManager.NONE(type),
				new TransitionManager(type)
					.addEntranceAnimations(new TransitionAnimation("enter", 3/12f))
			)
		)),
		
		CACTUS(Color.GREEN.darker().darker(), type -> new TileType(type, false,
			new DestructionManager(type, 12, null),
			
			new TileTypeRenderer(type, false)
			
		) {
			@Override
			public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) {
				return entity.attackedBy(tile, null, 1);
			}
		}),
		
		TREE_CARTOON(Color.GREEN.darker(), TreeTile::new),
		TREE_DARK(Color.GREEN.darker(), TreeTile::new),
		TREE_PINE(Color.GREEN.darker(), TreeTile::new),
		TREE_POOF(Color.GREEN.darker(), TreeTile::new);
		
		
		@NotNull public final Color color;
		@NotNull private final MonoValueFunction<TileTypeEnum, TileType> tileTypeFetcher;
		private TileType tileType;
		
		TileTypeEnum(@NotNull Color color, @NotNull MonoValueFunction<TileTypeEnum, TileType> typeFetcher) {
			this.color = color;
			tileTypeFetcher = typeFetcher;
		}
		
		private static final TileTypeEnum[] values = values();
		
		public static TileTypeEnum values(int ordinal) { return values[ordinal]; }
		
		/** @noinspection StaticMethodReferencedViaSubclass*/
		public static void init() {
			CacheTag.init();
			PropertyTag.init();
		}
		
		public TileType getTileType(@NotNull WorldManager world) {
			if(tileType == null)
				tileType = tileTypeFetcher.get(this);
			return world.getTileTypeFetcher().mapValue(this, tileType);
		}
	}
	
	private final TileTypeEnum enumType;
	final DataMap propertyMap;
	final boolean walkable;
	final DestructionManager destructionManager;
	final TileTypeRenderer renderer;
	final UpdateManager updateManager;
	
	/*
		Renderer
			- opaque?
			- connection behavior - 
			- overlap behavior
			- transitions - entrance/exit; complete with triggers.
			
	 */
	
	/*TileType(@NotNull TileTypeEnum enumType, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this(enumType, false, destructionManager, renderer);
	}*/
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this(enumType, walkable, new DataMap(), destructionManager, renderer);
	}
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, DataMap propertyMap, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this(enumType, walkable, propertyMap, destructionManager, renderer, new UpdateManager(enumType));
	}
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, DataMap propertyMap, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		this.enumType = enumType;
		this.walkable = walkable;
		this.propertyMap = propertyMap;
		this.destructionManager = destructionManager;
		this.renderer = renderer;
		this.updateManager = updateManager;
	}
	
	public DataMap getInitialData() { return new DataMap(); }
	
	public boolean hasProperty(PropertyTag<?> tag) { return propertyMap.contains(tag); }
	
	public <T> T getPropertyOrDefault(PropertyTag<T> tag, T defaultValue) {
		return propertyMap.getOrDefault(tag, defaultValue);
	}
	
	public TileTypeEnum getEnumType() { return enumType; }
	public String getName() { return enumType.name(); }
	
	public TileTypeRenderer getRenderer() { return renderer; }
	
	public boolean isPermeableBy(Entity e) { return e instanceof Particle || walkable; }
	
	/*
		Some update methods. All methods have Tile param in addition to whatever listed.
		can update:
		- behavior/state update
		- (LATER) - sprite update (so it doesn't have to be calculated every frame; animations don't count.)
		
		rendering:
		- get sprite (main/connection)
		- get overlap sprite on (param TileLayer index, in given tile's layer stack, b/c it won't always be the top TileLayer we're overlapping)
		
		interaction:
		- attacked (param WorldObject source; param item (may be null))
		- interaction (param player; param item)
		
	 */
	
	
	/**
	 * Called to update the tile's state in some way, whenever an adjacent tile is updated. It is also called once on tile load to get the first value and determine future calls.
	 * If this returns a value greater than 0, then it is called after at least the specified amount of time has passed. Otherwise, it won't be updated until an adjacent tile is updated.
	 * 
	 * @param tile the tile
	 * @return how long to wait before next call, or 0 for never (until adjacent tile update)
	 */ 
	public float update(@NotNull Tile tile) {
		DataMap dataMap = tile.getDataMap(this);
		
		float now = tile.getWorld().getGameTime();
		float lastUpdate = dataMap.getOrDefault(CacheTag.LastUpdate, now);
		float delta = now - lastUpdate;
		dataMap.put(CacheTag.LastUpdate, now);
		
		return updateManager.update(tile, delta);
	}
	
	
	public boolean interact(@NotNull Tile tile, Player player, @Nullable Item item) { return false; }
	
	public boolean attacked(@NotNull Tile tile, WorldObject source, @Nullable Item item, int damage) {
		return destructionManager.tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) { return false; }
	
	@Override
	public final boolean equals(Object other) {
		return other instanceof TileType && ((TileType)other).getEnumType().equals(getEnumType());
	}
	@Override
	public final int hashCode() { return enumType.hashCode(); }
	
	@Override
	public String toString() { return MyUtils.toTitleCase(enumType.name()); }
}
