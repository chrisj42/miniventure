package miniventure.game.world.entitynew.property;

import miniventure.game.item.Item;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface AttackListener extends EntityProperty {
	boolean attackedBy(WorldObject obj, Item heldItem, int damage, Entity e);
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return AttackListener.class; }
}
