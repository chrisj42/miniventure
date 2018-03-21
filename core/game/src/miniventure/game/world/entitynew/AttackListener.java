package miniventure.game.world.entitynew;

import miniventure.game.item.Item;
import miniventure.game.world.WorldObject;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AttackListener extends EntityProperty {
	
	void attackedBy(WorldObject obj, @Nullable Item attackItem, int damage, Entity e);
	
}
