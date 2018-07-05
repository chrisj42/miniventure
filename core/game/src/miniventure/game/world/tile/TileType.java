package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.ToolType;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.DataTag;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileType {
	
	public enum TileTypeEnum {
		
		HOLE(type -> new GroundTileType(type, false,
			DestructionManager.INDESTRUCTIBLE(type),
			
			new TileTypeRenderer(type, true,
				new ConnectionManager(type, RenderStyle.SINGLE_FRAME, TileTypeEnum.valueOf("WATER"))
			)
		)),
		
		DIRT(type -> new GroundTileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel)),
			
			new TileTypeRenderer(type, true)
		)),
		
		SAND(type -> new GroundTileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel)),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			))
		),
		
		GRASS(type -> new GroundTileType(type, true,
			new DestructionManager.DestructibleBuilder(type, 1, false)
				.require(new RequiredTool(ToolType.Shovel))
				.make(),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			),
			new UpdateManager(type,
				new SpreadUpdateAction(type, 10, 45,
					(newType, tile) -> tile.addTile(newType),
					DIRT
				)
			)
		)),
		
		SNOW(type -> new GroundTileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel)),
			
			new TileTypeRenderer(type, true,
				new OverlapManager(type, RenderStyle.SINGLE_FRAME)
			)
		)),
		
		WATER(type -> new LiquidTileType(type, true,
			DestructionManager.INDESTRUCTIBLE(type),
			
			new TileTypeRenderer(type, true,
				new ConnectionManager(type, new RenderStyle(PlayMode.LOOP_RANDOM, 0.2f)),
				new OverlapManager(type, new RenderStyle(true, 1/24f))
			),
			new UpdateManager(type,
				new SpreadUpdateAction(type, 0.33f, (newType, tile) -> tile.addTile(newType), HOLE)
			)
		)),
		
		STONE(type -> new SurfaceTileType(type, false,
			new DestructionManager(type, 40,
				new PreferredTool(ToolType.Pickaxe, 5)
			),
			
			new TileTypeRenderer(type, true)
		)),
		
		STONE_FLOOR(type -> new FloorTile(type, ToolType.Pickaxe)),
		
		WOOD_WALL(type -> new WallTile(type, 20, new PreferredTool(ToolType.Axe, 3))),
		
		STONE_WALL(type -> new WallTile(type, 40, new PreferredTool(ToolType.Pickaxe, 5))),
		
		DOOR_OPEN(DoorTile::getOpenDoor),
		
		DOOR_CLOSED(DoorTile::getClosedDoor),
		
		TORCH(type -> new SurfaceTileType(type, true, 2,
			new DestructionManager(type),
			
			new TileTypeRenderer(type, false,
				new ConnectionManager(type, new RenderStyle(1/12f)),
				OverlapManager.NONE(type),
				new TransitionManager(type)
					.addEntranceAnimations(new TransitionAnimation("enter", 3/12f))
			),
			new UpdateManager(type)
		)),
		
		CACTUS(type -> new SurfaceTileType(type, false,
			new DestructionManager(type, 12, null),
			
			new TileTypeRenderer(type, false)
			
		) {
			@Override
			public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) {
				return entity.attackedBy(tile, null, 1);
			}
		}),
		
		TREE_CARTOON(TreeTile::new),
		TREE_DARK(TreeTile::new),
		TREE_PINE(TreeTile::new),
		TREE_POOF(TreeTile::new);
		
		
		
		@NotNull private final ValueMonoFunction<TileTypeEnum, TileType> tileTypeFetcher;
		private TileType tileType;
		
		TileTypeEnum(@NotNull ValueMonoFunction<TileTypeEnum, TileType> typeFetcher) {
			tileTypeFetcher = typeFetcher;
		}
		
		private static final TileTypeEnum[] values = values();
		
		public static TileTypeEnum values(int ordinal) { return values[ordinal]; }
		
		public static void init() { DataTag.init(); }
		
		public TileType getTileType(@NotNull WorldManager world) {
			if(tileType == null)
				tileType = tileTypeFetcher.get(this);
			return world.getTileTypeFetcher().mapValue(this, tileType);
		}
	}
	
	private final TileTypeEnum enumType;
	final boolean walkable;
	final float lightRadius;
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
		this(enumType, walkable, destructionManager, renderer, new UpdateManager(enumType));
	}
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		this(enumType, walkable, 0, destructionManager, renderer, updateManager);
	}
	protected TileType(@NotNull TileTypeEnum enumType, boolean walkable, float lightRadius, DestructionManager destructionManager, TileTypeRenderer renderer, UpdateManager updateManager) {
		this.enumType = enumType;
		this.walkable = walkable;
		this.lightRadius = lightRadius;
		this.destructionManager = destructionManager;
		this.renderer = renderer;
		this.updateManager = updateManager;
	}
	
	public DataMap getInitialData() { return new DataMap(); }
	
	public TileTypeEnum getEnumType() { return enumType; }
	public String getName() { return enumType.name(); }
	
	public float getLightRadius() { return lightRadius; }
	
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
		float lastUpdate = dataMap.getOrDefault(DataTag.LastUpdate, now);
		float delta = now - lastUpdate;
		dataMap.put(DataTag.LastUpdate, now);
		
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
}
