package miniventure.game.world.tile;

import java.util.HashMap;
import java.util.LinkedHashMap;

import miniventure.game.GameCore;
import miniventure.game.item.ToolType;
import miniventure.game.world.tile.AnimationProperty.Animated;
import miniventure.game.world.tile.AnimationProperty.SingleFrame;
import miniventure.game.world.tile.DestructibleProperty.PreferredTool;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	GRASS(),
	
	TREE(
		SolidProperty.SOLID,
		new DestructibleProperty(10, GRASS, new PreferredTool(ToolType.AXE, 2))
	);
	
	final SolidProperty solidProperty;
	final DestructibleProperty destructibleProperty;
	final InteractableProperty interactableProperty;
	final TouchListener touchListener;
	final AnimationProperty animationProperty;
	
	final HashMap<TileProperty, Integer> propertyDataIndexes = new HashMap<>();
	final int dataLength;
	
	TileType(@NotNull TileProperty... properties) {
		// get the default properties
		LinkedHashMap<String, TileProperty> propertyMap = TileProperty.getDefaultPropertyMap();
		
		// replace the defaults with specified properties
		for(TileProperty property: properties) {
			String className = property.getClass().getName();
			if(className.contains("$")) className = className.substring(0, className.indexOf("$")); // strip off extra stuff generated if it was a lambda expression
			propertyMap.put(className, property);
		}
		
		// fetch the animationProperty, and initialize it how it should be
		TileProperty animationProperty = propertyMap.get(AnimationProperty.class.getName());
		if(animationProperty instanceof SingleFrame)
			((SingleFrame)animationProperty).initialize(GameCore.tileAtlas.findRegion(name().toLowerCase()));
		else
			((Animated)animationProperty).initialize(GameCore.tileAtlas.findRegions(name().toLowerCase()));
		
		this.solidProperty = (SolidProperty)propertyMap.get(SolidProperty.class.getName());
		this.destructibleProperty = (DestructibleProperty)propertyMap.get(DestructibleProperty.class.getName());
		this.interactableProperty = (InteractableProperty)propertyMap.get(InteractableProperty.class.getName());
		this.touchListener = (TouchListener)propertyMap.get(TouchListener.class.getName());
		this.animationProperty = (AnimationProperty) animationProperty;
		
		int curIdx = 0;
		
		for(TileProperty prop: properties) {
			propertyDataIndexes.put(prop, curIdx);
			curIdx += prop.getDataLength();
		}
		
		dataLength = curIdx;
	}
}
