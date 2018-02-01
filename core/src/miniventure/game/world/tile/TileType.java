package miniventure.game.world.tile;

import java.util.Comparator;
import java.util.HashMap;

import miniventure.game.item.ResourceItem;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.tile.AnimationProperty.AnimationType;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;
import miniventure.game.world.tile.DestructibleProperty.RequiredTool;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	HOLE(
		SolidProperty.SOLID,
		new CoveredTileProperty(),
		new ConnectionProperty(true)
	),
	
	DIRT(
		new CoveredTileProperty(HOLE),
		new DestructibleProperty(true, new RequiredTool(ToolType.Shovel))
	),
	
	GRASS(
		new CoveredTileProperty(DIRT),
		new DestructibleProperty(true, new RequiredTool(ToolType.Shovel)),
		new OverlapProperty(true)
	),
	
	SAND(
		new CoveredTileProperty(DIRT),
		new DestructibleProperty(true, new RequiredTool(ToolType.Shovel)),
		new OverlapProperty(true)
	),
	
	STONE(
		SolidProperty.SOLID,
		new CoveredTileProperty(DIRT),
		new DestructibleProperty(20, new PreferredTool(ToolType.Pickaxe, 5), true)
	),
	
	TREE(
		SolidProperty.SOLID,
		new CoveredTileProperty(GRASS),
		new DestructibleProperty(10, new PreferredTool(ToolType.Axe, 2), new ItemDrop(ResourceItem.Log.get())),
		new AnimationProperty(false, AnimationType.SINGLE_FRAME),
		new ConnectionProperty(true)
	),
	
	CACTUS(
		SolidProperty.SOLID,
		new CoveredTileProperty(SAND),
		new DestructibleProperty(7, null, true),
		new AnimationProperty(false, AnimationType.SINGLE_FRAME),
		(TouchListener) (e, t) -> e.hurtBy(t, 1)
	),
	
	WATER(
		new CoveredTileProperty(HOLE),
		new AnimationProperty(true, AnimationType.RANDOM, 0.2f, AnimationType.SEQUENCE, 1/16f),
		new SpreadUpdateProperty(HOLE),
		new OverlapProperty(true)
	);
	
	/*LAVA(
		(TouchListener) Entity::hurtBy,
		new AnimationProperty.RandomFrame(0.1f)
	);*/
	
	/*
		Others:
		wheat, farmland, door, floor, wall, stairs?, sapling, torch, ore, ice, cloud?,
		laser source, laser, mirror, laser receiver.
	 */
	
	
	private final HashMap<Class<? extends TileProperty>, TileProperty> propertyMap;
	
	private final HashMap<Class<? extends TileProperty>, Integer> propertyDataIndexes = new HashMap<>();
	private final HashMap<Class<? extends TileProperty>, Integer> propertyDataLengths = new HashMap<>();
	private final String[] initialData;
	
	TileType(@NotNull TileProperty... properties) {
		// get the default properties
		propertyMap = TileProperty.getDefaultPropertyMap();
		
		// replace the defaults with specified properties
		for(TileProperty property: properties) {
			Class<? extends TileProperty> clazz = castProp(property.getClass());
			propertyMap.put(clazz, property);
		}
		
		for(TileProperty prop: propertyMap.values())
			prop.init(this);
		
		Array<String> initData = new Array<>();
		
		for(TileProperty prop: propertyMap.values()) {
			propertyDataIndexes.put(prop.getClass(), initData.size);
			String[] propData = prop.getInitData();
			propertyDataLengths.put(prop.getClass(), propData.length);
			initData.addAll(propData);
		}
		
		initialData = new String[initData.size];
		for(int i = 0; i < initialData.length; i++)
			initialData[i] = initData.get(i);
	}
	
	private static Class<? extends TileProperty> castProp(Class<? extends TileProperty> prop) {
		// to make for less typing and clutter. :)
		return MyUtils.getDirectSubclass(TileProperty.class, prop);
	}
	
	/// POST-INITIALIZATION
	static {
		HOLE.getProp(ConnectionProperty.class).addConnectingType(WATER);
	}
	
	/* What I've learned:
		Casting with parenthesis works because the generic type is replaced by Object during runtime, or, if you've specified bounds, as specific a class as you can get with the specified bounds.
		But calling (T var).getClass().cast(Tile t) doesn't always work because getClass() returns the actual class of the generic variable, and that may not be compatible with whatever you're trying to cast.
	 */
	
	public <T extends TileProperty> T getProp(Class<T> clazz) {
		//noinspection unchecked
		return (T)propertyMap.get(castProp(clazz));
	}
	
	int getDataLength() { return initialData.length; }
	
	String[] getInitialData() {
		String[] data = new String[initialData.length];
		for(int i = 0; i < data.length; i++)
			data[i] = initialData[i];
		
		return data;
	}
	
	void checkDataAccess(Class<? extends TileProperty> property, int propDataIndex) {
		// technically, the below should never happen, unless it's passed the TileProperty class or a dynamically generated class, or something, because the propertyMap should have an instance of each implementer of the TileProperty interface.
		if(!propertyDataIndexes.containsKey(property))
			throw new IllegalArgumentException("The specified property class, " + property + ", is not part of the list of TileType property classes.");
		
		if(propDataIndex >= propertyDataLengths.get(property))
			throw new IllegalArgumentException("Tile property " + property + " tried to access index past stated length; length="+propertyDataLengths.get(property)+", index="+propDataIndex);
	}
	
	int getPropDataIndex(Class<? extends TileProperty> prop) { return propertyDataIndexes.get(castProp(prop)); }
	int getPropDataLength(Class<? extends TileProperty> prop) { return propertyDataLengths.get(castProp(prop)); }
	
	
	public static final TileType[] values = TileType.values();
	public static final TileType[] zOrder = { // first tile is drawn over by all.
		HOLE, DIRT, SAND, GRASS, WATER, STONE, CACTUS, TREE
	};
	
	public static final Comparator<TileType> tileSorter = (t1, t2) -> {
		if(t1 == t2) return 0;
		for(TileType type: zOrder) {
			if(type == t1) return -1;
			if(type == t2) return 1;
		}
		
		return 0; // shouldn't ever reach here...
	};
	
	public String getName() { return MyUtils.toTitleCase(name()); }
}
