package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;


import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InteractableProperty extends TilePropertyInstance {
	
	boolean interact(Player player, @Nullable Item heldItem, Tile tile);
	
}
