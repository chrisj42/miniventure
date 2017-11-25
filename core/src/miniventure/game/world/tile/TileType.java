package miniventure.game.world.tile;

import java.util.HashMap;
import java.util.LinkedHashMap;

import miniventure.game.GameCore;
import miniventure.game.world.tile.AnimationProperty.Animated;
import miniventure.game.world.tile.AnimationProperty.SingleFrame;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	GRASS();
	
	final SolidProperty solidProperty;
	final DestructibleProperty destructibleProperty;
	final InteractableProperty interactableProperty;
	final TouchListener touchListener;
	final AnimationProperty animationProperty;
	
	final HashMap<TileProperty, Integer> propertyDataIndexes = new HashMap<>();
	final int dataLength;
	
	TileType(@NotNull TileProperty... properties) {
		
		// get the default properties
		LinkedHashMap<Class<? extends TileProperty>, TileProperty> propertyMap = new LinkedHashMap<>(TileProperty.defaultProperties);
		
		// replace the defaults with specified properties
		for(TileProperty property: properties)
			propertyMap.put(property.getClass(), property);
		
		// fetch the animationProperty, and initialize it how it should be
		TileProperty animationProperty = propertyMap.get(AnimationProperty.class);
		if(animationProperty instanceof SingleFrame)
			((SingleFrame)animationProperty).initialize(GameCore.tileAtlas.findRegion(name().toLowerCase()));
		else
			((Animated)animationProperty).initialize(GameCore.tileAtlas.findRegions(name().toLowerCase()));
		
		this.solidProperty = (SolidProperty)propertyMap.get(SolidProperty.class);
		this.destructibleProperty = (DestructibleProperty)propertyMap.get(DestructibleProperty.class);
		this.interactableProperty = (InteractableProperty)propertyMap.get(InteractableProperty.class);
		this.touchListener = (TouchListener)propertyMap.get(TouchListener.class);
		this.animationProperty = (AnimationProperty) animationProperty;
		
		int curIdx = 0;
		
		for(TileProperty prop: properties) {
			propertyDataIndexes.put(prop, curIdx);
			curIdx += prop.getDataLength();
		}
		
		dataLength = curIdx;
	}
}
