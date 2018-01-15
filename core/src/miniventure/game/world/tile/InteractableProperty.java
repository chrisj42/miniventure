package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;

@FunctionalInterface
public interface InteractableProperty extends TileProperty {
	
	//InteractableProperty NONE = (p, i, t) -> {};
	
	boolean interact(Player player, Item heldItem, Tile tile);
	
	@Override
	default Integer[] getInitData() { return new Integer[0]; }
}
