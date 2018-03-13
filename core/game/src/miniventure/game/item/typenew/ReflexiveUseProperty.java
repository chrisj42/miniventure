package miniventure.game.item.typenew;

import miniventure.game.world.entity.mob.Player;

@FunctionalInterface
public interface ReflexiveUseProperty extends ItemProperty {
	
	void interact(Player player, Item item);
	
	@Override
	default Class<? extends ItemProperty> getUniquePropertyClass() { return ReflexiveUseProperty.class; }
}
