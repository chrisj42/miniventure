package miniventure.game.world.tile;

import java.util.LinkedHashMap;

import miniventure.game.world.tile.AnimationProperty.SingleFrame;

public interface TileProperty {
	
	static LinkedHashMap<String, TileProperty> getDefaultPropertyMap() {
		LinkedHashMap<String, TileProperty> map = new LinkedHashMap<>();
		map.put(SolidProperty.class.getName(), SolidProperty.WALKABLE);
		map.put(DestructibleProperty.class.getName(), DestructibleProperty.INDESTRUCTIBLE);
		map.put(InteractableProperty.class.getName(), (InteractableProperty)((p, i, t) -> {}));
		map.put(TouchListener.class.getName(), (TouchListener)(entity -> {}));
		map.put(AnimationProperty.class.getName(), new SingleFrame());
		return map;
	}
	
	int getDataLength();
	
}
