package miniventure.game.world.tile;

import java.util.HashMap;
import java.util.LinkedHashMap;

import miniventure.game.GameCore;
import miniventure.game.item.ToolType;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	HOLE(),
	
	DIRT(
		new DestructibleProperty(HOLE, null)
	),
	
	GRASS(
		new DestructibleProperty(DIRT, null)
	),
	
	SAND(
		new DestructibleProperty(HOLE, null)
	),
	
	ROCK(
		SolidProperty.SOLID,
		new DestructibleProperty(20, DIRT, new PreferredTool(ToolType.PICKAXE, 5))
	),
	
	TREE(
		SolidProperty.SOLID,
		new DestructibleProperty(10, GRASS, new PreferredTool(ToolType.AXE, 2))
	),
	
	CACTUS(
		SolidProperty.SOLID,
		new DestructibleProperty(7, SAND, null),
		(TouchListener) Entity::hurtBy
	),
	
	WATER(
		new AnimationProperty.RandomFrame(0.2f),
		new SpreadUpdateProperty(HOLE)
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
	
	final SolidProperty solidProperty;
	final DestructibleProperty destructibleProperty;
	final InteractableProperty interactableProperty;
	final TouchListener touchListener;
	final AnimationProperty animationProperty;
	final UpdateProperty updateProperty;
	
	final HashMap<TileProperty, Integer> propertyDataIndexes = new HashMap<>();
	final HashMap<TileProperty, Integer> propertyDataLengths = new HashMap<>();
	private final Integer[] initialData;
	
	TileType(@NotNull TileProperty... properties) {
		// get the default properties
		LinkedHashMap<String, TileProperty> propertyMap = TileProperty.getDefaultPropertyMap();
		
		//System.out.println("making tile type: " + this);
		// replace the defaults with specified properties
		for(TileProperty property: properties) {
			Class clazz = GameCore.getDirectSubclass(TileProperty.class, property.getClass());
			if(clazz == null) throw new NullPointerException();
			String className = clazz.getName();
			//System.out.println(className);
			if(className.contains("$")) className = className.substring(0, className.indexOf("$")); // strip off extra stuff generated if it was a lambda expression
			TileProperty oldProp = propertyMap.put(className, property);
			//System.out.println("replaced property of class " + className + ": " + oldProp + ", with new property: " + property);
		}
		
		this.solidProperty = (SolidProperty)propertyMap.get(SolidProperty.class.getName());
		this.destructibleProperty = (DestructibleProperty)propertyMap.get(DestructibleProperty.class.getName());
		this.interactableProperty = (InteractableProperty)propertyMap.get(InteractableProperty.class.getName());
		this.touchListener = (TouchListener)propertyMap.get(TouchListener.class.getName());
		this.animationProperty = (AnimationProperty)propertyMap.get(AnimationProperty.class.getName());
		this.updateProperty = (UpdateProperty)propertyMap.get(UpdateProperty.class.getName());
		
		if(GameCore.tileAtlas != null)
			animationProperty.initialize(GameCore.tileAtlas.findRegions(name().toLowerCase()));
		
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
	
	int[] getInitialData() {
		int[] data = new int[initialData.length];
		for(int i = 0; i < data.length; i++)
			data[i] = initialData[i];
		
		return data;
	}
	
	
	
	public static final TileType[] values = TileType.values();
}
