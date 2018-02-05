package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;

@FunctionalInterface
public interface InteractableProperty extends TileProperty {
	
	boolean interact(Player player, Item heldItem, Tile tile);
	
}
