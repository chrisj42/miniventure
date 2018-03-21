package miniventure.game.world.entitynew;

import miniventure.game.world.Level;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RemovalListener extends EntityProperty {
	
	void entityRemoved(Level prevLevel, Entity e);
	
}
