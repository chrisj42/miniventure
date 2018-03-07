package miniventure.game.world.tile;

import miniventure.game.api.Property;
import miniventure.game.api.PropertyFetcher;
import miniventure.game.world.tile.AnimationProperty.AnimationType;

import org.jetbrains.annotations.NotNull;

public interface TileProperty extends Property<TileProperty> {
	
	static PropertyFetcher<TileProperty> getDefaultPropertyMap() {
		return () -> new TileProperty[] {
			SolidProperty.WALKABLE,
			DestructibleProperty.INDESTRUCTIBLE,
			(InteractableProperty) ((p, i, t) -> false),
			(TouchListener) ((entity, tile) -> {}),
			new AnimationProperty(true, AnimationType.SINGLE_FRAME),
			new ConnectionProperty(false),
			new OverlapProperty(false),
			(UpdateProperty) (delta, tile) -> {},
			new CoveredTileProperty((TileType[]) null),
			(LightProperty) () -> 0,
			new TransitionProperty()
		};
	}
	
	default String[] getInitialData() { return new String[0]; }
	
	default void init(@NotNull TileType type) {}
}
