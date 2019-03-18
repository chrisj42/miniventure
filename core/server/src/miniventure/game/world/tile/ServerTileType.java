package miniventure.game.world.tile;

import miniventure.game.GameProtocol.MapRequest;
import miniventure.game.item.FoodType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.item.TileItem;
import miniventure.game.item.TileItem.PlacementCheck;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.SpreadUpdateAction.FloatFetcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ServerTileType extends TileType {
	
	public static void init() {
		for(ServerTileTypeEnum type: ServerTileTypeEnum.values)
			type.getType();
	}
	
	// this exists because the each TileType needs its own manager objects; they can't share the same default object that a Param instance holds. This way, each call makes a new manager.
	// besides, the managers need a reference to their TileType! so you can't even make a default.
	@FunctionalInterface
	private interface ValueFetcher<T> extends MapFunction<TileTypeEnum, T> {}
	
	@FunctionalInterface
	private interface P {
		Param<ValueFetcher<UpdateManager>> UPDATE = new Param<>(UpdateManager::new);
		Param<ValueFetcher<TransitionManager>> TRANS = new Param<>(TransitionManager::new);
		// TODO interaction manager
		
		ServerTileType get(TileTypeEnum type);
	}
	
	public final DestructionManager destructionManager;
	public final UpdateManager updateManager;
	public final TransitionManager transitionManager;
	
	private ServerTileType(@NotNull TileTypeEnum type, DestructionManager destructionManager, Value... params) {
		super(type);
		this.destructionManager = destructionManager;
		ParamMap map = new ParamMap(params);
		updateManager = map.get(P.UPDATE).get(type);
		transitionManager = map.get(P.TRANS).get(type);
	}
	
	public static ServerTileType get(TileTypeEnum type) {
		return ServerTileTypeEnum.value(type.ordinal()).getType();
	}
	
	// used initially by destruction manager to get tile type item while the ServerTileType object is still being constructed; also used normally throughout the code to get TileItems.
	public static TileItem getItem(TileTypeEnum type) {
		TileItem item = ServerTileTypeEnum.value(type.ordinal()).tileItem;
		if(item == null)
			System.err.println("warning! TileType "+type+" has no TileItem, but one is being requested. Null will be returned.");
		return item;
	}
	
	public SerialMap getInitialData() { return new SerialMap(); }
	
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
	
	
	public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) { return Result.NONE; }
	
	public Result attacked(@NotNull ServerTile tile, WorldObject source, @Nullable ServerItem item, int damage) {
		return destructionManager.tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) { return false; }
	
	
	private enum ServerTileTypeEnum {
		
		HOLE(type -> new ServerTileType(type,
			DestructionManager.INDESTRUCTIBLE(type)
		)),
		
		DIRT(type -> new ServerTileType(type,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel))
		), type -> new TileItem(type, true, PlacementCheck.on(TileTypeEnum.HOLE))),
		
		SAND(type -> new ServerTileType(type,
			new DestructionManager(type, new RequiredTool(ToolType.Shovel))
		), type -> new TileItem(type, true, PlacementCheck.on(TileTypeEnum.DIRT))),
		
		GRASS(type -> new ServerTileType(type,
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
		
		STONE_PATH(type -> new ServerTileType(type,
			new DestructionManager(type,
				new ItemDrop(ResourceType.Stone.get(), 2),
				new RequiredTool(ToolType.Pickaxe)
			)
		), type -> new TileItem(type, false, PlacementCheck.groundExcluding(type))),
		
		SNOW(type -> new ServerTileType(type,
			new DestructionManager.DestructibleBuilder(type, false)
				.drops(
					new ItemDrop(getItem(TileTypeEnum.SNOW)),
					new ItemDrop(FoodType.Snow_Berries.get(), 0, 1, .1f)
				)
				.require(new RequiredTool(ToolType.Shovel))
				.make()
		), type -> new TileItem(type, true, PlacementCheck.groundExcluding(type))),
		
		FLINT(type -> new ServerTileType(type,
			new DestructionManager(type, new ItemDrop(ResourceType.Flint.get()))
		)),
		
		WATER(type -> new ServerTileType(type,
			DestructionManager.INDESTRUCTIBLE(type),
			
			P.UPDATE.as(type1 -> new UpdateManager(type1,
				new SpreadUpdateAction(type1, 0.33f,
					(newType, tile) -> tile.addTile(newType), TileTypeEnum.HOLE)
			))
		)),
		
		DOCK(type -> new ServerTileType(type,
			DestructionManager.INDESTRUCTIBLE(type)
		) {
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.getServer().sendToPlayer((ServerPlayer) player, new MapRequest());
				return Result.INTERACT;
			}
		}),
		
		COAL_ORE(type -> ServerTileFactory.ore(type, ResourceType.Coal, 25)),
		IRON_ORE(type -> ServerTileFactory.ore(type, ResourceType.Iron, 35)),
		TUNGSTEN_ORE(type -> ServerTileFactory.ore(type, ResourceType.Tungsten, 45)),
		RUBY_ORE(type -> ServerTileFactory.ore(type, ResourceType.Ruby, 60)),
		
		STONE(type -> new ServerTileType(type,
			new DestructionManager(type, 40,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(ResourceType.Stone.get(), 2, 3)
			)
		)),
		
		STONE_FLOOR(type -> new ServerTileType(type,
			new DestructionManager(type, new RequiredTool(ToolType.Pickaxe))
		), type -> new TileItem(type, false, PlacementCheck.on(TileTypeEnum.HOLE))),
		
		WOOD_WALL(type -> new ServerTileType(type,
			new DestructionManager(type, 20, new PreferredTool(ToolType.Axe, 3))
		), type -> new TileItem(type, false, PlacementCheck.GROUND)),
		
		STONE_WALL(type -> new ServerTileType(type,
			new DestructionManager(type, 40, new PreferredTool(ToolType.Pickaxe, 5))
		), type -> new TileItem(type, false, PlacementCheck.GROUND)),
		
		OPEN_DOOR(type -> new ServerTileType(type,
			new DestructionManager(type,
				new ItemDrop(getItem(TileTypeEnum.CLOSED_DOOR)),
				new RequiredTool(ToolType.Axe)
			),
			
			P.TRANS.as(enumType -> new TransitionManager(enumType)
				.addEntranceAnimations(new ServerTileTransition("open", 3/24f, TileTypeEnum.CLOSED_DOOR))
				.addExitAnimations(new ServerTileTransition("close", 3/24f, TileTypeEnum.CLOSED_DOOR))
			)
		) {
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.replaceTile(CLOSED_DOOR.getType());
				return Result.INTERACT;
			}
		}),
		
		CLOSED_DOOR(type -> new ServerTileType(type,
			new DestructionManager(type, new RequiredTool(ToolType.Axe))
		) {
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.replaceTile(OPEN_DOOR.getType());
				return Result.INTERACT;
			}
		}, type -> new TileItem("Door", type, false, PlacementCheck.GROUND)),
		
		// note / to-do: I could pretty easily make torches melt adjacent snow...
		TORCH(type -> new ServerTileType(type,
			new DestructionManager(type),
			P.TRANS.as(type1 -> new TransitionManager(type1)
				.addEntranceAnimations(new ServerTileTransition("enter", 3/12f)))
		), type -> new TileItem(type, false, PlacementCheck.GROUND)),
		
		CACTUS(type -> new ServerTileType(type,
			new DestructionManager(type, 12, null,
				new ItemDrop(FoodType.Cactus_Fruit.get(), 1, 2, .15f)
			)
		) {
			@Override
			public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) {
				return entity.attackedBy(tile, null, 1).success;
			}
		}),
		
		CARTOON_TREE(ServerTileFactory::tree),
		DARK_TREE(ServerTileFactory::tree),
		PINE_TREE(ServerTileFactory::tree),
		POOF_TREE(ServerTileFactory::tree),
		
		AIR(type -> new ServerTileType(type,
			DestructionManager.INDESTRUCTIBLE(type)
		));
		
		/** @noinspection NonFinalFieldInEnum*/
		private ServerTileType type = null;
		private final P fetcher;
		private final TileTypeEnum mainEnum;
		private final TileItem tileItem;
		
		ServerTileTypeEnum(P fetcher) { this(fetcher, type -> null); }
		ServerTileTypeEnum(P fetcher, MapFunction<TileTypeEnum, TileItem> itemFetcher) {
			this.fetcher = fetcher;
			mainEnum = TileTypeEnum.value(ordinal());
			tileItem = itemFetcher.get(mainEnum);
		}
		
		public ServerTileType getType() {
			if(type == null)
				type = fetcher.get(mainEnum);
			return type;
		}
		
		private static final ServerTileTypeEnum[] values = ServerTileTypeEnum.values();
		public static ServerTileTypeEnum value(int ord) { return values[ord]; }
	}
	
	private interface ServerTileFactory {
		static ServerTileType ore(TileTypeEnum type, ResourceType oreType, int health) {
			return new ServerTileType(type,
				new DestructionManager(type, health,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(oreType.get(), 3, 4)
				)
			);
		}
		
		static ServerTileType tree(TileTypeEnum type) {
			return new ServerTileType(type,
				new DestructionManager(type, 24,
					new PreferredTool(ToolType.Axe, 2),
					new ItemDrop(ResourceType.Log.get(), 2),
					new ItemDrop(FoodType.Apple.get(), 0, 2, 0.32f)
				)
			);
		}
	}
}
