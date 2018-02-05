package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.world.tile.AnimationProperty.AnimationType;

public interface TileProperty {
	
	static HashMap<Class<? extends TileProperty>, TileProperty> getDefaultPropertyMap() {
		HashMap<Class<? extends TileProperty>, TileProperty> map = new HashMap<>();
		map.put(SolidProperty.class, SolidProperty.WALKABLE);
		map.put(DestructibleProperty.class, DestructibleProperty.INDESTRUCTIBLE);
		map.put(InteractableProperty.class, (InteractableProperty)((p, i, t) -> false));
		map.put(TouchListener.class, (TouchListener)((entity, tile) -> {}));
		map.put(AnimationProperty.class, new AnimationProperty(true, AnimationType.SINGLE_FRAME));
		map.put(ConnectionProperty.class, new ConnectionProperty(false));
		map.put(OverlapProperty.class, new OverlapProperty(false));
		map.put(UpdateProperty.class, (UpdateProperty)(delta, tile) -> {});
		map.put(CoveredTileProperty.class, new CoveredTileProperty((TileType[])null));
		map.put(LightProperty.class, (LightProperty) () -> 0);
		return map;
	}
	
	default String[] getInitData() { return new String[0]; }
	
	default void init(TileType type) {}
}
