package miniventure.game.world.tile;

import miniventure.game.item.FoodType;
import miniventure.game.item.Item;
import miniventure.game.item.ResourceType;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolType;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.SpreadUpdateAction.FloatFetcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ServerTileType extends TileType {
	
	@FunctionalInterface
	private interface ManagerFetcher<T> extends MapFunction<TileTypeEnum, T> {}
	
	@FunctionalInterface
	private interface P {
		Param<ManagerFetcher<UpdateManager>> UPDATE = new Param<>(UpdateManager::new);
		Param<ManagerFetcher<TransitionManager>> TRANS = new Param<>(TransitionManager::new);
		// TODO interaction manager
		
		ServerTileType get(TileTypeEnum type);
	}
	
	private final boolean walkable;
	private final float speedRatio;
	public final DestructionManager destructionManager;
	public final UpdateManager updateManager;
	public final TransitionManager transitionManager;
	
	private ServerTileType(@NotNull TileTypeEnum type, boolean walkable, DestructionManager destructionManager, Value... params) {
		this(type, walkable, 1, destructionManager, params);
	}
	private ServerTileType(@NotNull TileTypeEnum type, boolean walkable, float speedRatio, DestructionManager destructionManager, Value... params) {
		super(type);
		this.walkable = walkable;
		this.speedRatio = speedRatio;
		this.destructionManager = destructionManager;
		ParamMap map = new ParamMap(params);
		updateManager = map.get(P.UPDATE).get(type);
		transitionManager = map.get(P.TRANS).get(type);
	}
	
	public static ServerTileType get(TileTypeEnum type) {
		return ServerTileTypeEnum.value(type.ordinal()).getType();
	}
	
	public SerialMap getInitialData() { return new SerialMap(); }
	
	public boolean isWalkable() {
		return walkable;
	}
	
	public float getSpeedRatio() {
		return speedRatio;
	}
	
	/**
	 * Called to update the tile's state in some way, whenever an adjacent tile is updated. It is also called once on tile load to get the first value and determine future calls.
	 * If this returns a value greater than 0, then it is called after at least the specified amount of time has passed. Otherwise, it won't be updated until an adjacent tile is updated.
	 *
	 * @param tile the tile
	 * @return how long to wait before next call, or 0 for never (until adjacent tile update)
	 */
	public float update(@NotNull ServerTile tile) {
		SerialMap dataMap = tile.getDataMap(this);
		
		float now = tile.getWorld().getGameTime();
		float lastUpdate = dataMap.getOrDefault(TileCacheTag.LastUpdate, now);
		float delta = now - lastUpdate;
		dataMap.put(TileCacheTag.LastUpdate, now);
		
		return updateManager.update(tile, delta);
	}
	
	
	public boolean interact(@NotNull ServerTile tile, Player player, @Nullable Item item) { return false; }
	
	public boolean attacked(@NotNull ServerTile tile, WorldObject source, @Nullable Item item, int damage) {
		return destructionManager.tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) { return false; }
	
	
	private enum ServerTileTypeEnum {
		
		HOLE(type -> new ServerTileType(type, true,
			DestructionManager.INDESTRUCTIBLE(type)
		)),
		
		DIRT(type -> new ServerTileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel))
		)),
		
		SAND(type -> new ServerTileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel))
		)),
		
		GRASS(type -> new ServerTileType(type, true,
			new DestructionManager.DestructibleBuilder(type, 1, false)
				.require(new RequiredTool(ToolType.Shovel))
				.make(),
			
			P.UPDATE.as(type1 ->
				new UpdateManager(type1,
					new SpreadUpdateAction(type1, FloatFetcher.random(5, 30, 60), .95f,
						(newType, tile) -> tile.addTile(newType),
						TileTypeEnum.DIRT
					)
				)
			)
		)),
		
		STONE_PATH(type -> new ServerTileType(type, true,
			new DestructionManager(type,
				new ItemDrop(ResourceType.Stone.get(), 2),
				new RequiredTool(ToolType.Pickaxe)
			)
		)),
		
		SNOW(type -> new ServerTileType(type, true,
			new DestructionManager.DestructibleBuilder(type, true)
				.drops(false,
					new ItemDrop(FoodType.Snow_Berries.get(), 0, 1, .1f)
				)
				.require(new RequiredTool(ToolType.Shovel))
				.make()
		)),
		
		FLINT(type -> new ServerTileType(type, true,
			new DestructionManager(type)
		)),
		
		WATER(type -> new ServerTileType(type, true, 0.6f,
			DestructionManager.INDESTRUCTIBLE(type),
			
			P.UPDATE.as(type1 -> new UpdateManager(type1,
				new SpreadUpdateAction(type1, 0.33f,
					(newType, tile) -> tile.addTile(newType), TileTypeEnum.HOLE)
			))
		)),
		
		COAL(type -> ServerTileFactory.ore(type, 25)),
		IRON(type -> ServerTileFactory.ore(type, 35)),
		TUNGSTEN(type -> ServerTileFactory.ore(type, 45)),
		RUBY(type -> ServerTileFactory.ore(type, 60)),
		
		STONE(type -> new ServerTileType(type, false,
			new DestructionManager(type, 40,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(ResourceType.Stone.get(), 2, 3)
			)
		)),
		
		STONE_FLOOR(type -> new ServerTileType(type, true,
			new DestructionManager(type, new RequiredTool(ToolType.Pickaxe))
		)),
		
		WOOD_WALL(type -> new ServerTileType(type, false,
			new DestructionManager(type, 20, new PreferredTool(ToolType.Axe, 3))
		)),
		
		STONE_WALL(type -> new ServerTileType(type, false,
			new DestructionManager(type, 40, new PreferredTool(ToolType.Pickaxe, 5))
		)),
		
		DOOR_OPEN(type -> new ServerTileType(type, true,
			new DestructionManager(type,
				new ItemDrop(TileItem.get(TileTypeEnum.DOOR_CLOSED)),
				new RequiredTool(ToolType.Axe)
			),
			
			P.TRANS.as(enumType -> new TransitionManager(enumType)
				.addEntranceAnimations(new ServerTileTransition("open", 3/24f, TileTypeEnum.DOOR_CLOSED))
				.addExitAnimations(new ServerTileTransition("close", 3/24f, TileTypeEnum.DOOR_CLOSED))
			)
		) {
			@Override
			public boolean interact(@NotNull ServerTile tile, Player player, @Nullable Item item) {
				tile.replaceTile(DOOR_CLOSED.getType());
				return true;
			}
		}),
		
		DOOR_CLOSED(type -> new ServerTileType(type, false,
			new DestructionManager(type, new RequiredTool(ToolType.Axe))
		) {
			@Override
			public boolean interact(@NotNull ServerTile tile, Player player, @Nullable Item item) {
				tile.replaceTile(DOOR_OPEN.getType());
				return true;
			}
		}),
		
		TORCH(type -> new ServerTileType(type, true,
			new DestructionManager(type),
			P.TRANS.as(type1 -> new TransitionManager(type1)
				.addEntranceAnimations(new ServerTileTransition("enter", 3/12f)))
		)),
		
		CACTUS(type -> new ServerTileType(type, false,
			new DestructionManager(type, 12, null,
				new ItemDrop(FoodType.Cactus_Fruit.get(), 1, 2, .15f)
			)
		) {
			@Override
			public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) {
				return entity.attackedBy(tile, null, 1);
			}
		}),
		
		CARTOON_TREE(ServerTileFactory::tree),
		DARK_TREE(ServerTileFactory::tree),
		PINE_TREE(ServerTileFactory::tree),
		POOF_TREE(ServerTileFactory::tree);
		
		/** @noinspection NonFinalFieldInEnum*/
		private ServerTileType type = null;
		private final P fetcher;
		
		ServerTileTypeEnum(P fetcher) {
			this.fetcher = fetcher;
		}
		
		public ServerTileType getType() {
			if(type == null)
				type = fetcher.get(TileTypeEnum.value(ordinal()));
			return type;
		}
		
		private static final ServerTileTypeEnum[] values = ServerTileTypeEnum.values();
		public static ServerTileTypeEnum value(int ord) { return values[ord]; }
	}
	
	private interface ServerTileFactory {
		static ServerTileType ore(TileTypeEnum type, int health) {
			return new ServerTileType(type, false,
				new DestructionManager(type, health,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(ResourceType.Iron.get(), 3, 4)
				)
			);
		}
		
		static ServerTileType tree(TileTypeEnum type) {
			return new ServerTileType(type, false,
				new DestructionManager(type, 24,
					new PreferredTool(ToolType.Axe, 2),
					new ItemDrop(ResourceType.Log.get(), 2),
					new ItemDrop(FoodType.Apple.get(), 0, 2, 0.32f)
				)
			);
		}
	}
}
