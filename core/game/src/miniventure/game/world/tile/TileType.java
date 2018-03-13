package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.item.FoodItem;
import miniventure.game.item.ResourceItem;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.AnimationProperty.AnimationType;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;
import miniventure.game.world.tile.DestructibleProperty.RequiredTool;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	HOLE(() -> {
		return new TileProperty[] {
			SolidProperty.SOLID,
			new ConnectionProperty(true, TileType.valueOf("WATER"))
		};
	}),
	
	DIRT(() -> new TileProperty[] {
		SolidProperty.WALKABLE,
		new DestructibleProperty(true, new RequiredTool(ToolType.Shovel))
	}),
	
	SAND(() -> new TileProperty[] {
		SolidProperty.WALKABLE,
		new DestructibleProperty(true, new RequiredTool(ToolType.Shovel)),
		new OverlapProperty(true)
	}),
	
	GRASS(() -> new TileProperty[] {
		SolidProperty.WALKABLE,
		new DestructibleProperty(true, new RequiredTool(ToolType.Shovel)),
		new OverlapProperty(true)
	}),
	
	WATER(() -> new TileProperty[] {
		new AnimationProperty(true, AnimationType.RANDOM, 0.2f, AnimationType.SEQUENCE, 1/24f),
		new SpreadUpdateProperty(HOLE),
		new OverlapProperty(true)
	}),
	
	STONE(() -> new TileProperty[] {
		SolidProperty.SOLID,
		new DestructibleProperty(20, new PreferredTool(ToolType.Pickaxe, 5), true)
	}),
	
	DOOR_CLOSED(() -> new TileProperty[] {
		SolidProperty.SOLID,
		new DestructibleProperty(true, new RequiredTool(ToolType.Axe)),
		new AnimationProperty(true, AnimationType.SINGLE_FRAME),
		(InteractableProperty) (p, i, t) -> {
			t.replaceTile(TileType.valueOf("DOOR_OPEN"));
			return true;
		}
	}),
	
	DOOR_OPEN(() -> new TileProperty[] {
		SolidProperty.WALKABLE,
		new AnimationProperty(false, AnimationType.SINGLE_FRAME),
		new TransitionProperty(
			new TransitionAnimation("open", true, 1/24f, DOOR_CLOSED),
			new TransitionAnimation("close", false, 1/24f, DOOR_CLOSED)
		),
		(InteractableProperty) (p, i, t) -> {
			t.replaceTile(DOOR_CLOSED);
			return true;
		},
		new DestructibleProperty(new ItemDrop(TileItem.get(TileType.DOOR_CLOSED)), new RequiredTool(ToolType.Axe))
	}),
	
	TORCH(() -> new TileProperty[] {
		new DestructibleProperty(true),
		(LightProperty) () -> 2,
		new AnimationProperty(false, AnimationType.SEQUENCE, 1/12f),
		new TransitionProperty(new TransitionAnimation("enter", true, 1/12f))
	}),
	
	CACTUS(() -> new TileProperty[] {
		SolidProperty.SOLID,
		new DestructibleProperty(7, null, true),
		new AnimationProperty(false, AnimationType.SINGLE_FRAME),
		(TouchListener) (e, t) -> e.attackedBy(t, null, 1)
	}),
	
	TREE(() -> new TileProperty[] {
		SolidProperty.SOLID,
		new DestructibleProperty(10, new PreferredTool(ToolType.Axe, 2), new ItemDrop(ResourceItem.Log.get()), new ItemDrop(FoodItem.Apple.get())),
		new AnimationProperty(false, AnimationType.SINGLE_FRAME),
		new ConnectionProperty(true)
	});
	
	/*LAVA(() -> new TileProperty[] {
		(TouchListener) (e, t) -> e.attackedBy(t, null, 5),
		new AnimationProperty.RandomFrame(0.1f)
	});*/
	
	/*
		Others:
		wheat, farmland, door, floor, wall, stairs?, sapling, torch, ore, ice, cloud?,
		laser source, laser, mirror, laser receiver.
	 */
	
	
	private final HashMap<Class<? extends TileProperty>, TileProperty> propertyMap = new HashMap<>();
	
	private final HashMap<Class<? extends TileProperty>, Integer> propertyDataIndexes = new HashMap<>();
	private final HashMap<Class<? extends TileProperty>, Integer> propertyDataLengths = new HashMap<>();
	private String[] initialData;
	
	interface PropertyFetcher {
		TileProperty[] getProperties();
	}
	
	private final PropertyFetcher fetcher;
	
	TileType(@NotNull PropertyFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	public static final TileType[] values = TileType.values();
	
	static {
		for(TileType type: TileType.values)
			type.init();
	}
	
	private void init() {
		// add the default properties
		for(TileProperty prop: TileProperty.getDefaultProperties())
			propertyMap.put(castProp(prop), prop);
		
		// replace the defaults with specified properties
		for(TileProperty prop: fetcher.getProperties())
			propertyMap.put(castProp(prop), prop);
		
		for(TileProperty prop: propertyMap.values())
			prop.init(this);
		
		Array<String> initData = new Array<>();
		
		for(TileProperty prop: propertyMap.values()) {
			propertyDataIndexes.put(castProp(prop), initData.size);
			String[] propData = prop.getInitialData();
			propertyDataLengths.put(castProp(prop), propData.length);
			initData.addAll(propData);
		}
		
		initialData = new String[initData.size];
		for(int i = 0; i < initialData.length; i++)
			initialData[i] = initData.get(i);
	}
	
	/* What I've learned:
		Casting with parenthesis works because the generic type is replaced by Object during runtime, or, if you've specified bounds, as specific a class as you can get with the specified bounds.
		But calling (T var).getClass().cast(Tile t) doesn't always work because getClass() returns the actual class of the generic variable, and that may not be compatible with whatever you're trying to cast.
	 */
	
	private static Class<? extends TileProperty> castProp(TileProperty prop) { return castProp(prop.getClass()); }
	private static Class<? extends TileProperty> castProp(Class<? extends TileProperty> clazz) {
		return MyUtils.getDirectSubclass(TileProperty.class, clazz);
	}
	
	public <T extends TileProperty> T getProp(Class<T> clazz) {
		//noinspection unchecked
		return (T) propertyMap.get(castProp(clazz));
	}
	
	int getDataLength() { return initialData.length; }
	String[] getInitialData() { return Arrays.copyOf(initialData, initialData.length); }
	
	void checkDataAccess(Class<? extends TileProperty> prop, int propDataIndex) {
		Class<? extends TileProperty> property = castProp(prop);
		// technically, the below should never happen, unless it's passed the TileProperty class or a dynamically generated class, or something, because the propertyMap should have an instance of each implementer of the TileProperty interface.
		if(!propertyDataIndexes.containsKey(property))
			throw new IllegalArgumentException("The specified property class, " + property + ", is not part of the list of property classes for the "+this+" tile type.");
		
		if(propDataIndex >= propertyDataLengths.get(property))
			throw new IllegalArgumentException("Tile property " + property + ", for the "+this+" tile type, tried to access index past stated length; length="+propertyDataLengths.get(property)+", index="+propDataIndex);
	}
	
	int getPropDataIndex(Class<? extends TileProperty> prop) { return propertyDataIndexes.get(castProp(prop)); }
	int getPropDataLength(Class<? extends TileProperty> prop) { return propertyDataLengths.get(castProp(prop)); }
	
	public String getName() { return MyUtils.toTitleCase(name()); }
}
