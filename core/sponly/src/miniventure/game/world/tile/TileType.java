package miniventure.game.world.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

import miniventure.game.item.FoodType;
import miniventure.game.item.Item;
import miniventure.game.item.PlaceableItemType;
import miniventure.game.item.ResourceType;
import miniventure.game.item.Result;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.GEnumMap;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.param.Param;
import miniventure.game.util.param.ParamMap;
import miniventure.game.util.param.Value;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.tile.DestructionManager.PreferredTool;
import miniventure.game.world.tile.DestructionManager.RequiredTool;
import miniventure.game.world.tile.SpreadUpdateAction.FloatFetcher;
import miniventure.game.world.worldgen.island.ProtoTile;
import miniventure.game.world.worldgen.island.TileProcessor;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.world.tile.TileTypeRenderer.ConnectionCheck.list;
import static miniventure.game.world.tile.TileTypeRenderer.buildRenderer;

public enum TileType implements TileProcessor, TileTypeSource {
	
	HOLE(true, true,
		Prop.RENDER.as(type -> buildRenderer(type)
			.connect(list(type, TileTypeEnum.WATER))
			.swim(new SwimAnimation(type, 0.75f))
			.build()
		)
	),
	DIRT(true, true, P.COLOR.as(new Color(200, 100, 0)),
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(PlaceableItemType.Dirt.get()),
					new RequiredTool(ToolType.Shovel)
			))
	),
	SAND(true, true, P.COLOR.as(Color.YELLOW),
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(PlaceableItemType.Sand.get()),
					new RequiredTool(ToolType.Shovel)
			))
	),
	GRASS(true, true, P.COLOR.as(Color.GREEN),
			P.UNDER.as(DIRT),
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(PlaceableItemType.Dirt.get()), 
					new RequiredTool(ToolType.Shovel)
			)),
			Prop.UPDATE.as(() -> new UpdateManager(
					new SpreadUpdateAction(FloatFetcher.random(5, 30, 60), .95f,
							(newType, tile) -> tile.addTile(newType),
							TileType.DIRT
					)
			))
	),
	
	// todo repurpose this to a tile I can use for rocky beaches
	STONE_PATH(true, true,
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(ResourceType.Stone.get(), 2),
					new RequiredTool(ToolType.Pickaxe)
			))
	),
	
	SNOW(true, true, P.COLOR.as(Color.WHITE),
			Prop.DESTRUCT.as(() -> new DestructionManager.DestructibleBuilder()
					.drops(
							new ItemDrop(PlaceableItemType.Snow.get()),
							new ItemDrop(FoodType.Snow_Berries.get(), 0, 1, .1f)
					)
					.require(new RequiredTool(ToolType.Shovel))
					.make()
			)
	),
	
	SMALL_STONE(true, true, P.COLOR.as(Color.LIGHT_GRAY),
			P.UNDER.as(GRASS),
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(ResourceType.Stone.get())
			))
	),
	
	WATER(0.6f, true, P.COLOR.as(Color.BLUE.darker()),
			Prop.RENDER.as(type -> buildRenderer(type,
					new RenderStyle(PlayMode.LOOP_RANDOM, 5),
					new RenderStyle(true, 24)
					)
					.swim(new SwimAnimation(type))
					.build()
			),
			Prop.UPDATE.as(() -> new UpdateManager(
					new SpreadUpdateAction(0.33f,
							(newType, tile) -> tile.addTile(newType), TileType.HOLE)
			))
	),
	
	DOCK(true, false) {
		@Override
		public Result interact(@NotNull Tile tile, Player player, @Nullable Item item) {
			// todo bring up map menu, but locally now
			// tile.getServer().sendToPlayer((Player) player, tile.getWorld().getMapData());
			return Result.INTERACT;
		}
	},
	
	COAL_ORE(false, true, TileFactory.ore(ResourceType.Coal, 25)),
	IRON_ORE(false, true, TileFactory.ore(ResourceType.Iron, 35)),
	TUNGSTEN_ORE(false, true, TileFactory.ore(ResourceType.Tungsten, 45)),
	RUBY_ORE(false, true, TileFactory.ore(ResourceType.Ruby, 60)),
	
	STONE(false, true, P.COLOR.as(Color.GRAY),
			P.UNDER.as(DIRT),
			Prop.RENDER.as(type -> buildRenderer(type)
					.connect(list(TileTypeEnum.STONE, TileType.COAL_ORE, TileType.IRON_ORE, TileType.TUNGSTEN_ORE, TileType.RUBY_ORE))
					.build()
			),
			Prop.DESTRUCT.as(() -> new DestructionManager(40,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(ResourceType.Stone.get(), 2, 3)
			))
	),
	
	STONE_FLOOR(true, true,
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(ResourceType.Stone.get(), 3),
					new RequiredTool(ToolType.Pickaxe)
			))
	),
	WOOD_WALL(false, false,
			Prop.DESTRUCT.as(() -> new DestructionManager(20,
					new PreferredTool(ToolType.Axe, 3),
					new ItemDrop(ResourceType.Log.get(), 3)
			))
	), // by saying it's not opaque, grass will still connect under it
	STONE_WALL(false, false,
			Prop.DESTRUCT.as(() -> new DestructionManager(40,
					new PreferredTool(ToolType.Pickaxe, 5),
					new ItemDrop(ResourceType.Stone.get(), 3)
			))
	),
	
	OPEN_DOOR(true, false,
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(ResourceType.Log.get(), 3),
					new RequiredTool(ToolType.Axe)
			)),
			Prop.TRANS.as(type -> new TransitionManager.TransitionBuilder(type)
					.addEntrance("open", new RenderStyle(PlayMode.NORMAL, 24), TileTypeEnum.CLOSED_DOOR)
					.addExit("close", new RenderStyle(PlayMode.NORMAL, 24), TileTypeEnum.CLOSED_DOOR)
					.build()
			)
	) {
		@Override
		public Result interact(@NotNull Tile tile, Player player, @Nullable Item item) {
			tile.replaceTile(TileType.CLOSED_DOOR);
			return Result.INTERACT;
		}
	},
	
	CLOSED_DOOR(false, false,
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(ResourceType.Log.get(), 3),
					new RequiredTool(ToolType.Axe)
			))
	) {
		@Override
		public Result interact(@NotNull Tile tile, Player player, @Nullable Item item) {
			tile.replaceTile(TileType.OPEN_DOOR);
			return Result.INTERACT;
		}
	},
	
	TORCH(true, false,
			Prop.RENDER.as(type -> buildRenderer(type, new RenderStyle(12))
					.lightRadius(2f)
					.build()
			),
			Prop.DESTRUCT.as(() -> new DestructionManager(
					new ItemDrop(PlaceableItemType.Torch.get())
			)),
			Prop.TRANS.as(type -> new TransitionManager.TransitionBuilder(type)
					.addEntrance("enter", new RenderStyle(12))
					.build()
			)
	),
	
	CACTUS(false, false, P.COLOR.as(Color.GREEN.darker().darker()),
			Prop.DESTRUCT.as(() -> new DestructionManager(12, null,
					new ItemDrop(FoodType.Cactus_Fruit.get(), 1, 2, .15f)
			))
	) {
		@Override
		public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) {
			return entity.attackedBy(tile, null, 1).success;
		}
	},
	
	CARTOON_TREE(false, false, TileFactory.tree),
	DARK_TREE(false, false, TileFactory.tree),
	PINE_TREE(false, false, TileFactory.tree),
	POOF_TREE(false, false, TileFactory.tree),
	
	AIR(true, false);
	
	
	@Override
	public TileType getType() { return this; }
	
	private interface TileFactory {
		static Value<?>[] ore(ResourceType oreType, int health) {
			return new Value[] {
					P.UNDER.as(DIRT),
					Prop.RENDER.as(type -> buildRenderer(type)
							.connect(list(TileType.STONE, TileType.COAL_ORE, TileType.IRON_ORE, TileType.TUNGSTEN_ORE, TileType.RUBY_ORE))
							.build()
					),
					Prop.DESTRUCT.as(() -> new DestructionManager(health,
							new PreferredTool(ToolType.Pickaxe, 5),
							new ItemDrop(oreType.get(), 3, 4)
					))
			};
		}
		
		Value<?>[] tree = {
				P.COLOR.as(Color.GREEN.darker().darker()),
				Prop.DESTRUCT.as(type -> new DestructionManager(24,
						new PreferredTool(ToolType.Axe, 2),
						new ItemDrop(ResourceType.Log.get(), 2),
						new ItemDrop(FoodType.Apple.get(), 0, 2, 0.32f)
				))	
		};
	}
	
	
	private interface P {
		Param<Color> COLOR = new Param<>(Color.BLACK);
		
		// the tile that is found beneath this one by default. Used when the bottom of the stack is reached.
		// nulls will result in HOLE
		Param<TileType> UNDER = new Param<>(null);
		
	}
	
	public static class Prop<T extends TileProperty> extends TilePropertyGroup<T, Prop<T>> {
		
		public static final Prop<TileTypeRenderer> RENDER = new Prop<TileTypeRenderer>(TileTypeRenderer::basicRenderer);
		
		public static final Prop<DestructionManager> DESTRUCT = new Prop<>(DestructionManager.INDESTRUCTIBLE);
		
		public static final Prop<UpdateManager> UPDATE = new Prop<>(UpdateManager.NONE);
		
		public static final Prop<TransitionManager> TRANS = new Prop<>(TransitionManager.NONE);
		
		// private final MapFunction<TileType, T> propertyFetcher;
		
		private Prop(T defaultValue) { this(type -> defaultValue); }
		private Prop(MapFunction<TileType, T> defaultValue) {
			super(defaultValue);
		}
		
		/*void transfer(GEnumMap<Prop> propMap, ParamMap paramMap, TileType tileType) {
			T property = paramMap.get(super.fetchParam).get(tileType);
			property.registerDataTags(tileType);
			propMap.add(this, property);
		}*/
	}
	
	private static class TileDataSet extends GEnumOrderedDataSet<TileDataTag> {
		TileDataSet() {
			super(TileDataTag.class);
		}
	}
	
	@NotNull
	private final Color color;
	private final boolean walkable;
	private final float speedRatio;
	
	private final boolean opaque;
	@Nullable
	private final TileType underType;
	
	private final GEnumMap<Prop> propertyMap;
	
	private final TileDataSet dataSet;
	private final TileDataSet topSet;
	
	private final ParamMap tempMap;
	
	TileType(boolean walkable, boolean opaque, Value<?>... properties) {
		this(walkable ? 1 : 0, opaque, properties);
	}
	TileType(float speedRatio, boolean opaque, Value<?>... properties) {
		this.walkable = speedRatio != 0;
		this.speedRatio = speedRatio;
		this.opaque = opaque;
		
		tempMap = new ParamMap(properties);
		this.color = tempMap.get(P.COLOR);
		this.underType = tempMap.get(P.UNDER);
		
		dataSet = new TileDataSet();
		topSet = new TileDataSet();
		
		// init actual tile properties
		propertyMap = new GEnumMap<>(Prop.class);
	}
	
	private void initProperties() {
		for(Prop prop: GenericEnum.values(Prop.class)) {
			prop.addToMap(propertyMap, tempMap, this);
		}
	}
	
	
	public boolean isWalkable() { return walkable; }
	public float getSpeedRatio() { return speedRatio; }
	public boolean isOpaque() { return opaque; }
	
	public TileType getUnderType() { return underType == null ? TileType.HOLE : underType; }
	public boolean hasUnderType() { return underType != null; }
	@NotNull
	public Color getColor() { return color; }
	
	public <T extends TileProperty> T get(Prop<T> prop) {
		return propertyMap.get(prop);
	}
	
	
	public Result interact(@NotNull Tile tile, Player player, @Nullable Item item) { return Result.NONE; }
	
	public Result attacked(@NotNull Tile.TileContext tile, WorldObject source, @Nullable Item item, int damage) {
		return get(Prop.DESTRUCT).tileAttacked(tile, source, item, damage);
	}
	
	public boolean touched(@NotNull Tile tile, Entity entity, boolean initial) { return false; }
	
	
	void addDataTag(TileDataTag<?> dataTag) {
		if(dataTag.perLayer)
			topSet.addKey(dataTag);
		else
			dataSet.addKey(dataTag);
	}
	public Object[] createDataArray() {
		return dataSet.createDataArray();
	}
	public Object[] createTopDataArray() {
		return topSet.createDataArray();
	}
	
	// different tile types will use different data tags, and the tags that are used should be packed efficiently into an array.
	public boolean hasData(TileDataTag<?> dataTag) {
		TileDataSet set = dataTag.perLayer ? topSet : dataSet;
		return set.getDataIndex(dataTag) >= 0;
	}
	// this method will provide the index into the data array for a certain tag, for this tile type.
	public <T> T getData(TileDataTag<T> dataTag, Object[] dataArray, Object[] topArray) {
		Object[] ar = dataTag.perLayer ? topArray : dataArray;
		TileDataSet set = dataTag.perLayer ? topSet : dataSet;
		int idx = set.getDataIndex(dataTag);
		return idx >= 0 ? (T) ar[idx] : null;
	}
	public <T> void setData(TileDataTag<T> dataTag, T value, Object[] dataArray, Object[] topArray) {
		Object[] ar = dataTag.perLayer ? topArray : dataArray;
		TileDataSet set = dataTag.perLayer ? topSet : dataSet;
		ar[set.getDataIndex(dataTag)] = value;
	}
	
	// make topArray null to prevent it from being drawn from
	/*public void serializeData(TileTypeDataMap map, Object[] dataArray, @Nullable Object[] topArray) {
		for(TileDataTag tag: TileDataTag.values) {
			Object[] ar = tag.topOnly ? topArray : dataArray;
			if(ar == null) continue;
			TileDataSet set = tag.topOnly ? topSet : dataSet;
			int idx = set.getDataIndex(tag);
			if(idx < 0 || ar[idx] == null) continue;
			map.add(tag, ar[idx]);
		}
	}*/
	
	private static class DataIterator implements Iterator<String> {
		
		private final TileType tileType;
		// private boolean save;
		private Object[] data;
		private Object[] top;
		// private int pos;
		private int nextPos;
		private boolean giveOrdinal = true;
		
		DataIterator(TileType tileType) {
			this.tileType = tileType;
		}
		
		void reset(/*boolean save, */Object[] data, Object[] top) {
			// this.save = save;
			this.data = data;
			this.top = top;
			// pos = -1;
			nextPos = -1;
			giveOrdinal = true;
			next();
		}
		
		@Override
		public boolean hasNext() {
			return nextPos < TileDataTag.values.length;
		}
		
		@Override
		public String next() {
			if(!hasNext()) return null;
			
			String serial = null;
			if(nextPos >= 0) {
				if (giveOrdinal) {
					giveOrdinal = false;
					return String.valueOf(TileDataTag.values[nextPos].ordinal());
				} else {
					giveOrdinal = true;
					serial = getSerial(TileDataTag.values[nextPos]);
				}
			}
			
			// find next pos
			do nextPos++;
			while(hasNext() && !currentPosValid());
			
			return serial;
		}
		
		private <T> String getSerial(TileDataTag<T> tag) {
			return tag.serialize(tileType.getData(tag, data, top));
		}
		
		private boolean currentPosValid() {
			TileDataTag<?> tag = TileDataTag.values[nextPos];
			// is this tag part of the data set?
			// if(save && !tag.save || !save && !tag.send)
			//	return false;
			Object[] ar = tag.perLayer ? top : data;
			if(ar == null || !tileType.hasData(tag))
				return false;
			return true;
		}
	}
	private final DataIterator dataIterator = new DataIterator(this);
	private final Iterable<String> dataIterable = () -> dataIterator;
	
	// make topArray null to prevent it from being drawn from
	public String serializeData(/*boolean save, */Object[] dataArray, @Nullable Object[] topArray) {
		dataIterator.reset(/*save, */dataArray, topArray);
		return MyUtils.encodeStringArray(dataIterable);
	}
	
	public void deserializeData(String data, Object[] dataArray, @Nullable Object[] topArray) {
		String[] dataAr = MyUtils.parseLayeredString(data);
		for (int i = 0; i < dataAr.length; i+=2) {
			TileDataTag<?> tag = TileDataTag.values[Integer.parseInt(dataAr[i])];
			deserializeTagData(dataAr[i+1], tag, dataArray, topArray);
		}
	}
	
	private <T> void deserializeTagData(String data, TileDataTag<T> tag, Object[] dataArray, Object[] topArray) {
		setData(tag, tag.deserialize(data), dataArray, topArray);
	}
	
	public String getName() { return MyUtils.toTitleCase(name(), "_"); }
	
	@Override
	public String toString() { return getName()+' '+getClass().getSimpleName(); }
	
	@Override
	// adds this tile type to the tile stack.
	public void processTile(ProtoTile tile) {
		tile.addLayer(this);
	}
	
	static abstract class TilePropertyGroup<T extends TileProperty, E extends TilePropertyGroup<T, E>> extends GenericEnum<T, E> {
		
		private Param<MapFunction<TileType, T>> fetchParam;
		
		TilePropertyGroup(MapFunction<TileType, T> defaultValue) {
			fetchParam = new Param<>(defaultValue);
		}
		
		public Value<MapFunction<TileType, T>> as(MapFunction<TileType, T> value) {
			return fetchParam.as(value);
		}

		public Value<MapFunction<TileType, T>> as(T value) {
			return as(type -> value);
		}
		
		public Value<MapFunction<TileType, T>> as(FetchFunction<T> value) {
			return as(type -> value.get());
		}
		
		public void addToMap(GEnumMap<E> propertyMap, ParamMap paramMap, TileType tileType) {
			T value = paramMap.get(fetchParam).get(tileType);
			propertyMap.add(this, value);
			value.registerDataTags(tileType);
		}
	}
	
	public enum TypeGroup {
		GROUND(DIRT, GRASS, SAND, STONE_PATH, STONE_FLOOR, SNOW);
		
		private final EnumSet<TileType> types;
		
		TypeGroup(TileType... types) {
			this.types = EnumSet.copyOf(Arrays.asList(types));
		}
		
		public boolean contains(TileType type) {
			return types.contains(type);
		}
	}
	
	// a second copy in case some back references are needed during creation.
	private enum TileTypeEnum implements TileTypeSource {
		HOLE,
		DIRT,
		SAND,
		GRASS,
		STONE_PATH,
		SNOW,
		SMALL_STONE,
		WATER,
		DOCK,
		COAL_ORE,
		IRON_ORE,
		TUNGSTEN_ORE,
		RUBY_ORE,
		STONE,
		STONE_FLOOR,
		WOOD_WALL,
		STONE_WALL,
		OPEN_DOOR,
		CLOSED_DOOR,
		TORCH,
		CACTUS,
		CARTOON_TREE,
		DARK_TREE,
		PINE_TREE,
		POOF_TREE,
		AIR;
		
		@Override
		public TileType getType() {
			return TileType.values[ordinal()];
		}
	}
	
	public static TileType[] values = TileType.values();
	
	public static void init() {
		for(TileType type: TileType.values)
			type.initProperties();
	}
}
