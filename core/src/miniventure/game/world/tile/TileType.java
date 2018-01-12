package miniventure.game.world.tile;

import java.util.Comparator;
import java.util.HashMap;

import miniventure.game.MyUtils;
import miniventure.game.item.ToolType;
import miniventure.game.world.tile.AnimationProperty.AnimationType;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	HOLE(
		SolidProperty.SOLID,
		new ConnectionProperty(true)
	),
	
	DIRT(
		new CoveredTileProperty(HOLE),
		new DestructibleProperty(true)
	),
	
	GRASS(
		new CoveredTileProperty(DIRT),
		new DestructibleProperty(true),
		new OverlapProperty(true)
	),
	
	SAND(
		new CoveredTileProperty(HOLE),
		new DestructibleProperty(true),
		new OverlapProperty(true)
	),
	
	ROCK(false,
		SolidProperty.SOLID,
		new CoveredTileProperty(DIRT),
		new DestructibleProperty(20, new PreferredTool(ToolType.PICKAXE, 5))
	),
	
	TREE(false,
		SolidProperty.SOLID,
		new CoveredTileProperty(GRASS),
		new DestructibleProperty(10, new PreferredTool(ToolType.AXE, 2)),
		new AnimationProperty(AnimationType.SINGLE_FRAME),
		new ConnectionProperty(true)
	),
	
	CACTUS(false,
		SolidProperty.SOLID,
		new CoveredTileProperty(SAND),
		new DestructibleProperty(7, null),
		new AnimationProperty(AnimationType.SINGLE_FRAME),
		(TouchListener) (e, t) -> e.hurtBy(t, 1)
	),
	
	WATER(
		new CoveredTileProperty(HOLE),
		new AnimationProperty(AnimationType.RANDOM, 0.2f, AnimationType.SEQUENCE, 1/16f),
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
	
	
	private final boolean isGroundTile;
	private final HashMap<Class<? extends TileProperty>, TileProperty> propertyMap;
	
	private final HashMap<Class<? extends TileProperty>, Integer> propertyDataIndexes = new HashMap<>();
	private final HashMap<Class<? extends TileProperty>, Integer> propertyDataLengths = new HashMap<>();
	private final Integer[] initialData;
	
	TileType(@NotNull TileProperty... properties) { this(true, properties); }
	TileType(boolean isGroundTile, @NotNull TileProperty... properties) {
		this.isGroundTile = isGroundTile;
		
		// get the default properties
		propertyMap = TileProperty.getDefaultPropertyMap();
		
		// replace the defaults with specified properties
		for(TileProperty property: properties) {
			Class<? extends TileProperty> clazz = MyUtils.getDirectSubclass(TileProperty.class, property.getClass());
			//System.out.println("found class " + clazz);
			propertyMap.put(clazz, property);
		}
		
		for(TileProperty prop: propertyMap.values())
			prop.init(this);
		
		Array<Integer> initData = new Array<>();
		
		for(TileProperty prop: propertyMap.values()) {
			propertyDataIndexes.put(prop.getClass(), initData.size);
			Integer[] propData = prop.getInitData();
			propertyDataLengths.put(prop.getClass(), propData.length);
			initData.addAll(propData);
		}
		
		initialData = new Integer[initData.size];
		for(int i = 0; i < initialData.length; i++)
			initialData[i] = initData.get(i);
	}
	
	/// POST-INITIALIZATION
	static {
		HOLE.getProp(ConnectionProperty.class).addConnectingType(WATER);
	}
	
	boolean isGroundTile() { return isGroundTile; }
	
	public <T extends TileProperty> T getProp(Class<T> clazz) {
		//noinspection unchecked
		return (T)propertyMap.get(clazz);
	}
	
	int getDataLength() { return initialData.length; }
	
	int[] getInitialData() {
		int[] data = new int[initialData.length];
		for(int i = 0; i < data.length; i++)
			data[i] = initialData[i];
		
		return data;
	}
	
	
	void checkDataAccess(Class<? extends TileProperty> property, int propDataIndex) {
		if(!propertyDataIndexes.containsKey(property))
			throw new IllegalArgumentException("specified property " + property + " is not from this tile's type, "+this+".");
		
		if(propDataIndex >= propertyDataLengths.get(property))
			throw new IndexOutOfBoundsException("tile property " + property + " tried to access index past stated length; length="+propertyDataLengths.get(property)+", index="+propDataIndex);
	}
	
	int getPropDataIndex(Class<? extends TileProperty> prop) { return propertyDataIndexes.get(prop); }
	int getPropDataLength(Class<? extends TileProperty> prop) { return propertyDataLengths.get(prop); }
	
	
	public static final TileType[] values = TileType.values();
	public static final TileType[] zOrder = { // first tile is drawn over by all.
		HOLE, DIRT, SAND, GRASS, WATER, ROCK, CACTUS, TREE
	};
	
	public static final Comparator<TileType> tileSorter = (t1, t2) -> {
		if(t1 == t2) return 0;
		for(TileType type: zOrder) {
			if(type == t1) return -1;
			if(type == t2) return 1;
		}
		
		return 0; // shouldn't ever reach here...
	};
}
