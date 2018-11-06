package miniventure.game.world.tile;

import java.awt.Color;

import miniventure.game.item.FoodType;
import miniventure.game.item.Item;
import miniventure.game.item.ResourceType;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.PropertyMap;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.SpreadUpdateAction.FloatFetcher;
import miniventure.game.world.tile.data.TileCacheTag;
import miniventure.game.world.tile.data.TilePropertyTag;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileType {
	
	private static final Color BROWN = new Color(138, 66, 16);
	
	public enum TileTypeEnum {
		
		HOLE(BROWN.darker(), type -> new TileType(type, true,
			new PropertyMap(TilePropertyTag.Swim.as(new SwimAnimation(type, 0.75f))),
			
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
		
		GRASS(Color.GREEN, type -> new TileType(type, true, new PropertyMap(),
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
		
		STONE_PATH(Color.GRAY, type -> new TileType(type, true,
			new DestructionManager(type,
				new ItemDrop(ResourceType.Stone.get(), 2),
				new RequiredTool(ToolType.Pickaxe)
			),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		)),
		
		SNOW(Color.WHITE, type -> new TileType(type, true,
			new DestructionManager.DestructibleBuilder(type, true)
				.drops(false,
					new ItemDrop(FoodType.Snow_Berries.get(), 0, 1, .1f)
				)
				.require(new RequiredTool(ToolType.Shovel))
				.make(),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		)),
		
		FLINT(Color.DARK_GRAY, type -> new TileType(type, true,
			new DestructionManager(type),
			new TileTypeRenderer(type, false)
		)),
		
		WATER(Color.BLUE, type -> new TileType(type, true,
			new PropertyMap(TilePropertyTag.SpeedRatio.as(0.6f))
			.add(TilePropertyTag.Swim.as(new SwimAnimation(type))),
			
			DestructionManager.INDESTRUCTIBLE(type),
			
			new TileTypeRenderer(type, true,
				new ConnectionManager(type, new RenderStyle(PlayMode.LOOP_RANDOM, 0.2f)),
				new OverlapManager(type, new RenderStyle(true, 1/24f))
			),
			new UpdateManager(type,
				new SpreadUpdateAction(type, 0.33f, (newType, tile) -> tile.addTile(newType), HOLE)
			)
		)),
		
		COAL(Color.BLACK, type -> new OreTile(type, 25)),
		IRON(new Color(205, 212, 210), type -> new OreTile(type, 35)),
		TUNGSTEN(new Color(176, 182, 180), type -> new OreTile(type, 45)),
		RUBY(Color.RED, type -> new OreTile(type, 60)),
		
		STONE(Color.GRAY, type -> new TileType(type, false,
			new DestructionManager(type, 40,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(ResourceType.Stone.get(), 2, 3)
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
			new PropertyMap(TilePropertyTag.LightRadius.as(2f)),
			
			new DestructionManager(type),
			
			new TileTypeRenderer(type, false,
				new ConnectionManager(type, new RenderStyle(1/12f)),
				OverlapManager.NONE(type),
				new TransitionManager(type)
					.addEntranceAnimations(new TransitionAnimation("enter", 3/12f))
			)
		)),
		
		CACTUS(Color.GREEN.darker().darker(), type -> new TileType(type, false,
			new DestructionManager(type, 12, null,
				new ItemDrop(FoodType.Cactus_Fruit.get(), 1, 2, .15f)
			),
			
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
		@NotNull private final MapFunction<TileTypeEnum, TileType> tileTypeFetcher;
		private TileType tileType;
		
		TileTypeEnum(@NotNull Color color, @NotNull MapFunction<TileTypeEnum, TileType> typeFetcher) {
			this.color = color;
			tileTypeFetcher = typeFetcher;
		}
		
		private static final TileTypeEnum[] values = values();
		
		public static TileTypeEnum values(int ordinal) { return values[ordinal]; }
		
		/** @noinspection StaticMethodReferencedViaSubclass*/
		public static void init() {
			TileCacheTag.init();
			TilePropertyTag.init();
		}
		
		public TileType getTileType(@NotNull WorldManager world) {
			if(tileType == null)
				tileType = tileTypeFetcher.get(this);
			return world.getTileTypeFetcher().mapValue(this, tileType);
		}
	}
	
	private final TileTypeEnum enumType;
	final PropertyMap propertyMap;
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
		this(enumType, walkable, new PropertyMap(), destructionManager, renderer);
	}
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, PropertyMap propertyMap, DestructionManager destructionManager, TileTypeRenderer renderer) {
		this(enumType, walkable, propertyMap, destructionManager, renderer, new UpdateManager(enumType));
	}
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, PropertyMap propertyMap, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		this.enumType = enumType;
		this.walkable = walkable;
		this.propertyMap = propertyMap;
		this.destructionManager = destructionManager;
		this.renderer = renderer;
		this.updateManager = updateManager;
	}
	
	public SerialMap getInitialData() { return new SerialMap(); }
	
	public boolean hasProperty(TilePropertyTag<?> tag) { return propertyMap.contains(tag); }
	
	public <T> T getPropertyOrDefault(TilePropertyTag<T> tag, T defaultValue) {
		return propertyMap.getOrDefault(tag, defaultValue);
	}
	
	public TileTypeEnum getEnumType() { return enumType; }
	public String getName() { return enumType.name(); }
	
	public TileTypeRenderer getRenderer() { return renderer; }
	
	public boolean isWalkable() { return walkable; }
	
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
		SerialMap dataMap = tile.getDataMap(this);
		
		float now = tile.getWorld().getGameTime();
		float lastUpdate = dataMap.getOrDefault(TileCacheTag.LastUpdate, now);
		float delta = now - lastUpdate;
		dataMap.put(TileCacheTag.LastUpdate, now);
		
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
