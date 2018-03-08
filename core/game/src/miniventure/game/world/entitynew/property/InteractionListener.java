package miniventure.game.world.entitynew.property;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface InteractionListener extends EntityProperty {
	boolean interact(Player player, Item heldItem, Entity e);
	
	default InteractionListener combineProperty(InteractionListener other) {
		return (player, heldItem, e) -> {
			boolean val = other.interact(player, heldItem, e);
			val = interact(player, heldItem, e) || val;
			return val;
		};
	}
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return InteractionListener.class; }
}
