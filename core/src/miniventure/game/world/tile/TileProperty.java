package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.world.tile.AnimationProperty.SingleFrame;

public interface TileProperty {
	
	HashMap<Class<? extends TileProperty>, TileProperty> defaultProperties = new HashMap<Class<? extends TileProperty>, TileProperty>() {
		{
			put(SolidProperty.class, SolidProperty.WALKABLE);
			put(DestructibleProperty.class, DestructibleProperty.INDESTRUCTIBLE);
			put(InteractableProperty.class, (InteractableProperty)((p, i, t) -> {}));
			put(TouchListener.class, (TouchListener)(entity -> {}));
			put(AnimationProperty.class, new SingleFrame());
		}
	};
	
	int getDataLength();
	
}
