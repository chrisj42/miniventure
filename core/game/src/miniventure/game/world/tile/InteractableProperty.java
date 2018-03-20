package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tilenew.Tile;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InteractableProperty extends TileProperty {
	
	boolean interact(Player player, @Nullable Item heldItem, Tile tile);
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return InteractableProperty.class; }
}
