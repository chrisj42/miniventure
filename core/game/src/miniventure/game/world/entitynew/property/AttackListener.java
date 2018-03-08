package miniventure.game.world.entitynew.property;

import miniventure.game.item.Item;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface AttackListener extends EntityProperty {
	boolean attackedBy(WorldObject obj, Item heldItem, int damage, Entity e);
	
	default AttackListener combineProperty(AttackListener other) {
		return (obj, heldItem, damage, e) -> {
			boolean val = other.attackedBy(obj, heldItem, damage, e);
			val = attackedBy(obj, heldItem, damage, e) || val;
			return val;
		};
	}
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return AttackListener.class; }
}
