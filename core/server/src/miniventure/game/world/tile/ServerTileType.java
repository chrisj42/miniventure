package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.item.FoodType;
import miniventure.game.item.Item;
import miniventure.game.item.PlaceableItemType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.ParamMap;
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
	
	
	public interface P {
		TParam<DestructionManager> DESTRUCT = new TParam<>(DestructionManager::INDESTRUCTIBLE);
		TParam<UpdateManager> UPDATE = new TParam<>(UpdateManager::new);
		TParam<TransitionManager> TRANS = new TParam<>(TransitionManager::new);
		// TParam<TileItem> ITEM = new TParam<>(type -> null);
	}
	
	private final HashMap<TParam<?>, TileProperty> propertyMap;
	// private boolean initializing = false;
	// private boolean initialized = false;
	
	private ServerTileType(@NotNull TileTypeEnum type) {
		super(type);
		propertyMap = new HashMap<>(4);
	}
	
	private void initProperties(TValue<?>... params) {
		// if(initialized || initializing) {
		// 	System.err.println("ServerTileType initProperties was called more than once for type "+getTypeEnum());
		// 	return;
		// }
		ParamMap map = new ParamMap(params);
		TileTypeEnum type = getTypeEnum();
		
		// initializing = true;
		// propertyMap.put(P.ITEM, map.get(P.ITEM).get(type)); // important to create and set the item first
		propertyMap.put(P.DESTRUCT, map.get(P.DESTRUCT).get(type));
		propertyMap.put(P.UPDATE, map.get(P.UPDATE).get(type));
		propertyMap.put(P.TRANS, map.get(P.TRANS).get(type));
		propertyMap.forEach((param, prop) -> prop.registerDataTypes(this));
		// initialized = true;
		// initializing = false;
	}
	
	@SuppressWarnings("unchecked")
	// this fetches the given property from this type's property map.
	public <T extends TileProperty> T get(TParam<T> property) {
		/*T prop =*/return (T) propertyMap.get(property);
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
	public static <T extends TileProperty> T get(TileTypeEnum type, TParam<T> property) {
		return get(type).get(property);
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
	
	/**
	 * Called to update the tile's state in some way, whenever an adjacent tile is updated. It is also called once on tile load to get the first value and determine future calls.
	 * If this returns a value greater than 0, then it is called after at least the specified amount of time has passed. Otherwise, it won't be updated until an adjacent tile is updated.
	 *
	 * @param tile the tile
	 * @return how long to wait before next call, or 0 for never (until adjacent tile update)
	 */
	public float update(@NotNull ServerTile tile) {
		if(!get(P.TRANS).playingAnimation(tile) && !get(P.UPDATE).doesUpdate())
			return 0;
		
		TileTypeDataMap dataMap = tile.getDataMap(getTypeEnum());
		
		float now = tile.getWorld().getGameTime();
		float lastUpdate = dataMap.getOrDefault(TileDataTag.LastUpdate, now);
		float delta = now - lastUpdate;
		dataMap.put(TileDataTag.LastUpdate, now);
		
		return get(P.UPDATE).update(tile, delta);
	}
	
	
	public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) { return Result.NONE; }
	
	public Result attacked(@NotNull ServerTile tile, WorldObject source, @Nullable ServerItem item, int damage) {
		return get(P.DESTRUCT).tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) { return false; }
	
	
	private enum ServerTileTypeEnum {
		
		HOLE(),
		
		DIRT(
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(PlaceableItemType.Dirt.get()),
				new RequiredTool(ToolType.Shovel)
			))
		),
		
		SAND(
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(PlaceableItemType.Sand.get()),
				new RequiredTool(ToolType.Shovel)
			))
		),
		
		GRASS(
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(PlaceableItemType.Dirt.get()),
				new RequiredTool(ToolType.Shovel)
			)),
			
			P.UPDATE.as(type ->
				new UpdateManager(type,
					new SpreadUpdateAction(type, FloatFetcher.random(5, 30, 60), .95f,
						(newType, tile) -> tile.addTile(newType),
						TileTypeEnum.DIRT
					)
				)
			)
		),
		
		STONE_PATH(
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(ResourceType.Stone.get(), 2),
				new RequiredTool(ToolType.Pickaxe)
			))
		),
		
		SNOW(
			P.DESTRUCT.as(type -> new DestructionManager.DestructibleBuilder(type)
				.drops(
					new ItemDrop(PlaceableItemType.Snow.get()),
					new ItemDrop(FoodType.Snow_Berries.get(), 0, 1, .1f)
				)
				.require(new RequiredTool(ToolType.Shovel))
				.make()
			)
		),
		
		SMALL_STONE(
			P.DESTRUCT.as(type -> new DestructionManager(type, new ItemDrop(ResourceType.Stone.get())))
		),
		
		FLOWER(
			P.DESTRUCT.as(type -> new DestructionManager(type, null))
		),
		
		STICK(
			P.DESTRUCT.as(type -> new DestructionManager(type, new ItemDrop(ResourceType.Stick.get())))
		),
		
		WATER(
			P.UPDATE.as(type -> new UpdateManager(type,
				new SpreadUpdateAction(type, 0.33f,
					(newType, tile) -> tile.addTile(newType), TileTypeEnum.HOLE)
			))
		),
		
		REEDS(
			P.DESTRUCT.as(type -> new DestructionManager(type, new ItemDrop(ResourceType.Reed.get())))
		),
		
		DOCK(type -> new ServerTileType(type)
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
			P.DESTRUCT.as(type -> new DestructionManager(type, 40,
				new PreferredTool(ToolType.Pickaxe, 5),
				new ItemDrop(ResourceType.Stone.get(), 2, 3)
			))
		),
		
		STONE_FLOOR(
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(ResourceType.Stone.get(), 3),
				new RequiredTool(ToolType.Pickaxe)
			))
		),
		
		WOOD_WALL(
			P.DESTRUCT.as(type -> 
				new DestructionManager(type, 20,
					new PreferredTool(ToolType.Axe, 3),
					new ItemDrop(ResourceType.Log.get(), 3)
				)
			)
		),
		
		STONE_WALL(
			P.DESTRUCT.as(type -> 
				new DestructionManager(type, 40,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(ResourceType.Stone.get(), 3)
				)
			)
		),
		
		OPEN_DOOR(type -> new ServerTileType(type)
		{
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.replaceTile(CLOSED_DOOR.getType());
				return Result.INTERACT;
			}
		},
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(ResourceType.Log.get(), 3),
				new RequiredTool(ToolType.Axe)
			)),
			
			P.TRANS.as(type -> new TransitionManager(type)
				.addEntrance("open", 24, TileTypeEnum.CLOSED_DOOR)
				.addExit("close", 24, TileTypeEnum.CLOSED_DOOR)
			)
		),
		
		CLOSED_DOOR(type -> new ServerTileType(type)
		{
			@Override
			public Result interact(@NotNull ServerTile tile, Player player, @Nullable ServerItem item) {
				tile.replaceTile(OPEN_DOOR.getType());
				return Result.INTERACT;
			}
		},
			P.DESTRUCT.as(type -> new DestructionManager(type,
				new ItemDrop(ResourceType.Log.get(), 3),
				new RequiredTool(ToolType.Axe)
			))
		),
		
		// note / to-do: I could pretty easily make torches melt adjacent snow...
		TORCH(
			P.DESTRUCT.as(type -> new DestructionManager(type, new ItemDrop(PlaceableItemType.Torch.get()))),
			P.TRANS.as(type -> new TransitionManager(type)
				.addEntrance("enter", 12)
			)
		),
		
		CACTUS(type -> new ServerTileType(type)
		{
			@Override
			public boolean touched(@NotNull ServerTile tile, Entity entity, boolean initial) {
				return entity.attackedBy(tile, null, 1).success;
			}
		},
			P.DESTRUCT.as(type -> new DestructionManager(type, 12, null,
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
		private final MapFunction<TileTypeEnum, ServerTileType> fetcher;
		private final TValue<?>[] params;
		private final TileTypeEnum mainEnum;
		
		ServerTileTypeEnum(TValue<?>... params) { this(ServerTileType::new, params); }
		ServerTileTypeEnum(MapFunction<TileTypeEnum, ServerTileType> fetcher, TValue<?>... params) {
			this.fetcher = fetcher;
			this.params = params;
			mainEnum = TileTypeEnum.value(ordinal());
		}
		
		public ServerTileType getType() {
			if(type == null) {
				type = fetcher.get(mainEnum);
				type.initProperties(params);
			}
			return type;
		}
		
		private static final ServerTileTypeEnum[] values = ServerTileTypeEnum.values();
		public static ServerTileTypeEnum value(int ord) { return values[ord]; }
	}
	
	private interface ServerTileFactory {
		static TValue<?>[] ore(ResourceType oreType, int health) {
			return new TValue<?>[] {
				P.DESTRUCT.as(type -> new DestructionManager(type, health,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(oreType.get(), 3, 4)
				))
			};
		}
		
		TValue<?>[] tree = {
			P.DESTRUCT.as(type -> new DestructionManager(type, 24,
				new PreferredTool(ToolType.Axe, 2),
				new ItemDrop(ResourceType.Log.get(), 2),
				new ItemDrop(FoodType.Apple.get(), 0, 2, 0.32f)
			))
		};
	}
}
