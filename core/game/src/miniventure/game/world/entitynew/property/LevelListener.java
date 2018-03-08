package miniventure.game.world.entitynew.property;

import miniventure.game.world.Level;
import miniventure.game.world.entitynew.Entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface LevelListener extends EntityProperty {
	// you can get the new level through the entity itself, so it doesn't need to be a parameter.
	void levelChanged(@NotNull Entity e, @Nullable Level oldLevel);
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return LevelListener.class; }
}
