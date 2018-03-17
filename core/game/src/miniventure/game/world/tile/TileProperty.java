package miniventure.game.world.tile;

import miniventure.game.world.tile.AnimationProperty.AnimationType;

import org.jetbrains.annotations.NotNull;

public interface TileProperty {
	
	static TileProperty[] getDefaultProperties() {
		return new TileProperty[] {
			SolidProperty.WALKABLE,
			DestructibleProperty.INDESTRUCTIBLE,
			(InteractableProperty) ((p, i, t) -> false),
			(TouchListener) ((entity, tile, initial) -> {}),
			new AnimationProperty(true, AnimationType.SINGLE_FRAME),
			new ConnectionProperty(false),
			new OverlapProperty(false),
			(UpdateProperty) (delta, tile) -> {},
			(LightProperty) () -> 0,
			new TransitionProperty()
		};
	}
	
	default String[] getInitialData() { return new String[0]; }
	
	default void init(@NotNull TileType type) {}
	
	// return the class right below the one that signifies the common class for the properties.
	// essentially, this will be used for sorting and such; property classes that are specific to a certain set should return themselves, while any that extend them should do nothing.
	Class<? extends TileProperty> getUniquePropertyClass();
}
