package miniventure.game.world.tile;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import miniventure.game.GameCore;
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
	
	DIRT(true,
		new DestructibleProperty(HOLE, null)
	),
	
	GRASS(true,
		new DestructibleProperty(DIRT, null),
		new OverlapProperty(true)
	),
	
	SAND(true,
		new DestructibleProperty(HOLE, null),
		new OverlapProperty(true)
	),
	
	ROCK(
		SolidProperty.SOLID,
		new DestructibleProperty(20, DIRT, new PreferredTool(ToolType.PICKAXE, 5))
	),
	
	TREE(
		SolidProperty.SOLID,
		new DestructibleProperty(10, GRASS, new PreferredTool(ToolType.AXE, 2)),
		new AnimationProperty(GRASS, AnimationType.SINGLE_FRAME),
		new ConnectionProperty(true)
	),
	
	CACTUS(
		SolidProperty.SOLID,
		new DestructibleProperty(7, SAND, null),
		new AnimationProperty(SAND, AnimationType.SINGLE_FRAME),
		(TouchListener) (e, t) -> e.hurtBy(t, 1)
	),
	
	WATER(
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
		water, lava, sand, stone, cactus, wheat, farmland, door, floor, wall, stairs?, sapling, torch, ore, cloud?,
		laser source, laser, mirror, laser receiver.
		ice
		
		
	 */
	
	public final boolean maySpawn;
	final SolidProperty solidProperty;
	final DestructibleProperty destructibleProperty;
	final InteractableProperty interactableProperty;
	final TouchListener touchListener;
	final AnimationProperty animationProperty;
	final ConnectionProperty connectionProperty;
	final OverlapProperty overlapProperty;
	final UpdateProperty updateProperty;
	
	final HashMap<TileProperty, Integer> propertyDataIndexes = new HashMap<>();
	final HashMap<TileProperty, Integer> propertyDataLengths = new HashMap<>();
	private final Integer[] initialData;
	
	TileType(@NotNull TileProperty... properties) {
		this(false, properties);
	}
	TileType(boolean maySpawn, @NotNull TileProperty... properties) {
		this.maySpawn = maySpawn;
		
		// get the default properties
		LinkedHashMap<String, TileProperty> propertyMap = TileProperty.getDefaultPropertyMap();
		
		// replace the defaults with specified properties
		for(TileProperty property: properties) {
			Class clazz = GameCore.getDirectSubclass(TileProperty.class, property.getClass());
			if(clazz == null) throw new NullPointerException();
			String className = clazz.getName();
			if(className.contains("$")) className = className.substring(0, className.indexOf("$")); // strip off extra stuff generated if it was a lambda expression
			propertyMap.put(className, property);
		}
		
		this.solidProperty = (SolidProperty)propertyMap.get(SolidProperty.class.getName());
		this.destructibleProperty = (DestructibleProperty)propertyMap.get(DestructibleProperty.class.getName());
		this.interactableProperty = (InteractableProperty)propertyMap.get(InteractableProperty.class.getName());
		this.touchListener = (TouchListener)propertyMap.get(TouchListener.class.getName());
		this.animationProperty = (AnimationProperty)propertyMap.get(AnimationProperty.class.getName());
		this.connectionProperty = (ConnectionProperty)propertyMap.get(ConnectionProperty.class.getName());
		this.overlapProperty = (OverlapProperty)propertyMap.get(OverlapProperty.class.getName());
		this.updateProperty = (UpdateProperty)propertyMap.get(UpdateProperty.class.getName());
		
		this.connectionProperty.addConnectingType(this);
		
		Array<Integer> initData = new Array<Integer>();
		
		for(TileProperty prop: propertyMap.values()) {
			propertyDataIndexes.put(prop, initData.size);
			Integer[] propData = prop.getInitData();
			propertyDataLengths.put(prop, propData.length);
			initData.addAll(propData);
		}
		
		initialData = new Integer[initData.size];
		for(int i = 0; i < initialData.length; i++)
			initialData[i] = initData.get(i);
	}
	
	/// POST-INITIALIZATION
	static {
		HOLE.connectionProperty.addConnectingType(WATER);
	}
	
	int[] getInitialData() {
		int[] data = new int[initialData.length];
		for(int i = 0; i < data.length; i++)
			data[i] = initialData[i];
		
		return data;
	}
	
	
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
