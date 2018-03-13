package miniventure.game.item.typenew;

import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

@FunctionalInterface
public interface InteractProperty extends ItemProperty {
	
	boolean interact(WorldObject obj, Player player, Item item);
	
	@Override
	default Class<? extends ItemProperty> getUniquePropertyClass() { return InteractProperty.class; }
}
