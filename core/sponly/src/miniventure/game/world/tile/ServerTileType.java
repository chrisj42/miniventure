package miniventure.game.world.tile;

import miniventure.game.item.FoodType;
import miniventure.game.item.PlaceableItemType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.customenum.GEnumMap;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.PropertyEnum;
import miniventure.game.util.function.MapFunction;
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
import miniventure.game.world.tile.Tile.TileContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ServerTileType extends TileType {
	
	public static void init() {
		for(ServerTileTypeEnum type: ServerTileTypeEnum.values)
			type.getType();
	}
	
	private static class TypeFetchPropertyEnum<T, E extends TypeFetchPropertyEnum<T, E>> extends PropertyEnum<MapFunction<TileTypeEnum, T>, E> {
		protected TypeFetchPropertyEnum(MapFunction<TileTypeEnum, T> defaultValue) {
			super(defaultValue);
		}
	}
	public static class P<T extends TileProperty> extends TypeFetchPropertyEnum<T, P<T>> {
		public static final P<DestructionManager> DESTRUCT = new P<>(DestructionManager.INDESTRUCTIBLE);
		public static final P<UpdateManager> UPDATE = new P<>(UpdateManager::new);
		public static final P<TransitionManager> TRANS = new P<>(TransitionManager::new);
		
		public static final P<?>[] values = GenericEnum.values(P.class);
		
		private P(MapFunction<TileTypeEnum, T> mapFunction) {
			super(mapFunction);
		}
	}
	
	@FunctionalInterface
	private interface TypeFetcher {
		ServerTileType get(@NotNull TileTypeEnum tileType, Value<?>[] params);
	}
	
	// private final HashMap<TParam<?>, Object> propertyMap;
	private final GEnumMap<P> propertyMap;
	// private boolean initializing = false;
	// private boolean initialized = false;
	
	private ServerTileType(@NotNull TileTypeEnum type, Value<?>[] params) {
		super(type);
		propertyMap = new GEnumMap<>(P.class);
	}
	
	@SuppressWarnings("unchecked")
	private void initProperties(Value<?>... params) {
		// if(initialized || initializing) {
		// 	System.err.println("ServerTileType initProperties was called more than once for type "+getTypeEnum());
		// 	return;
		// }
		ParamMap map = new ParamMap(params);
		// TileTypeEnum type = getTypeEnum();
		// initializing = true;
		for(P prop: P.values) {
			prop.addToMap(propertyMap, map, this);
		}
		// propertyMap.put(P.ITEM, map.get(P.ITEM).get(type)); // important to create and set the item first
		// propertyMap.put(P.DESTRUCT, map.get(P.DESTRUCT).get(type));
		// propertyMap.put(P.UPDATE, map.get(P.UPDATE).get(type));
		// propertyMap.put(P.TRANS, map.get(P.TRANS).get(type));
		// initialized = true;
		// initializing = false;
		
		// get(P.DESTRUCT)
	}
	
	// this fetches the given property from this type's property map.
	public <T extends TileProperty> T get(P<T> property) {
		/*T prop =*/return propertyMap.get(property);
		// String propClass = prop.getClass().getSimpleName();
		// if(initializing)
		// 	GameCore.debug(propClass+" property fetch during initialization of ServerTileType "+getTypeEnum());
		// else if(!initialized)
		// 	GameCore.debug(propClass+" property fetch before initialization of ServerTileType "+getTypeEnum());
		// return prop;
	}
	
	// returns the type instance of the given type.
	public static ServerTileType get(@NotNull TileTypeEnum type) {
		return ServerTileTypeEnum.value(type.ordinal()).getType();
	}
	
	// combines the above two methods into a single neat call.
	public static <T extends TileProperty> T get(TileTypeEnum type, P<T> property) {
		return get(type).get(property);
	}
	public static <T extends TileProperty> T get(TileContext context, P<T> property) {
		return get(context.getType().getTypeEnum(), property);
	}
	
	// used initially by destruction manager to get tile type item while the ServerTileType object is still being constructed; also used normally throughout the code to get TileItems.
	/*public static TileItem getItem(TileTypeEnum type) {
		TileItem item = get(type, P.ITEM);
		if(item == null)
			System.err.println("warning! TileType "+type+" has no TileItem, but one is being requested. Null will be returned.");
		return item;
	}*/
	
	// it turns out that I solved the problem of initial data differently: datamap.getOrDefaultAndPut().
	// after all, initial data is data that should be a given, and therefore shouldn't need to be stored.
	// public SerialMap getInitialData() { return new SerialMap(); }
	
	/*
	 * Called to update the tile's state in some way, whenever an adjacent tile is updated. It is also called once on tile load to get the first value and determine future calls.
	 * If this returns a value greater than 0, then it is called after at least the specified amount of time has passed. Otherwise, it won't be updated until an adjacent tile is updated.
	 *
	 * @param context the tile context
	 * @return how long to wait before next call, or 0 for never (until adjacent tile update)
	 */
	/*public float update(@NotNull Tile.TileContext context, float delta) {
		// TileDataMap dataMap = tile.getDataMap(getTypeEnum());
		
		float now = context.getWorld().getGameTime();
		float delayOriginal = context.getData(TileDataTag.UpdateTimer);
		float delayLeft = now - delayOriginal;
		
		if(delayLeft <= 0)
			delayLeft = get(P.UPDATE).update(context, delta);
		
		context.setData(TileDataTag.UpdateTimer, delayLeft);
		return delayLeft;
	}*/
	
	
	public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) { return Result.NONE; }
	
	public Result attacked(@NotNull TileContext tile, WorldObject source, @Nullable ServerItem item, int damage) {
		return get(P.DESTRUCT).tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) { return false; }
	
	
	private enum ServerTileTypeEnum {
		
		HOLE(),
		
		DIRT(
			new DestructionManager(
				new ItemDrop(PlaceableItemType.Dirt.get()),
				new RequiredTool(ToolType.Shovel)
			)
		),
		
		SAND(
			new DestructionManager(
				new ItemDrop(PlaceableItemType.Sand.get()),
				new RequiredTool(ToolType.Shovel)
			)
		),
		
		GRASS(
			new DestructionManager(
				new ItemDrop(PlaceableItemType.Dirt.get()),
				new RequiredTool(ToolType.Shovel)
			),
			
			new UpdateManager(
				new SpreadUpdateAction(FloatFetcher.random(5, 30, 60), .95f,
					(newType, tile) -> tile.addTile(newType.getTypeEnum()),
					TileTypeEnum.DIRT
				)
			)
		),
		
		STONE_PATH(
			new DestructionManager(
				new ItemDrop(ResourceType.Stone.get(), 2),
				new RequiredTool(ToolType.Pickaxe)
			)
		),
		
		SNOW(
			new DestructionManager.DestructibleBuilder()
				.drops(
					new ItemDrop(PlaceableItemType.Snow.get()),
					new ItemDrop(FoodType.Snow_Berries.get(), 0, 1, .1f)
				)
				.require(new RequiredTool(ToolType.Shovel))
				.make()
		),
		
		SMALL_STONE(
			new DestructionManager(new ItemDrop(ResourceType.Stone.get()))
		),
		
		WATER(
			new UpdateManager(
				new SpreadUpdateAction(0.33f,
					(newType, tile) -> tile.addTile(newType.getTypeEnum()), TileTypeEnum.HOLE)
			)
		),
		
		DOCK((type, params) -> new ServerTileType(type, params)
		{
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.getServer().sendToPlayer((ServerPlayer) player, tile.getWorld().getMapData());
				return Result.INTERACT;
			}
		}),
		
		COAL_ORE(ServerTileFactory.ore(ResourceType.Coal, 25)),
		IRON_ORE(ServerTileFactory.ore(ResourceType.Iron, 35)),
		TUNGSTEN_ORE(ServerTileFactory.ore(ResourceType.Tungsten, 45)),
		RUBY_ORE(ServerTileFactory.ore(ResourceType.Ruby, 60)),
		
		STONE(
			P.DESTRUCT.as(type -> new DestructionManager(40,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(ResourceType.Stone.get(), 2, 3)
			))
		),
		
		STONE_FLOOR(
			P.DESTRUCT.as(type -> new DestructionManager(
				new ItemDrop(ResourceType.Stone.get(), 3),
				new RequiredTool(ToolType.Pickaxe)
			))
		),
		
		WOOD_WALL(
			P.DESTRUCT.as(type -> 
				new DestructionManager(20,
					new PreferredTool(ToolType.Axe, 3),
					new ItemDrop(ResourceType.Log.get(), 3)
				)
			)
		),
		
		STONE_WALL(
			P.DESTRUCT.as(type -> 
				new DestructionManager(40,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(ResourceType.Stone.get(), 3)
				)
			)
		),
		
		OPEN_DOOR((type, params) -> new ServerTileType(type, params)
		{
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.replaceTile(TileTypeEnum.CLOSED_DOOR);
				return Result.INTERACT;
			}
		},
			P.DESTRUCT.as(type -> new DestructionManager(
				new ItemDrop(ResourceType.Log.get(), 3),
				new RequiredTool(ToolType.Axe)
			)),
			
			P.TRANS.as(type -> new TransitionManager(type)
				.addEntrance("open", 24, TileTypeEnum.CLOSED_DOOR)
				.addExit("close", 24, TileTypeEnum.CLOSED_DOOR)
			)
		),
		
		CLOSED_DOOR((type, params) -> new ServerTileType(type, params)
		{
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.replaceTile(TileTypeEnum.OPEN_DOOR);
				return Result.INTERACT;
			}
		},
			P.DESTRUCT.as(type -> new DestructionManager(
				new ItemDrop(ResourceType.Log.get(), 3),
				new RequiredTool(ToolType.Axe)
			))
		),
		
		// note / to-do: I could pretty easily make torches melt adjacent snow...
		TORCH(
			P.DESTRUCT.as(type -> new DestructionManager(new ItemDrop(PlaceableItemType.Torch.get()))),
			P.TRANS.as(type -> new TransitionManager(type)
				.addEntrance("enter", 12)
			)
		),
		
		CACTUS((type, params) -> new ServerTileType(type, params)
		{
			@Override
			public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) {
				return entity.attackedBy(tile, null, 1).success;
			}
		},
			P.DESTRUCT.as(type -> new DestructionManager(12, null,
				new ItemDrop(FoodType.Cactus_Fruit.get(), 1, 2, .15f)
			))
		),
		
		CARTOON_TREE(ServerTileFactory.tree),
		DARK_TREE(ServerTileFactory.tree),
		PINE_TREE(ServerTileFactory.tree),
		POOF_TREE(ServerTileFactory.tree),
		
		AIR();
		
		/** @noinspection NonFinalFieldInEnum*/
		private ServerTileType type = null;
		private final TypeFetcher fetcher;
		private final Value<?>[] params;
		private final TileTypeEnum mainEnum;
		
		// ServerTileTypeEnum(DataEntry<?, P<?>>... params) {
		ServerTileTypeEnum(DestructionManager destructionManager)
		ServerTileTypeEnum(Value<?>... params) { this(ServerTileType::new, params); }
		ServerTileTypeEnum(TypeFetcher fetcher, Value<?>... params) {
			this.fetcher = fetcher;
			this.params = params;
			mainEnum = TileTypeEnum.value(ordinal());
		}
		
		public ServerTileType getType() {
			if(type == null) {
				type = fetcher.get(mainEnum, params);
				type.initProperties(params);
			}
			return type;
		}
		
		private static final ServerTileTypeEnum[] values = ServerTileTypeEnum.values();
		public static ServerTileTypeEnum value(int ord) { return values[ord]; }
	}
	
	private interface ServerTileFactory {
		static Value<?>[] ore(ResourceType oreType, int health) {
			return new Value[] {
				P.DESTRUCT.as(type -> new DestructionManager(health,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(oreType.get(), 3, 4)
				))
			};
		}
		
		Value<?>[] tree = {
			P.DESTRUCT.as(type -> new DestructionManager(24,
				new PreferredTool(ToolType.Axe, 2),
				new ItemDrop(ResourceType.Log.get(), 2),
				new ItemDrop(FoodType.Apple.get(), 0, 2, 0.32f)
			))
		};
	}
}
