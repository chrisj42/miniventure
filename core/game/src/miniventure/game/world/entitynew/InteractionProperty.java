package miniventure.game.world.entitynew;


import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InteractionProperty extends EntityProperty {
	
	boolean interactWith(Player player, @Nullable Item heldItem, Entity e);
	
}
