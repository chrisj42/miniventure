package miniventure.game.world.entitynew;

import miniventure.game.item.Item;
import miniventure.game.world.WorldObject;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AttackProperty extends EntityProperty {
	
	boolean attackedBy(WorldObject obj, @Nullable Item attackItem, int damage, Entity e);
	
}
